# Neo4flix

Neo4flix is a movie recommendation platform. You can browse a catalogue of movies, rate them, keep a watchlist, add friends, share movies with them, and get personalised recommendations computed from the graph of who rated what.

The backend is a set of **Spring Boot microservices** that share a single **Neo4j** graph database. They register with a **Eureka** discovery server and sit behind a **Spring Cloud Gateway** that handles JWT authentication. The frontend is an **Angular 22** single-page app styled with **Tailwind CSS 4**. A set of **Python scripts** seeds the database with real movie data from TMDB and with generated users, ratings and watchlists.

---

## Table of contents

- [Architecture](#architecture)
- [Repository layout](#repository-layout)
- [Tech stack](#tech-stack)
- [The graph data model](#the-graph-data-model)
- [How authentication works](#how-authentication-works)
- [Backend services](#backend-services)
  - [discovery-service](#discovery-service)
  - [api-gateway](#api-gateway)
  - [user-service](#user-service)
  - [movie-service](#movie-service)
  - [rating-service](#rating-service)
  - [recommendation-service](#recommendation-service)
- [Frontend](#frontend)
- [Seed scripts](#seed-scripts)
- [Configuration](#configuration)
- [Running the project](#running-the-project)
- [Testing](#testing)
- [Response format](#response-format)

---

## Architecture

```
                        ┌──────────────────────────┐
   Browser ───────────► │  frontend (Angular SPA)  │  :4200
                        └────────────┬─────────────┘
                                     │  HTTP + "Authorization: Bearer <JWT>"
                                     ▼
                        ┌──────────────────────────┐        ┌──────────────────────┐
                        │       api-gateway        │ ◄────► │  discovery-service   │
                        │ JWT check, CORS, routing │        │   (Eureka) :8761     │
                        │          :8080           │        └──────────▲───────────┘
                        └────────────┬─────────────┘                   │ register
                 adds Neo-User-Id / Neo-User-Role headers              │
         ┌──────────────┬────────────┴─────┬──────────────────┐        │
         ▼              ▼                  ▼                  ▼        │
   user-service   movie-service     rating-service   recommendation-service
      :8084           :8081              :8082              :8083
         └──────────────┴────────┬─────────┴──────────────────┘
                                 ▼
                        ┌──────────────────┐
                        │   Neo4j 5 graph  │  Bolt :7687 / Browser :7474
                        └──────────────────┘
```

- The frontend only talks to the **api-gateway** on port 8080.
- The gateway validates the JWT, decides whether the request is allowed, and forwards it to the right service. It finds each service through Eureka (`lb://<service-name>`), so services are never called by hard-coded address.
- Every domain service reads and writes the **same Neo4j database**. Instead of calling each other over HTTP, services connect through the graph: a `RATED` relationship written by rating-service links the `User` nodes from user-service to the `Movie` nodes from movie-service, and recommendation-service reads all of them in one Cypher query.

## Repository layout

```
neo-4-flix/
├── backend/
│   ├── discovery-service/        Eureka server
│   ├── api-gateway/              Spring Cloud Gateway (WebMVC) + JWT security
│   ├── user-service/             Accounts, auth, JWT issuing, 2FA, friends
│   ├── movie-service/            Movie catalogue, genres, watchlist, sharing
│   ├── rating-service/           1–5 star ratings, stats, taste comparison
│   └── recommendation-service/   Collaborative / trending / similar-movie recommendations
├── frontend/                     Angular 22 + Tailwind 4 app
├── scripts/                      Python seeders (TMDB movies, users, ratings, watchlists)
├── docker-compose.yml            Runs Neo4j + all services + frontend
└── .env.example                  Environment variables you need to fill in
```

Each backend service is an independent Maven project with its own `pom.xml`, Maven wrapper (`mvnw`) and multi-stage `Dockerfile`.

## Tech stack

| Layer | Technology |
|---|---|
| Language (backend) | Java 17 |
| Framework | Spring Boot 4.1.2, Spring Cloud 2025.1.4 |
| Discovery | Spring Cloud Netflix Eureka |
| Gateway | Spring Cloud Gateway Server WebMVC |
| Security | Spring Security, JJWT 0.11.5 (HS256), `dev.samstevens.totp` 1.7.1 for 2FA |
| Persistence | Spring Data Neo4j + `Neo4jClient` for hand-written Cypher |
| Other | Lombok, Jakarta Validation, Spring Boot Actuator |
| Database | Neo4j 5 |
| Frontend | Angular 22 (standalone components, signals), RxJS 7, Tailwind CSS 4, `angularx-qrcode` |
| Frontend tests | Vitest + jsdom |
| Seeding | Python 3, `neo4j` driver, `requests`, `bcrypt`, `faker`, TMDB API |
| Containers | Docker, Docker Compose |

## The graph data model

```
(:User)-[:FRIEND_OF]->(:User)
(:User)-[:RATED {score, createdAt, updatedAt}]->(:Movie)
(:User)-[:WANTS_TO_WATCH]->(:Movie)                       // watchlist
(:User)-[:SHARED_WITH {sharedBy, sharedAt}]->(:Movie)     // recipient -> movie
(:Movie)-[:IN_GENRE]->(:Genre)
```

**`User`**: `id` (UUID), `firstName`, `lastName`, `username`, `email`, `password` (BCrypt hash), `role` (`USER` | `ADMIN`), `twoFactorEnabled`, `twoFactorSecret`, `createdAt`, `updatedAt`.

**`Movie`**: `movieId` (UUID), `title`, `plot`, `year`, `runtime` (minutes), `poster`, `trailerUrl`, `director`, `imdbRating`, `createdAt`, `updatedAt`.
In the Java model these are exposed as `id`, `description`, `releaseYear`, `durationMinutes` and `posterUrl`.

**`Genre`**: `name` (unique, used as the id).

`FRIEND_OF` is stored with a direction but always queried without one, so a friendship works both ways.

**Constraints and indexes.** These are created automatically on startup by `CommandLineRunner` beans:

| Created by | Statement |
|---|---|
| user-service (`UserConstraintsInitializer`) | unique `User.email`, unique `User.username` |
| movie-service (`MovieConstraintsInitializer`) | unique `Movie.movieId`, unique `Genre.name`, index on `Movie.title`, index on `Movie.year` |

## How authentication works

1. **Register or log in.** The client calls `POST /api/auth/register` or `POST /api/auth/login` (with a username *or* an email). user-service checks the BCrypt password and returns a JWT.
2. **JWT contents.** The token is signed with HS256 using `JWT_SECRET_KEY`. It carries the claims `userId` and `role`, and it **expires after 1 hour**.
3. **Two-factor authentication.** If the account has 2FA turned on, login returns `twoFactorRequired: true` and no token. The client must then call `POST /api/auth/2fa/verify` with the 6-digit TOTP code to get the token. Any standard authenticator app works (Google Authenticator, Authy, and so on); the issuer shown is `Neo4flix`.
4. **At the gateway.** `JwtAuthenticationFilter` reads `Authorization: Bearer <token>`, checks the signature and expiry, and fills in the Spring Security context. `SecurityConfig` then applies the access rules below. Before forwarding, `UserClaimsGatewayFilter` **removes** any `Neo-User-Id` / `Neo-User-Role` headers the client sent and sets them again from the verified token, so a client cannot spoof them.
5. **In each service.** `HeaderAuthFilter` turns those two headers back into a Spring `Authentication` (principal = user id, authority = `ROLE_<role>`). Methods are protected with `@PreAuthorize`. The services do not check JWTs themselves; they rely on the gateway, so **only the gateway should be reachable from outside**. In `docker-compose.yml` only the gateway, Eureka, Neo4j and the frontend publish ports.

**Gateway access rules** (`api-gateway/.../SecurityConfig.java`):

| Path | Access |
|---|---|
| `POST /api/auth/login`, `/api/auth/register`, `/api/auth/2fa/verify` | public |
| `/api/movies/watchlist`, `/api/movies/{id}/watchlist` | authenticated |
| `/api/movies/shared`, `/api/movies/{id}/share/{recipientId}` | authenticated |
| `GET /api/movies/**`, `GET /api/genres/**` | public |
| any other `/api/movies/**` (create / update / delete) | `ADMIN` only |
| `GET /api/ratings/movie/**` | public |
| everything else | authenticated |

Errors from the gateway's security layer come back as JSON: `401 {"success":false,"message":"Invalid credentials !"}` or `403 {"success":false,"message":"Access denied !"}`.

---

## Backend services

### discovery-service

**Port 8761**. A Eureka server (`@EnableEurekaServer`) where every other backend service registers itself. It does not register with itself (`register-with-eureka: false`). Open `http://localhost:8761` for the Eureka dashboard to see which instances are up.

### api-gateway

**Port 8080**. The single entry point for the frontend, built on Spring Cloud Gateway **WebMVC** (servlet-based, not reactive).

| Class | Role |
|---|---|
| `GatewayRoutesConfig` | Defines four routes and load-balances each one through Eureka |
| `JwtAuthenticationFilter` / `JwtService` | Validates the bearer token and extracts `userId` and `role` |
| `SecurityConfig` | Stateless security, CSRF disabled, CORS, the access rules above |
| `UserClaimsGatewayFilter` | Puts the verified identity into the `Neo-User-Id` / `Neo-User-Role` headers |
| `SecurityErrorHandler` | Returns JSON bodies for 401 and 403 errors |

**Routes:**

| Path prefix | Forwarded to |
|---|---|
| `/api/auth/**`, `/api/users/**` | `user-service` |
| `/api/movies/**`, `/api/genre*` | `movie-service` |
| `/api/ratings/**` | `rating-service` |
| `/api/recommendations/**` | `recommendation-service` |

**CORS** allows the origins listed in `CORS_ALLOWED_ORIGINS` (comma-separated), the methods `GET, POST, PUT, PATCH, DELETE, OPTIONS`, and the headers `Authorization, Content-Type, Accept`.

### user-service

**Port 8084**. Handles accounts, login, JWT issuing, TOTP 2FA, user search and friendships.

**Auth endpoints (`/api/auth`), public:**

| Method | Path | Body | Description |
|---|---|---|---|
| POST | `/register` | `firstName, lastName, username, email, password` | Creates a `USER` account and returns a JWT (201) |
| POST | `/login` | `usernameOrEmail, password` | Returns a JWT, or `twoFactorRequired: true` |
| POST | `/2fa/verify` | `usernameOrEmail, code` | Exchanges a valid TOTP code for a JWT |

Registration rules: username 3–30 characters, a valid email, and a password of at least 8 characters with an uppercase letter, a lowercase letter, a digit and a symbol. Duplicate usernames or emails are rejected.

**User endpoints (`/api/users`), authenticated:**

| Method | Path | Description |
|---|---|---|
| GET | `/me` | The current user's full profile |
| PUT | `/me` | Update the profile |
| PUT | `/me/password` | Change the password |
| POST | `/me/verify-password` | Re-confirm the password (used before sensitive actions) |
| DELETE | `/me` | Delete your own account |
| GET | `/{id}` | Another user's public profile |
| GET | `/search?q=` | Search users by name or username |
| DELETE | `/{id}` | Delete a user (yourself, or anyone if you are `ADMIN`; admin accounts cannot be deleted) |
| GET | `/me/friends` | List your friends |
| POST | `/me/friends/{id}` | Add a friend (creates `FRIEND_OF`) |
| DELETE | `/me/friends/{id}` | Remove a friend |
| POST | `/me/2fa/enable` | Generate a TOTP secret and return the `otpauth://` QR-code URI |
| POST | `/me/2fa/confirm` | Confirm setup with a first valid code (2FA is switched on only after this) |
| POST | `/me/2fa/disable` | Turn 2FA off |

### movie-service

**Port 8081**. Handles the movie catalogue, genres, watchlists and movie sharing.

**Movies (`/api/movies`):**

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/` | public | Search with paging. Query params: `title` (case-insensitive contains), `year`, `genre`, `page` (default 0), `size` (default 10), `sortBy` (`title` by default; `year`/`releaseYear`, `imdbRating`, and so on), `direction` (`asc`/`desc`) |
| GET | `/{id}` | public | One movie with its genres |
| POST | `/` | ADMIN | Create a movie (`title`, `description`, `releaseYear` 1888–2100, `durationMinutes` ≥ 1, optional `posterUrl`, `trailerUrl`, `director`, `genres`). Rejects duplicate titles |
| PUT | `/{id}` | ADMIN | Update a movie |
| DELETE | `/{id}` | ADMIN | Delete a movie |
| GET | `/watchlist?page=&size=` | auth | Your watchlist, paged and sorted by title |
| POST | `/{id}/watchlist` | auth | Add to the watchlist (`WANTS_TO_WATCH`) |
| DELETE | `/{id}/watchlist` | auth | Remove from the watchlist |
| POST | `/{id}/share/{recipientId}` | auth | Share a movie with another user. Creates `(recipient)-[:SHARED_WITH {sharedBy, sharedAt}]->(movie)` |
| GET | `/shared` | auth | Movies shared with you, newest first |

**Genres:** `GET /api/genres` (alias `/api/genre`), public. Lists all genres.

Paged responses use `PageResponse` (content plus page metadata).

### rating-service

**Port 8082**. Stores ratings as the `RATED` relationship between a user and a movie. Scores are whole numbers from **1 to 5**.

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/ratings` | auth | Rate a movie: `{ movieId, score }`. Fails if you already rated it |
| PUT | `/api/ratings/{movieId}` | auth | Change your score: `{ score }` |
| DELETE | `/api/ratings/{movieId}` | auth | Remove your rating |
| GET | `/api/ratings/me` | auth | All your ratings |
| GET | `/api/ratings/movie/{movieId}` | public | All ratings for a movie |
| GET | `/api/ratings/user/{userId}/stats` | auth | `ratingsCount` and `averageRating` for a user |
| GET | `/api/ratings/user/{userId}/compare` | auth | Compares your taste with another user's (see below) |

**Taste comparison** (`/compare`) returns:
- `bothRatedCount`: the number of movies you both rated.
- `tasteMatchPercent`: `100 − (average |myScore − theirScore| / 4 × 100)`, rounded. 100 means identical scores; 0 means maximally opposite. It is `null` if you have no movies in common.
- `bothLoved`: movies you both rated **4 or higher**.
- `recentRatings`: movies the other user rated that you have not rated yet.

### recommendation-service

**Port 8083**. Read-only. Builds recommendations directly in Cypher on the shared graph.

| Method | Path | Params | Description |
|---|---|---|---|
| GET | `/api/recommendations` | `genre`, `year`, `limit` (default 10, max 50) | Personalised recommendations |
| GET | `/api/recommendations/trending` | `genre`, `year`, `limit` (default 10, max 50) | Top movies by IMDb rating |
| GET | `/api/recommendations/movies/{movieId}` | `limit` (default 5, max 20) | Movies similar to the given one |

All three routes require a valid JWT at the gateway.

**How personalised recommendations are chosen:**

1. **Cold start.** If the user has not rated anything, return trending movies (highest `imdbRating`), excluding anything already rated or on the watchlist.
2. **Collaborative filtering.** Find other users who rated **4 or higher** at least one movie that *you* also rated 4 or higher. Then take the movies *those* users rated 4 or higher that you have not rated and that are not on your watchlist. Each candidate gets a score equal to the number of distinct like-minded users who loved it, with IMDb rating as a tie-breaker. Reason shown to the user: *"Users with similar tastes also loved this"*.
3. **Fill-up.** If collaborative filtering returns fewer than `limit` movies, the remaining slots are filled with trending movies, without duplicates.

**Similar movies** ranks other movies by how many genres they share with the given movie, then by IMDb rating (*"Because you viewed this movie"*).

The `genre` filter is case-insensitive and `year` filters on the exact release year. Every result is a `RecommendedMovieResponse` with the movie fields plus `genres`, `recommendationReason` and `matchScore`.

---

## Frontend

`frontend/` is an Angular 22 app that uses standalone components, signals, the new control-flow syntax (`@if`, `@for`) and Tailwind CSS 4. It calls the gateway at `API_BASE_URL = 'http://localhost:8080'` (in `src/app/core/http/api-config.ts`).

**Core (`src/app/core`)**

| File | Role |
|---|---|
| `auth/auth.service.ts` | Keeps the JWT in a signal and in `localStorage` under the key `neo4flix_token` (with an SSR-safe check) |
| `auth/auth.guard.ts` | Sends users who are not logged in to `/login` |
| `auth/auth.interceptor.ts` | Adds `Authorization: Bearer <token>` to every request |
| `http/error.interceptor.ts` | On a `401` from any non-auth endpoint, clears the token and redirects to `/login` |
| `models/` | Shared `ResponseData<T>` and `PageResponse<T>` types |

**Routes (`app.routes.ts`)**

| Path | Page | Guarded | What it does |
|---|---|---|---|
| `/login` | `Login` | no | Log in with a username or email. Redirects to `/verify-2fa` if 2FA is required |
| `/register` | `Register` | no | Sign up, then go to `/secure-account` |
| `/secure-account` | `SecureAccount` | no | Optional step after sign-up: set up 2FA by scanning a QR code and confirming a code |
| `/verify-2fa` | `Verify2fa` | no | Enter the TOTP code to finish logging in |
| `/` | `Home` | yes | Netflix-style home page: a hero "spotlight" movie, then carousels for *Recommended*, *Trending*, *Action*, *Drama* and *Shared with me*, all loaded in parallel |
| `/search` | `Browse` | yes | Full catalogue with search, genre and year filters, sorting and paging. Admins can add, edit and delete movies here |
| `/recommendations` | `Recommendations` | yes | Personalised and trending lists with genre and year filters |
| `/my-list` | `Watchlist` | yes | Your watchlist, your ratings, and movies friends shared with you |
| `/:userId` | `ProfileGate` | yes | Shows `Profile` if the id is yours, otherwise `UserProfile` |

**Profile pages**
- `Profile` (your own): account details, turning 2FA on or off (with QR code), password re-check, and account deletion.
- `UserProfile` (someone else's): their rating stats, the taste-match comparison with you, add or remove friend, and admin actions.

**Shared components**
- `shared/navbar`: navigation plus a live user search that links to profiles.
- `features/movies/components/movie-detail-modal`: movie details, trailer, rating stars, and the watchlist button.
- `features/movies/components/add-movie-modal`: admin form to create or edit a movie.
- `features/movies/components/share-movie-modal`: pick a friend and share a movie with them.

Each feature folder (`auth`, `movies`, `ratings`, `recommendations`, `users`, `watchlist`) has its own `models/` and an `*-api.service.ts` that wraps the matching backend endpoints.

## Seed scripts

`scripts/` contains Python scripts that fill a running Neo4j instance (`bolt://localhost:7687`) with demo data. Run them **in this order**, after the services have started at least once so the constraints exist:

| Script | Wrapper | What it creates |
|---|---|---|
| `generate_movies.py` | `run_generate_movies.sh` | Gets popular movies from the **TMDB API** (up to 20 pages) and stores **200** of them with poster, runtime, director, YouTube trailer, rating and genres. Movies missing a poster, release date, runtime or genre are skipped. Creates `Movie` and `Genre` nodes and `IN_GENRE` links |
| `generate_users.py` | `run_generate_users.sh` | **500** users: 2 fixed admin accounts (passwords from `HAMZA_PASSWORD` / `ADNANE_PASSWORD`) and 498 Faker users who all share the password `USERS_PASSWORD` (BCrypt-hashed). Also creates about 5 random `FRIEND_OF` links per user |
| `generate_ratings_and_watchlist.py` | `run_generate_ratings_and_watchlist.sh` or `run_seed.py` | Random `RATED` (score 1–5) and `WANTS_TO_WATCH` links: exactly 10 of each for the two admins, and 0–10 of each for everyone else |

The `.sh` wrappers load `.env` and run with `scripts/venv/bin/python`, so create that virtual environment first:

```bash
python3 -m venv scripts/venv
scripts/venv/bin/pip install neo4j requests bcrypt faker
```

---

## Configuration

Copy `.env.example` to `.env` at the project root and fill it in:

| Variable | Used by | Description |
|---|---|---|
| `NEO4J_PASSWORD` | Neo4j, all data services, scripts | Password for the `neo4j` user |
| `JWT_SECRET_KEY` | api-gateway, user-service | Shared HS256 signing secret. **At least 32 bytes**; it must be identical in both services |
| `CORS_ALLOWED_ORIGINS` | api-gateway | Comma-separated list, for example `http://localhost:4200` |
| `TMDB_API_KEY` | `generate_movies.py` | TMDB v3 API key |
| `HAMZA_PASSWORD`, `ADNANE_PASSWORD` | `generate_users.py` | Passwords for the two seeded admin accounts |
| `USERS_PASSWORD` | `generate_users.py` | Shared password for the 498 generated users |

You can generate a secret with `openssl rand -base64 48`.

Inside Docker Compose, each service also gets `SPRING_NEO4J_URI=bolt://neo4j:7687`, `SPRING_NEO4J_AUTHENTICATION_*` and `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-service:8761/eureka`.

## Running the project

### With Docker Compose (recommended)

```bash
cp .env.example .env        # then fill in the values
docker compose up --build
```

| URL | What |
|---|---|
| http://localhost:4200 | Frontend (static build served by `serve` on port 4000 inside the container) |
| http://localhost:8080 | API gateway |
| http://localhost:8761 | Eureka dashboard |
| http://localhost:7474 | Neo4j Browser (log in as `neo4j` / `NEO4J_PASSWORD`, Bolt `bolt://localhost:7687`) |

The data services wait until Neo4j's healthcheck passes. Data is kept in the `neo4j_data` volume.

Then seed the database from your host machine:

```bash
scripts/run_generate_movies.sh
scripts/run_generate_users.sh
scripts/run_generate_ratings_and_watchlist.sh
```

### Locally, without Docker

Requirements: JDK 17, Node 22+, and a Neo4j 5 instance on `localhost:7687`.

```bash
# 1. Neo4j only
docker compose up neo4j

# 2. Export the shared variables in each terminal you run a service from
export $(grep -v '^#' .env | xargs)
export SPRING_NEO4J_AUTHENTICATION_USERNAME=neo4j
export SPRING_NEO4J_AUTHENTICATION_PASSWORD=$NEO4J_PASSWORD

# 3. Start discovery first, then the gateway and the services (each in its own terminal)
cd backend/discovery-service      && ./mvnw spring-boot:run
cd backend/api-gateway            && ./mvnw spring-boot:run
cd backend/user-service           && ./mvnw spring-boot:run
cd backend/movie-service          && ./mvnw spring-boot:run
cd backend/rating-service         && ./mvnw spring-boot:run
cd backend/recommendation-service && ./mvnw spring-boot:run

# 4. Frontend
cd frontend && npm install && npm start   # http://localhost:4200
```

`user-service` and `rating-service` have no Neo4j settings in their `application.yaml`, so they depend on the `SPRING_NEO4J_*` environment variables above.

## Testing

Each service has Mockito/JUnit service-layer tests (`MovieServiceTest`, `RatingServiceTest`, `RecommendationServiceTest`, `UserServiceTest`):

```bash
cd backend/<service> && ./mvnw test
```

Frontend unit tests use Vitest:

```bash
cd frontend && npm test
```

## Response format

Every domain service wraps its responses in the same envelope (`ResponseData<T>`):

```json
{
  "success": true,
  "message": "Movies retrieved successfully",
  "data": { "...": "..." }
}
```

Errors use the same shape with `success: false` and `data: null`. Each service maps its exceptions (for example `MovieNotFoundException`, `RatingAlreadyExistsException`, `EmailAlreadyExistsException`, `InvalidTwoFactorCodeException`) to the right HTTP status in a `@RestControllerAdvice`, and validation errors return the field messages.
