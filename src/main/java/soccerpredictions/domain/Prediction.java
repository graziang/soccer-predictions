package soccerpredictions.domain;

import lombok.Data;

import java.util.Map;

@Data
public class Prediction {
    public String id;
    public String federation;
    public Map<String, Object> odds;
    public String away_team;
    public String home_team;
    public String competition_name;
    public String competition_cluster;
    public String start_date;
    public String prediction;
    public Map<String, Object> probabilities;
    public Double probability;
    public Double quote;
    public String predictionType;
}
