package soccerpredictions.component;

import org.springframework.web.client.RestTemplate;
import soccerpredictions.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import soccerpredictions.domain.Data;
import soccerpredictions.domain.Prediction;

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
        String todayAsString = df.format(today);
        url += "&iso_date="+todayAsString;

        try {
           Data data = restTemplate.getForObject(url, Data.class);
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

}
