package soccerpredictions.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import soccerpredictions.component.PredictionAPIComponent;
import soccerpredictions.config.ApplicationProperties;
import soccerpredictions.domain.Bolla;
import soccerpredictions.domain.Prediction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PredictionService {

    @Autowired
    PredictionAPIComponent predictionAPIComponent;

    @Autowired
    ApplicationProperties applicationProperties;

    public List<Prediction> getPredictions(String type, String yes){

        String predictionType = this.getPredictionType(type);
        List<Prediction> predictions = this.predictionAPIComponent.getAllPredictions(null, predictionType, null);
        List<Prediction> mapped = predictions.stream().map((p) ->
        {
            Map<String, Object> probabilities = p.getProbabilities();
            p.setPredictionType(type);
            if(probabilities.keySet().contains(yes)) {
                p.setProbability(Double.parseDouble(probabilities.get(yes).toString()));
                if(p.getOdds().get(yes) != null) {
                    p.setQuote(Double.parseDouble(p.getOdds().get(yes).toString()));
                }

            }
            else if(probabilities.keySet().contains(type)) {
                p.setProbability(Double.parseDouble(probabilities.get(type).toString()));
                if(p.getOdds().get(type) != null) {
                    p.setQuote(Double.parseDouble(p.getOdds().get(type).toString()));
                }
            }
            return p;
        }).collect(Collectors.toList());

        return sortedPredictions(mapped);
    }

    public List<Prediction> sortedPredictions (List<Prediction> predictions) {

        List<Prediction> sortedPredictions = predictions.stream().sorted((p1, p2) ->
                p1.getProbability() > p2.getProbability() ? -1 : 1).collect(Collectors.toList());
        return sortedPredictions;
    }


    public String bestOf(){

        List<Prediction> predictions = new ArrayList<>();

        for (String type: applicationProperties.getCases()) {
            predictions.addAll(this.getPredictions(type, this.getYesNo(type)));
        }
        List<Prediction> sortedPredictions = this.sortedPredictions(predictions);
        return getPartiteMessage(sortedPredictions);
    }


    public String bestRaddoppioSub(List<String> list){
        List<String> cases = new ArrayList<>();

        if(list.contains("c")){
            cases.addAll(applicationProperties.getCaseClassic());
        }
        if(list.contains("d")){
            cases.addAll(applicationProperties.getCaseDoppiaChance());
        }
        if(list.contains("g")){
            cases.addAll(applicationProperties.getCaseGoal());
        }

        return bestRaddoppio(cases);
    }

    public String bestRaddoppioAll(){
        return bestRaddoppio(applicationProperties.getCases());
    }

    private String bestRaddoppio(List<String> cases){
        List<Prediction> predictions = new ArrayList<>();
        for (String type: cases) {
            predictions.addAll(this.getPredictions(type, this.getYesNo(type)));
        }

        List<Bolla> bolle = new ArrayList<>();
        for (Prediction prediction1: predictions) {
            for (Prediction prediction2: predictions) {
                if((!prediction1.equals(prediction2) || (prediction1.equals(prediction2) && !prediction1.getPredictionType().equals(prediction2.getPredictionType()))) && prediction1.getQuote() != null && prediction2.getQuote() != null) {
                    if(prediction1.getQuote() * prediction2.getQuote() > 2) {
                          if(prediction1.getProbability() > 0.5 && prediction2.getProbability() > 0.5) {
                        Bolla bolla = new Bolla();
                        bolla.setQuota(prediction1.getQuote() * prediction2.getQuote());
                        bolla.setProbability(prediction1.getProbability() * prediction2.getProbability());
                        bolla.getPartite().add(prediction1);
                        bolla.getPartite().add(prediction2);
                        if(bolle.stream().noneMatch(bolla1 -> bolla1.getPartite().get(0).getId().equals(prediction2.getId()) && bolla1.getPartite().get(1).getId().equals(prediction1.getId()))){
                            {
                                bolle.add(bolla);
                            }

                              }
                        }
                    }
                }

            }
        }

        bolle = bolle.stream().sorted((b1,b2) -> {
            return b1.getProbability() < b2.getProbability() ? 1 : -1;
        }).collect(Collectors.toList());

        int totPartite = predictions.size();
        if(bolle.size() > 5) {
            bolle = bolle.subList(0, 5);
        }

        StringBuilder message = new StringBuilder();
        for (Bolla bolla: bolle) {
            String raddoppio = String.format("RADDOPPIO: QUOTA: %.2f, PROBABILITA': %.2f PARTITE: %d\n", bolla.getQuota(), bolla.getProbability(), totPartite);
            message.append(raddoppio);
            message.append(this.getPartiteMessage(bolla.getPartite()));
            message.append("\n");
        }
        return message.toString();

    }



    public String getMessage(String type) {
        List<Prediction> predictions = this.getPredictions(type, getYesNo(type));

        StringBuilder message = new StringBuilder(String.format("Top 10 partite pronostici con esito " + type + ", TOT PARTITE %d:\n\n", predictions.size()));

        message.append(this.getPartiteMessage(predictions));
        message.append("\n\n");
        return message.toString();

    }

    public String getPartiteMessage(List<Prediction> predictions) {
        StringBuilder message = new StringBuilder();

        if(predictions.size() > 10) {
            predictions = predictions.subList(0, 10);
        }
        for (Prediction prediction: predictions)  {


            double probabilita = prediction.getProbability();
            Object quota = prediction.getQuote();
            String partita = "- " + prediction.getHome_team().toUpperCase() + " - " + prediction.getAway_team().toUpperCase() + " (" + prediction.getCompetition_name() + ", " + prediction.getCompetition_cluster() + ")";
            message.append(partita).append("\n");
            message.append("DATA: " + prediction.getStart_date() + "\n");
            String round = String.format("%.2f", probabilita*100);
            message.append("PROBABILITA': " + round + "%\n");
            message.append("QUOTA: " + quota + "\n");
            message.append("ESITO': " + prediction.getPredictionType() + "\n\n");

        }
        return message.toString();
    }

    public String getPredictionType(String type) {

       String predictionType = null;
        switch (type){
            case "1":
                predictionType= "classic";
                break;
            case "2":
                predictionType= "classic";
                break;
            case "X":
                predictionType= "classic";
                break;
            case "1X":
                predictionType= "classic";
                break;
            case "X2":
                predictionType= "classic";
                break;
            case "GOAL":
                predictionType= "btts";
                break;
            case "NOGOAL":
                predictionType= "btts";
                break;
            case "OVER25":
                predictionType= "over_25";
                break;
            case "UNDER25":
                predictionType= "over_25";
                break;
        }

        return predictionType;
    }

    public String getYesNo(String type) {

        String predictionType = null;
        switch (type){
            case "GOAL":
                predictionType= "yes";
                break;
            case "NOGOAL":
                predictionType= "no";
                break;
            case "OVER25":
                predictionType= "yes";
                break;
            case "UNDER25":
                predictionType= "no";
                break;
        }

        return predictionType;
    }
}
