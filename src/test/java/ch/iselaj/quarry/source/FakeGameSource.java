package ch.iselaj.quarry.source;

import java.util.List;
import java.util.stream.Stream;

public class FakeGameSource implements GameSource {

    private static final String PGN = """
            [Event "Test"]
            [White "A"]
            [Black "B"]
            [Result "1-0"]

            1. e4 e5 2. Nf3 Nc6 3. Bb5 1-0
            """;

    @Override
    public String name() {
        return "fake";
    }

    @Override
    public Stream<RawGame> fetchGames(String username, int max) {
        return List.of(
                new RawGame("game-1", PGN),
                new RawGame("game-2", PGN)
        ).stream();
    }
}