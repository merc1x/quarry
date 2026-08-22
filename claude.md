# CLAUDE.md

## What this project is

quarry is a chess data pipeline. It fetches games from Lichess and Chess.com, parses them into positions, evaluates those positions with Stockfish, and serves the results over a REST API.

It is one of three services. `deviate` consumes this API and does the chess analysis. MoveForge is the user facing product. Read `ARCHITECTURE.md` before making structural decisions.

## Hard boundaries

These are not preferences. Violating them breaks the system design.

- quarry never knows about repertoires, users, training or MoveForge. If the word "repertoire" appears in this codebase, something has gone wrong.
- quarry depends on nothing. No outbound calls to `deviate` or MoveForge, ever.
- Raw games live only here. Consumers read them over the API, they do not copy them.
- quarry reports what the engine says. It does not decide what a good or bad move is.

## Stack

Java 25, Spring Boot, PostgreSQL with Flyway, chesslib for PGN parsing, Stockfish over UCI. Docker Compose for local infrastructure, Testcontainers for integration tests.

## Commands

```bash
docker compose up -d              # start postgres on localhost:5433
./mvnw spring-boot:run            # run the application
./mvnw test                       # run tests
curl localhost:8080/actuator/health
```

On Windows use `.\mvnw.cmd` and `curl.exe`.

## Package structure

Sliced by domain, not by layer. Controller, service, repository and entity for a given concept live together in that concept's package.

```
ch.iselaj.quarry
├── config/
├── source/          fetching, GameSource interface, per-platform clients
├── game/
├── position/
└── evaluation/
```

Adding a new game source should mean touching one subpackage under `source/` and nothing else.

## Git

I make all commits myself. Do not run `git commit`, `git push`, `git rebase` or any other command that writes to history, and do not stage files with `git add`. Read only commands such as `git status`, `git diff` and `git log` are fine.

When a change is finished, tell me what you changed and suggest a commit message. I will review the diff and commit it.

This is not about trust. Every commit in this repo is something I need to be able to explain in an interview.

## Conventions

- Conventional Commits, lowercase type, imperative mood. `feat: add lichess game source`
- Flyway owns the schema. `spring.jpa.hibernate.ddl-auto` stays on `validate`. Never generate DDL from entities.
- Migrations are append only. Never edit a migration that has been applied.
- Records for immutable data carriers, classes for anything with behaviour.
- Constructor injection, no field injection.
- No Lombok.
- Secrets come from environment variables, never from committed files.

## Design rules that matter here

**Stream, never collect.** Game histories can be tens of thousands of games. Anything that reads a full response into a String or a List is a bug, even if it passes the test with `max=20`.

**Deduplicate by FEN.** Evaluations are stored once per unique position and depth, not once per occurrence. This is the largest single saving in the system.

**One long lived Stockfish process per core.** Never spawn a process per position.

**Evaluation requests do not block.** Cached positions answer immediately, uncached ones are enqueued and return a job reference.

**Virtual threads are for orchestration, not throughput.** Stockfish is CPU bound and the real ceiling is core count. Do not describe them as making analysis faster.

## Upstream etiquette

Both APIs are free and donation funded.

- Descriptive User-Agent with contact details on every outbound request
- Lichess returns 429 without documented limits, back off a full 60 seconds
- Chess.com allows serial requests only, no parallel fanout on import
- Cache Chess.com responses by ETag and revalidate with If-None-Match

## Current state

Milestone 1. Fetching games from Lichess and counting them. No parsing, no persistence, no engine yet.

Do not build ahead of the current milestone. The roadmap in `README.md` is the order of work.

## How I want you to work with me

This is a portfolio project for an apprenticeship application, so I need to understand and be able to defend every line in it.

- Explain the approach before writing a large amount of code
- Prefer showing me the pattern for one case over generating all cases
- Tell me when you think a decision is wrong rather than implementing it silently
- Small, reviewable changes over large ones