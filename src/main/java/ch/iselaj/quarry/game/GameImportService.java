package ch.iselaj.quarry.game;

import ch.iselaj.quarry.source.GameSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GameImportService {

    private static final Logger log = LoggerFactory.getLogger(GameImportService.class);

    private final GameSource source;
    private final GameRepository repository;

    public GameImportService(GameSource source, GameRepository repository) {
        this.source = source;
        this.repository = repository;
    }

    public ImportResult importGames(String username, int max) {
        int imported = 0;
        int skipped = 0;

        try (var games = source.fetchGames(username, max)) {
            for (var raw : (Iterable<ch.iselaj.quarry.source.RawGame>) games::iterator) {
                if (repository.existsBySourceAndExternalId(source.name(), raw.externalId())) {
                    skipped++;
                    continue;
                }
                repository.save(new Game(source.name(), raw.externalId(), raw.payload()));
                imported++;
            }
        }

        log.info("import finished for {}, imported {}, skipped {}", username, imported, skipped);
        return new ImportResult(imported, skipped);
    }

    public record ImportResult(int imported, int skipped) {
    }
}