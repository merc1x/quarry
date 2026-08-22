package ch.iselaj.quarry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import ch.iselaj.quarry.game.GameImportService;
import ch.iselaj.quarry.source.GameSource;

@SpringBootApplication
public class QuarryApplication {

	private static final Logger log = LoggerFactory.getLogger(QuarryApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(QuarryApplication.class, args);
	}


	@Bean
		ApplicationRunner smokeTest(GameImportService importService) {
    		return args -> importService.importGames("HappySettler", 20);
		}
}
