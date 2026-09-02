package ch.iselaj.quarry.position;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PgnParserTest {

    private static final String STANDARD_GAME = """
            [Event "Test"]
            [White "A"]
            [Black "B"]
            [Result "1-0"]

            1. e4 e5 2. Nf3 Nc6 3. Bb5 1-0
            """;

    private final PgnParser parser = new PgnParser();

    @Test
    void parsesEveryHalfMove() {
        var moves = parser.parse(STANDARD_GAME);

        assertThat(moves).hasSize(5);
        assertThat(moves.getFirst().san()).isEqualTo("e4");
        assertThat(moves.getFirst().ply()).isZero();
        assertThat(moves.getLast().san()).isEqualTo("Bb5");
    }

    @Test
    void startsFromTheStandardPosition() {
        var first = parser.parse(STANDARD_GAME).getFirst();

        assertThat(first.fen()).startsWith("rnbqkbnr/pppppppp/8/8/4P3");
    }

    @Test
    void rejectsMalformedPgn() {
        assertThatThrownBy(() -> parser.parse("this is not a pgn"))
                .isInstanceOf(RuntimeException.class);
    }
}