# quarry

Chess data pipeline. Fetches games from Lichess and Chess.com, parses them into positions, evaluates those positions with Stockfish, and serves the results over a REST API.

quarry does the fetching and the computing. It does not interpret. Anything that requires chess understanding, such as opening repertoires, deviation detection or training, lives in the services that consume this API.

> **Status** Early development. Milestone 2 done, games are imported and stored. Parsing into positions is next.

## Why this exists

Two separate projects need the same underlying data.

| Consumer | What it needs from quarry |
|---|---|
| `deviate` | Normalised games to compare against a repertoire tree, plus evaluations to judge whether a deviation was bad |
| C++ chess engine | Reference evaluations to measure its own output against |

Building the import and evaluation layer twice would be wasteful and would produce two slightly different versions of the same data. quarry is that layer, built once.

The stored shape of position, evaluation and played move is also what a machine learning consumer would need, so that option stays open without being planned for now.

See [ARCHITECTURE.md](ARCHITECTURE.md) for the full system context and the reasoning behind the boundaries.

## Stack

- Java 25, Spring Boot
- PostgreSQL with Flyway migrations
- Stockfish over UCI, running as a pool of long lived processes
- [chesslib](https://github.com/bhlangonijr/chesslib) for PGN parsing and move generation
- Docker Compose for local infrastructure, Testcontainers for integration tests

## Getting started

Requirements are a JDK 25 and Docker.

```bash
git clone https://github.com/<user>/quarry.git
cd quarry
docker compose up -d
./mvnw spring-boot:run
```

Verify the application is up.

```bash
curl localhost:8080/actuator/health
```

PostgreSQL is published on host port `5433`, not the default `5432`, so it does not collide with a locally installed server. Flyway applies the schema on startup.

Stockfish is not required for the early milestones. Lichess exposes its own server side evaluations for a large share of games, which is enough to build the storage and query layers before the engine pool exists.

## Planned API

Three capabilities, nothing more.

```
POST /imports                     trigger an import for a player
GET  /players/{source}/{username}/games   normalised game history
POST /evaluations                 evaluate a batch of FEN positions
```

The evaluation endpoint answers synchronously for cached positions and enqueues the rest, returning a job reference. Callers are never blocked waiting for Stockfish.

## Roadmap

- [ ] **M1** Fetch a player's games from Lichess and count them
- [ ] **M2** Parse PGN into games and positions, persist to PostgreSQL
- [ ] **M3** Talk to a single Stockfish process over UCI, synchronously
- [ ] **M4** Engine pool, virtual thread orchestration, idempotent and resumable imports
- [ ] **M5** REST API and query layer
- [ ] **M6** Chess.com as a second source behind the same `GameSource` interface

Milestone 4 is the substance of this project. Everything before it is groundwork.

## Rate limits and etiquette

Both upstream APIs are free and run on donations. This project treats them accordingly.

- Lichess returns 429 without publishing exact limits. The client waits a full minute before retrying.
- Chess.com allows serial requests but rate limits parallel ones. Imports run sequentially per player.
- Every outbound request carries a descriptive User-Agent with contact details.
- Chess.com responses are cached by ETag so repeated runs revalidate instead of refetching.

## Licence

MIT
