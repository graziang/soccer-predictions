package soccerpredictions.service;


import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import soccerpredictions.CombinationUtils;
import soccerpredictions.component.PredictionAPIComponent;
import soccerpredictions.config.ApplicationProperties;
import soccerpredictions.domain.Bolla;
import soccerpredictions.domain.Prediction;

import javax.annotation.PostConstruct;
import javax.ws.rs.BadRequestException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PredictionService {

    private final Logger log = LoggerFactory.getLogger(PredictionService.class);

    @Autowired
    PredictionAPIComponent predictionAPIComponent;

    @Autowired
    ApplicationProperties applicationProperties;

    public List<Prediction> getPredictions(String type, String yes, String data){

        String predictionType = this.getPredictionType(type);
        List<Prediction> predictions = this.predictionAPIComponent.getAllPredictions(data, predictionType, null);
        List<Prediction> mapped = predictions.stream().map((p) ->
        {
            Map<String, Object> probabilities = p.getProbabilities();
            p.setPredictionType(type);
            if(probabilities.keySet().contains(yes)) {
                p.setProbability(Double.parseDouble(probabilities.get(yes).toString()));
                if(p.getOdds().get(yes) != null) {
                    p.setQuote(Double.parseDouble(p.getOdds().get(yes).toString()));
                }
                else {
                    p.setQuote(1.0);
                }

            }
            else if(probabilities.keySet().contains(type)) {
                p.setProbability(Double.parseDouble(probabilities.get(type).toString()));
                if(p.getOdds().get(type) != null) {
                    p.setQuote(Double.parseDouble(p.getOdds().get(type).toString()));
                }
                else {
                    p.setQuote(1.0);
                }
            }
            return p;
        }).collect(Collectors.toList());

       // mapped = mapped.stream().filter(prediction -> !prediction.is_expired()).collect(Collectors.toList());

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
            predictions.addAll(this.getPredictions(type, this.getYesNo(type), null));
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

        return bestRaddoppio(cases, null);
    }

    public String bestRaddoppioAll(){
        return bestRaddoppio(applicationProperties.getCases(), null);
    }

    private List<Bolla> bestBolla(List<String> cases, double minQuota, int partite, double minProb, double minProbBolla, List<String> date){
        List<Prediction> predictions = new ArrayList<>();
        for (String data: date) {
            for (String type : cases) {
                predictions.addAll(this.getPredictions(type, this.getYesNo(type), data));
            }
        }

        predictions = predictions.stream().filter(prediction -> prediction.getProbability() > minProb).collect(Collectors.toList());
       /* predictions = predictions.stream().filter(prediction -> {
            if(prediction.getPredictionType().equals("1X")) {
                return prediction.getProbability() > 0.8;
            }
            return true;
        }).collect(Collectors.toList());*/
        //predictions = predictions.stream().filter(prediction -> prediction.getFederation().toUpperCase().equals("UEFA")).collect(Collectors.toList());
        //predictions = predictions.stream().filter(prediction -> prediction.getCompetition_cluster().toUpperCase().startsWith("ITA")).collect(Collectors.toList());

        List<int[]> combinations = CombinationUtils.generate(predictions.size(), partite);

        List<Bolla> bolle = new ArrayList<>();
        for (int[] combination: combinations) {
            Bolla bolla = new Bolla();
            for (int i: combination) {
                bolla.getPartite().add(predictions.get(i));
            }

            double quota = 1;
            double probability = 1;

            for (Prediction prediction :bolla.getPartite()) {
                quota *= prediction.getQuote();
                probability *= prediction.getProbability();

            }

            if(quota > minQuota && minProbBolla < probability) {
                bolla.setProbability(probability);
                bolla.setQuota(quota);
                bolle.add(bolla);
            }
        }

        return bolle;
    }



    private List<Bolla> bestRaddoppioList(List<String> cases, List<String> date) {
        List<Bolla> bolle = this.bestBolla(cases, 2, 2, 0.5, 0.2, date);

        bolle = bolle.stream().sorted((b1, b2) -> {
            if (b1.getProbability() < b2.getProbability())
                return 1;
            if (b1.getProbability() > b2.getProbability())
                return -1;
            else
                return 0;
        }).collect(Collectors.toList());


        if (bolle.size() > 1) {
            bolle = bolle.subList(0, 5);
        }

        return bolle;
    }
    private String bestRaddoppio(List<String> cases, String data){

        List<Bolla> bolle = this.bestRaddoppioList(cases, Collections.singletonList(data));

        int totPartite = bolle.size();
        StringBuilder message = new StringBuilder();
        for (Bolla bolla: bolle) {
            String raddoppio = String.format("RADDOPPIO: QUOTA: %.2f, PROBABILITA': %.2f PARTITE: %d\n", bolla.getQuota(), bolla.getProbability(), totPartite/cases.size());
            message.append(raddoppio);
            message.append(this.getPartiteMessage(bolla.getPartite()));
            message.append("\n");
        }
        return message.toString();

    }



    public String getMessage(String type) {
        List<Prediction> predictions = this.getPredictions(type, getYesNo(type), null);

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
            message.append("ESITO: " + prediction.getPredictionType() + "\n\n");

        }
        return message.toString();
    }


    //@PostConstruct
    public void createPronostici() {
        List<String[]> dataLinesGlobal = new ArrayList<>();
        List<String[]> dataLines = new ArrayList<>();

        LocalDate fromDate = LocalDate.of(2019, 1,1);
        LocalDate startDate = fromDate;
        LocalDate toDate = LocalDate.of(2019, 12,30);


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        int vinte = 0;
        int perse = 0;
        int tot = 0;

        int vinteGlobal = 0;
        int perseGlobal = 0;
        int totGlobal = 0;
        int count = 0;
        double quotaMediaVincenti = 0;
        while (startDate.isBefore(toDate)) {


            if(startDate.getDayOfWeek().getValue() != 4) {
               // startDate = startDate.plusDays(1);
               // continue;
            }

            int giorni = 1;
            List<String> date = new ArrayList<>();
            String data = startDate.format(formatter);
            date.add(data);
            for(int i = 1; i < giorni; i++) {
                startDate = startDate.plusDays(1);
                data = startDate.format(formatter);
                date.add(data);
            }


            List<Bolla> bolle = this.bestRaddoppioList(this.applicationProperties.getCases(), date);

            tot+=bolle.size();
            totGlobal+=bolle.size();
            for (Bolla bolla : bolle) {

                boolean no = true;
                String partite = "";
                Boolean vinta = true;
                for (Prediction p: bolla.getPartite()) {
                    partite = partite + String.format("%s - %s",p.getHome_team(), p.getAway_team())  +", " +p.getResult()+ ", " + p.getPredictionType() + ";";

                    try {
                        vinta = vinta && vinta(p);
                    } catch (IOException e) {
                        log.info("da buttare");
                        no = false;
                    }
                }

                if(!no) {
                    tot--;
                    totGlobal--;
                    continue;
                }
                dataLinesGlobal.add(new String[] { data, partite,  bolla.getQuota().toString(), bolla.getProbability().toString(),vinta.toString().toUpperCase()});
                dataLines.add(new String[] { data, partite,  bolla.getQuota().toString(), bolla.getProbability().toString(),vinta.toString().toUpperCase()});

                if(vinta) {
                    quotaMediaVincenti += bolla.getQuota();
                    vinte++;
                    vinteGlobal++;
                }
                else {
                    perse++;
                    perseGlobal++;
                }

            }
            int currentMonth = startDate.getMonthValue();
            startDate = startDate.plusDays(1);
            log.info("days: " + count);
            count++;


            if(currentMonth < startDate.getMonthValue()) {
                dataLinesGlobal.add(new String[]{"TOT:" + tot, "VINTE:" + vinte, "PERSE:" + perse, "PERCENTUALE VINTE: " + 100.0 / tot * vinte});
                dataLines.add(new String[]{"TOT:" + tot, "VINTE:" + vinte, "PERSE:" + perse, "PERCENTUALE VINTE: " + 100.0 / tot * vinte});
                try {
                    String name = "analytics_" + currentMonth + "-" + startDate.getYear() + ".csv";
                    createCsv(dataLines, name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tot = 0;
                vinte = 0;
                perse = 0;
                dataLines = new ArrayList<>();

            }

        }


        dataLines.add(new String[]{"TOT:" + tot, "VINTE:" + vinte, "PERSE:" + perse, "PERCENTUALE VINTE: " + 100.0 / tot * vinte});
        dataLinesGlobal.add(new String[]{"TOT:" + tot, "VINTE:" + vinte, "PERSE:" + perse, "PERCENTUALE VINTE: " + 100.0 / tot * vinte});
        try {
            String name = "analytics_" + startDate.getMonthValue() + "-" + startDate.getYear() + ".csv";
            createCsv(dataLines, name);
        } catch (IOException e) {
            e.printStackTrace();
        }



        String[] finalList = new String[]{"TOT:" + totGlobal, "VINTE:" + vinteGlobal, "PERSE:" + perseGlobal, "PERCENTUALE VINTE: " + 100.0 / totGlobal * vinteGlobal};
        log.info(String.join(", ", finalList));
        log.info("quota media vincenti = " + quotaMediaVincenti/vinteGlobal);
        dataLinesGlobal.add(finalList);
        try {
            String name = "analytics_" + fromDate.toString() + "_" + toDate.toString() + ".csv";
            createCsv(dataLinesGlobal, name);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public boolean vinta(Prediction prediction) throws IOException {
        if(!prediction.getResult().contains("-")){
            throw new IOException();
        }

        int homeGoal = Integer.parseInt(prediction.getResult().split(" - ")[0].trim());
        int awayGoal = Integer.parseInt(prediction.getResult().split(" - ")[1].trim());

        String predictionType = prediction.getPredictionType();
        switch (predictionType) {
            case "1":
                return homeGoal > awayGoal;
            case "2":
                return homeGoal < awayGoal;
            case "X":
                return homeGoal == awayGoal;
            case "1X":
                return homeGoal >= awayGoal;
            case "X2":
                return homeGoal <= awayGoal;
            case "GOAL":
                return homeGoal == 0 || awayGoal == 0;
            case "NOGOAL":
                return homeGoal > 0 && awayGoal > 0;
            case "OVER25":
                return homeGoal + awayGoal > 2;
            case "UNDER25":
                return homeGoal + awayGoal < 3;
            case "OVER35":
                return homeGoal + awayGoal > 3;
            case "UNDER35":
                return homeGoal + awayGoal < 4;
            case "12":
                return homeGoal != awayGoal;
            default:
                return false;
        }

    }

    public String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public void createCsv(List<String[]> dataLines, String name) throws IOException {
        Files.createDirectories(Paths.get("./statistiche"));
        File csvOutputFile = new File("./statistiche/" + name);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            dataLines.stream()
                    .map(this::convertToCSV)
                    .forEach(pw::println);
        }
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
            case "OVER35":
                predictionType= "over_35";
                break;
            case "UNDER35":
                predictionType= "over_35";
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
            case "OVER35":
                predictionType= "yes";
                break;
            case "UNDER35":
                predictionType= "no";
                break;
        }

        return predictionType;
    }
}
