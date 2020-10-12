package soccerpredictions.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = true)
public class ApplicationProperties {

    private String xRapidapiHost;

    private String xRapidapiKey;

    private String xRapidapiUrl;

    private List<String> cases;

    private List<String> caseClassic;
    private List<String> caseDoppiaChance;
    private List<String> caseGoal;

}
