package ch.iselaj.quarry.source;

import java.util.stream.Stream;

public interface GameSource {

    String name();

    /**
     * Streams a player's games. The returned stream is backed by an open
     * HTTP connection and must be closed by the caller.
     */
    Stream<RawGame> fetchGames(String username, int max);
}