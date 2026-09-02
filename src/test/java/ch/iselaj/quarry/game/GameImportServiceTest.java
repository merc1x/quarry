package ch.iselaj.quarry.game;

import ch.iselaj.quarry.IntegrationTest;
import ch.iselaj.quarry.position.PositionRepository;
import ch.iselaj.quarry.position.PositionService;
import ch.iselaj.quarry.source.FakeGameSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class GameImportServiceTest extends IntegrationTest {

    @Autowired
    GameRepository games;

    @Autowired
    PositionRepository positions;

    @Autowired
    PositionService positionService;

    GameImportService importService;

    @BeforeEach
    void setUp() {
        positions.deleteAll();
        games.deleteAll();
        importService = new GameImportService(new FakeGameSource(), games, positionService);
    }

    @Test
    void importsGamesAndExtractsPositions() {
        var result = importService.importGames("anyone", 10);

        assertThat(result.imported()).isEqualTo(2);
        assertThat(result.skipped()).isZero();
        assertThat(games.count()).isEqualTo(2);
        assertThat(positions.count()).isEqualTo(10);
    }

    @Test
    void skipsGamesThatAreAlreadyStored() {
        importService.importGames("anyone", 10);
        var second = importService.importGames("anyone", 10);

        assertThat(second.imported()).isZero();
        assertThat(second.skipped()).isEqualTo(2);
        assertThat(games.count()).isEqualTo(2);
    }
}