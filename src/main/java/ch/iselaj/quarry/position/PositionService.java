package ch.iselaj.quarry.position;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PositionService {

    private static final Logger log = LoggerFactory.getLogger(PositionService.class);

    private final PgnParser parser;
    private final PositionRepository repository;

    public PositionService(PgnParser parser, PositionRepository repository) {
        this.parser = parser;
        this.repository = repository;
    }

    @Transactional
    public int extract(Long gameId, String pgn) {
        if (repository.existsByGameId(gameId)) {
            return 0;
        }

        var positions = parser.parse(pgn).stream()
                .map(m -> new Position(gameId, m.ply(), m.fen(), m.san(), m.uci()))
                .toList();

        repository.saveAll(positions);
        return positions.size();
    }
}