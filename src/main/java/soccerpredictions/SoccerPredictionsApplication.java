package soccerpredictions;

import org.telegram.telegrambots.ApiContextInitializer;
import soccerpredictions.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EnableConfigurationProperties({ApplicationProperties.class})

@SpringBootApplication
public class SoccerPredictionsApplication {

	public static void main(String[] args) {
		ApiContextInitializer.init();
		SpringApplication.run(SoccerPredictionsApplication.class, args);
	}


	/*public static void main(String[] args) {
		List<int[]> selection = CombinationUtils.generate(50, 5);

		for (int[] sel : selection) {
			String combination = "[";
			for (int i: sel) {
				combination += i +",";
			}
			combination += "]";
			System.out.println(combination);
		}


	}*/


}
