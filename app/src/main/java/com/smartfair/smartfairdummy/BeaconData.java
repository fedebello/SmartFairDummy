package com.smartfair.smartfairdummy;

import java.nio.DoubleBuffer;
import java.util.List;

/**
 * Created by Fede on 10/29/16.
 */

public class BeaconData {

    private String id;
    private List<Double> measures;
    private Double avg;

    public String getId() {
        return id;
    }

    public List<Double> getMeasures() {
        return measures;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMeasures(List<Double> measures) {
        this.measures = measures;
    }

    public Double getAvg() {
        return avg;
    }

    public void setAvg(Double avg) {
        this.avg = avg;
    }

    public void addMeasure(Double measure) {
        measures.add(measure);

        if (measures.size() > 10)
            measures.remove(0);

        updateAvg();
    }

    public void updateAvg() {
        double avg = getAverageMeasure();
        setAvg(avg);
    }

    public Double getAverageMeasure() {
        if (measures.size() <= 0)
            return Double.valueOf(0);

        // ver si esto no se borra cuando se pase de 10
        double avg = measures.get(0);

        for (int i=1; i < measures.size(); i++) {
            avg += measures.get(i);
        }

        return avg / measures.size();

    }
}
