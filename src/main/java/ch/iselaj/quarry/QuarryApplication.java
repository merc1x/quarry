package ch.iselaj.quarry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import ch.iselaj.quarry.source.GameSource;

@SpringBootApplication
public class QuarryApplication {

	private static final Logger log = LoggerFactory.getLogger(QuarryApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(QuarryApplication.class, args);
	}

	@Bean
	ApplicationRunner smokeTest(GameSource source) {
		return args -> {
			try (var games = source.fetchGames("happysettler", 20)) {
    log.info("fetched {} games", games.count());

			}
		};
	}
}
