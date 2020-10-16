package soccerpredictions.domain;

import lombok.Data;

import java.util.Map;
import java.util.Objects;

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
    public boolean is_expired;
    public Map<String, Object> probabilities;
    public Double probability;
    public Double quote;
    public String predictionType;
    public String result;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prediction that = (Prediction) o;
        return Objects.equals(id, that.id) ||
                (Objects.equals(away_team, that.away_team) &&
                Objects.equals(home_team, that.home_team) &&
                Objects.equals(start_date, that.start_date));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, away_team, home_team, start_date);
    }
}
