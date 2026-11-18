"""
Make every user rate some movies and add some movies to their watchlist.

This matches the real relationships created by:
backend/rating-service/src/main/java/com/neo/rating_service/repositories/RatingRepository.java (RATED, with a score property)
backend/movie-service/src/main/java/com/neo/movie_service/repositories/MovieRepository.java (WANTS_TO_WATCH)

heeemzaaa and von (the two admins) each get exactly 10 ratings and
10 watchlist movies. Every other user gets a random number of each.

How to run:
    export NEO4J_PASSWORD=<same password as in your .env file>
    python scripts/generate_ratings_and_watchlist.py
"""

import os
import random
from datetime import datetime, timezone

from neo4j import GraphDatabase

NEO4J_URI = "bolt://localhost:7687"
NEO4J_USER = "neo4j"
NEO4J_PASSWORD = os.environ.get("NEO4J_PASSWORD")

FIXED_USERNAMES = ["heeemzaaa", "von"]
FIXED_RATINGS_COUNT = 10
FIXED_WATCHLIST_COUNT = 10

RANDOM_RATINGS_RANGE = (0, 10)
RANDOM_WATCHLIST_RANGE = (0, 10)


def fetch_users(session):
    result = session.run("MATCH (u:User) RETURN u.id AS id, u.username AS username")
    return [{"id": record["id"], "username": record["username"]} for record in result]


def fetch_movie_ids(session):
    result = session.run("MATCH (m:Movie) RETURN m.movieId AS id")
    return [record["id"] for record in result]


def pick_movies(movie_ids, count):
    count = min(count, len(movie_ids))
    return random.sample(movie_ids, count)


def build_ratings(user_id, movie_ids):
    now = datetime.now(timezone.utc)
    return [
        {
            "userId": user_id,
            "movieId": movie_id,
            "score": random.randint(1, 5),
            "createdAt": now,
            "updatedAt": now,
        }
        for movie_id in movie_ids
    ]


def build_watchlist_items(user_id, movie_ids):
    return [{"userId": user_id, "movieId": movie_id} for movie_id in movie_ids]


def main():
    if not NEO4J_PASSWORD:
        raise SystemExit("Set the NEO4J_PASSWORD environment variable first (same value as in .env).")

    driver = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_USER, NEO4J_PASSWORD))

    with driver.session() as session:
        users = fetch_users(session)
        movie_ids = fetch_movie_ids(session)

    if not movie_ids:
        raise SystemExit("No movies found. Run generate_movies.py first.")

    all_ratings = []
    all_watchlist_items = []

    for user in users:
        if user["username"] in FIXED_USERNAMES:
            rating_count = FIXED_RATINGS_COUNT
            watchlist_count = FIXED_WATCHLIST_COUNT
        else:
            rating_count = random.randint(*RANDOM_RATINGS_RANGE)
            watchlist_count = random.randint(*RANDOM_WATCHLIST_RANGE)

        rated_movies = pick_movies(movie_ids, rating_count)
        watchlist_movies = pick_movies(movie_ids, watchlist_count)

        all_ratings.extend(build_ratings(user["id"], rated_movies))
        all_watchlist_items.extend(build_watchlist_items(user["id"], watchlist_movies))

    with driver.session() as session:
        session.run(
            """
            UNWIND $ratings AS r
            MATCH (u:User {id: r.userId}), (m:Movie {movieId: r.movieId})
            MERGE (u)-[rel:RATED]->(m)
            SET rel.score = r.score, rel.createdAt = r.createdAt, rel.updatedAt = r.updatedAt
            """,
            ratings=all_ratings,
        )

        session.run(
            """
            UNWIND $items AS i
            MATCH (u:User {id: i.userId}), (m:Movie {movieId: i.movieId})
            MERGE (u)-[:WANTS_TO_WATCH]->(m)
            """,
            items=all_watchlist_items,
        )

    driver.close()

    print(f"Created {len(all_ratings)} ratings and {len(all_watchlist_items)} watchlist entries across {len(users)} users.")
    print("heeemzaaa and von each got exactly 10 ratings and 10 watchlist movies.")


if __name__ == "__main__":
    main()
