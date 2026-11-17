import os
import time
import uuid
from datetime import datetime, timezone

import requests
from neo4j import GraphDatabase

NEO4J_URI = "bolt://localhost:7687"
NEO4J_USER = "neo4j"
NEO4J_PASSWORD = os.environ.get("NEO4J_PASSWORD")
TMDB_API_KEY = os.environ.get("TMDB_API_KEY")

TMDB_BASE_URL = "https://api.themoviedb.org/3"
POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"

TARGET_MOVIE_COUNT = 200
MAX_PAGES = 20  # safety limit, some movies get skipped (missing poster/runtime/genre)


def fetch_popular_movie_ids():
    ids = []
    page = 1
    while len(ids) < TARGET_MOVIE_COUNT * 2 and page <= MAX_PAGES:
        response = requests.get(
            f"{TMDB_BASE_URL}/discover/movie",
            params={"api_key": TMDB_API_KEY, "sort_by": "popularity.desc", "page": page},
        )
        response.raise_for_status()
        for movie in response.json().get("results", []):
            ids.append(movie["id"])
        page += 1
    return ids


def fetch_movie_details(tmdb_id):
    response = requests.get(
        f"{TMDB_BASE_URL}/movie/{tmdb_id}",
        params={"api_key": TMDB_API_KEY, "append_to_response": "credits,videos"},
    )
    response.raise_for_status()
    return response.json()


def find_director(credits):
    for person in credits.get("crew", []):
        if person.get("job") == "Director":
            return person.get("name")
    return None


def find_trailer_url(videos):
    for video in videos.get("results", []):
        if video.get("site") == "YouTube" and video.get("type") == "Trailer":
            return f"https://www.youtube.com/watch?v={video['key']}"
    return None


def build_movie(data):
    poster_path = data.get("poster_path")
    release_date = data.get("release_date")
    runtime = data.get("runtime")
    genres = [g["name"] for g in data.get("genres", [])]

    # skip movies that are missing information we need
    if not poster_path or not release_date or not runtime or not genres:
        return None

    now = datetime.now(timezone.utc)
    return {
        "id": str(uuid.uuid4()),
        "title": data["title"],
        "description": data.get("overview") or "",
        "releaseYear": int(release_date[:4]),
        "durationMinutes": runtime,
        "posterUrl": POSTER_BASE_URL + poster_path,
        "trailerUrl": find_trailer_url(data.get("videos", {})),
        "director": find_director(data.get("credits", {})) or "Unknown",
        "imdbRating": data.get("vote_average"),
        "createdAt": now,
        "updatedAt": now,
        "genres": genres,
    }


def main():
    if not NEO4J_PASSWORD:
        raise SystemExit("Set NEO4J_PASSWORD first (same value as in .env).")
    if not TMDB_API_KEY:
        raise SystemExit("Set TMDB_API_KEY first (get a free one at themoviedb.org).")

    movies = []
    seen_titles = set()

    for tmdb_id in fetch_popular_movie_ids():
        if len(movies) >= TARGET_MOVIE_COUNT:
            break

        details = fetch_movie_details(tmdb_id)
        time.sleep(0.05)  # be polite to the API
        movie = build_movie(details)

        if movie is None or movie["title"] in seen_titles:
            continue

        seen_titles.add(movie["title"])
        movies.append(movie)

    all_genre_names = sorted({name for m in movies for name in m["genres"]})

    driver = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_USER, NEO4J_PASSWORD))

    with driver.session() as session:
        session.run(
            "UNWIND $names AS name MERGE (:Genre {name: name})",
            names=all_genre_names,
        )

        session.run(
            """
            UNWIND $movies AS m
            CREATE (movie:Movie {
                movieId: m.id,
                title: m.title,
                plot: m.description,
                year: m.releaseYear,
                runtime: m.durationMinutes,
                poster: m.posterUrl,
                trailerUrl: m.trailerUrl,
                director: m.director,
                imdbRating: m.imdbRating,
                createdAt: m.createdAt,
                updatedAt: m.updatedAt
            })
            WITH movie, m.genres AS genreNames
            UNWIND genreNames AS genreName
            MATCH (g:Genre {name: genreName})
            MERGE (movie)-[:IN_GENRE]->(g)
            """,
            movies=movies,
        )

    driver.close()

    print(f"Created {len(movies)} movies and {len(all_genre_names)} genres.")


if __name__ == "__main__":
    main()
