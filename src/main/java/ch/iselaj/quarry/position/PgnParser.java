package ch.iselaj.quarry.position;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class PgnParser {

    /**
     * Replays a single PGN and returns one entry per half-move.
     * Games that start from a set-up position are replayed from that
     * position rather than from the standard starting position.
     */
    public List<ParsedMove> parse(String pgn) {
        var game = readSingleGame(pgn);
        var board = new Board();

        var startFen = game.getFen();
        if (startFen != null && !startFen.isBlank()) {
            board.loadFromFen(startFen);
        }

        var moves = new ArrayList<ParsedMove>();
        int ply = 0;

                for (var move : game.getHalfMoves()) {
            var san = move.getSan();
            var uci = move.toString();
            board.doMove(move);
            moves.add(new ParsedMove(ply++, board.getFen(), san, uci));
        }

        if (moves.isEmpty()) {
            throw new IllegalArgumentException("PGN contained no moves");
        }

        return moves;
    }

    private Game readSingleGame(String pgn) {
        try {
            var temp = Files.createTempFile("quarry-", ".pgn");
            try {
                Files.writeString(temp, pgn);
                var holder = new PgnHolder(temp.toString());
                holder.loadPgn();
                if (holder.getGames().isEmpty()) {
                    throw new IllegalArgumentException("PGN contained no game");
                }
                var game = holder.getGames().getFirst();
                game.loadMoveText();
                return game;
            } finally {
                Files.deleteIfExists(temp);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read PGN", e);
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse PGN", e);
        }
    }

    public record ParsedMove(int ply, String fen, String san, String uci) {
    }
}