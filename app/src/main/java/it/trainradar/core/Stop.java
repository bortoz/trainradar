package it.trainradar.core;

import java.io.Serializable;
import java.time.LocalTime;

public class Stop implements Serializable {
    private String id;
    private LocalTime arrival;
    private LocalTime departure;

    public String getId() {
        return id;
    }

    public LocalTime getArrival() {
        return arrival;
    }

    public LocalTime getDeparture() {
        return departure;
    }
}