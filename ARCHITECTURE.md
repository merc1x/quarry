# Architecture

## System context

quarry is one of three services that together make up the chess training platform. This document records where the boundaries are and why they sit there.

```
                    Lichess API        Chess.com API
                          |                  |
                          +--------+---------+
                                   |
                            +--------------+
                            |    quarry    |   Java, Spring Boot, PostgreSQL
                            |  fetch,parse |
                            |  evaluate    |
                            +--------------+
                                   |
                        raw games, evaluations
                                   |
                                   v
                            +--------------+
                            |   deviate    |   TypeScript, PostgreSQL
                            |   analyse    |
                            +--------------+
                                   ^  |
                     repertoire tree|  | reports
                                   |  v
                            +--------------+
                            |  MoveForge   |   Next.js, PostgreSQL
                            |   product    |
                            +--------------+
```

## Responsibilities

### quarry, the data layer

Owns everything about acquiring and computing.

- Lichess and Chess.com clients behind a common `GameSource` interface
- PGN parsing, turning each game into an ordered sequence of FEN positions
- The Stockfish pool and the evaluation cache
- Its own PostgreSQL instance holding `games`, `positions` and `evaluations`
- Idempotent imports that resume rather than restart after a failure

quarry knows nothing about users, repertoires or training. If the word "repertoire" appears anywhere in this codebase, a boundary has been crossed.

### deviate, the analysis layer

Owns everything that requires chess understanding.

- Matching a game against a repertoire tree to find the deviation ply
- Distinguishing the two cases, own deviation versus opponent deviation
- Judging whether a deviation was actually bad, using evaluations from quarry
- Opponent profiles and preparation reports
- Its own database, holding analysis results only, never raw games

deviate never talks to Lichess or Chess.com directly.

### MoveForge, the product

Owns everything the user touches.

- Accounts and authentication
- Repertoire authoring, and therefore the source of truth for the tree
- Drills, spaced repetition, progress tracking
- Presentation of the reports deviate produces

## Dependency rules

These three rules keep the system from collapsing into a monolith.

**1. Arrows point one way.** quarry depends on nobody. deviate depends on quarry and MoveForge. MoveForge depends on deviate. Nothing points back. This is what makes quarry reusable for the C++ engine, which knows nothing about MoveForge, and for any future consumer in the same position.

**2. Raw games have exactly one owner.** They live in quarry. The moment deviate starts keeping its own copies, there are two truths and a synchronisation problem that will never fully be solved.

**3. The repertoire tree has exactly one owner.** It lives in MoveForge and is only edited there. deviate reads it and never writes it.

## Key decisions

### Both game sources sit behind one interface

Lichess streams an entire history in a single request. Chess.com requires iterating monthly archives. Both are reduced to a stream of raw PGN strings by the `GameSource` interface, so the rest of the pipeline is unaware of which source a game came from.

Adding a third source later should mean writing one class and changing nothing else.

### Positions are deduplicated by FEN

Opening positions repeat across hundreds of games. Evaluations are stored once per unique FEN and depth, not once per occurrence. This is the single largest saving in the system and it is why the evaluation cache exists at all rather than evaluating on demand.

### Stockfish runs as a pool of long lived processes

Roughly one process per core, started once and fed positions over UCI. Spawning a process per position would spend more time on startup than on search.

Virtual threads are used for orchestration, not for throughput. Stockfish is CPU bound, so the real ceiling is core count. What virtual threads buy is that each analysis task can block on reading engine output in plain sequential code, and that structured concurrency can fan a game out into position tasks and cancel the whole scope cleanly on timeout.

### Evaluation requests are asynchronous by default

A preparation report may touch positions that have never been evaluated. If the endpoint blocked until Stockfish finished, deviate would hang for minutes. Instead, cached positions are answered immediately and uncached ones are enqueued, with a job reference returned.

### Lichess evaluations are used where available

Lichess exposes server side analysis for a large share of games. Those evaluations are ingested directly and only the remainder is sent to Stockfish. Chess.com has no equivalent, so games from that source always require local evaluation.

### deviate pulls the repertoire rather than MoveForge pushing it

A repertoire changes rarely, so polling with an ETag or version number is cheap. Pushing would require MoveForge to know that deviate exists, which reverses the dependency arrow and breaks rule 1.

### Opponent scouting on Lichess uses the upstream explorer

Lichess already provides per player opening statistics through the opening explorer's player endpoint. Reimplementing that by importing an opponent's full history would be slower and would put unnecessary load on a donation funded service. Chess.com offers no equivalent, so opponent profiles for that platform are built from imported games.

## Data model

Deliberately small at this stage. It grows as the milestones require, not before.

```
games         id, source, external_id, played_at, white, black,
              result, eco, time_control, pgn

positions     id, game_id, ply, fen, san, uci

evaluations   fen, depth, centipawns, mate_in, best_move, engine_version
```

A unique index on `(source, external_id)` in `games` is what makes imports idempotent. A unique index on `(fen, depth)` in `evaluations` is what makes deduplication work. Those two indexes carry most of the design.

Storing the played move alongside the position, rather than only the position, costs almost nothing and leaves the door open for a future consumer that needs training samples in the shape of position, evaluation and played move. No such consumer is planned at present.

## Non goals

- quarry does not serve a user interface
- quarry does not store per user state or preferences
- quarry does not decide what a good or bad move is, it only reports what the engine says
- quarry is not a general chess API for third parties, it serves three known consumers
