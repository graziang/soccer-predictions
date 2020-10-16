package soccerpredictions.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.corba.se.spi.ior.ObjectKey;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.client.RestTemplate;
import soccerpredictions.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import soccerpredictions.domain.Data;
import soccerpredictions.domain.Prediction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class PredictionAPIComponent {

    private final Logger log = LoggerFactory.getLogger(PredictionAPIComponent.class);

    private final String URL_PREDICTIONS = "/api/v2/predictions";

    @Autowired
    ApplicationProperties applicationProperties;

    @Autowired
    RestTemplate restTemplate;


    public List<Prediction> getAllPredictions(String date, String market, String federation) {

        String url = this.applicationProperties.getXRapidapiUrl() + URL_PREDICTIONS;

        if(market != null) {
            url += "?market=" + market;
        }

         //url += "&federation=UEFA";

        String pattern = "yyyy-MM-dd";
        DateFormat df = new SimpleDateFormat(pattern);
        Date today = Calendar.getInstance().getTime();
        String stringDate = df.format(today);
        if(date != null) {
            stringDate = date;
        }
        url += "&iso_date="+stringDate;

        String sha256hex = DigestUtils.sha256Hex(url);

        Path filePath = Paths.get(this.applicationProperties.getCachePath(), sha256hex);

        if(Files.exists(filePath)) {
            try {
                String jsonString = readFile(filePath);
                Data data = new ObjectMapper().readValue(jsonString, Data.class);
                return data.getData();
            } catch (IOException e) {
                log.error(e.toString());
            }
        }

        try {
           Data data = restTemplate.getForObject(url, Data.class);
            Files.createDirectories(Paths.get(this.applicationProperties.getCachePath()));
            Files.createFile(filePath);
            Files.write(filePath, new ObjectMapper().writeValueAsString(data).getBytes());
           return data.getData();
        }
        catch (Exception e) {
            log.error(e.toString());
        }
        return Collections.emptyList();
    }

    public List<Prediction> getAllPredictions() {
        return getAllPredictions(null, null ,null);

    }


    static String readFile(Path path) throws IOException {
        byte[] encoded = Files.readAllBytes(path);
        return new String(encoded);
    }
}
