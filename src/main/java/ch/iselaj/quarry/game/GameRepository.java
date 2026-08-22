package ch.iselaj.quarry.game;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {

    boolean existsBySourceAndExternalId(String source, String externalId);
}