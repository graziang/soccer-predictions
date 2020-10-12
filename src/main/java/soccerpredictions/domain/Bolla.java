package soccerpredictions.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class Bolla {
    public Double quota;
    public Double probability;
    public List<Prediction> partite = new ArrayList<>();
}
