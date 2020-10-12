package soccerpredictions;

import org.telegram.telegrambots.ApiContextInitializer;
import soccerpredictions.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({ApplicationProperties.class})

@SpringBootApplication
public class SoccerPredictionsApplication {

	public static void main(String[] args) {
		ApiContextInitializer.init();
		SpringApplication.run(SoccerPredictionsApplication.class, args);
	}

}
