package it.trainradar.core;

import java.io.Serializable;
import java.time.LocalTime;

import it.trainradar.manager.StationManager;

public class Stop implements Serializable {
    private String station;
    private LocalTime arrivalTime;
    private LocalTime departureTime;

    public Station getStation() {
        return StationManager.getStation(station);
    }

    public LocalTime getArrival() {
        return arrivalTime != null ? arrivalTime : departureTime;
    }

    public LocalTime getDeparture() {
        return departureTime != null ? departureTime : arrivalTime;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Stop && station.equals(((Stop) object).station);
    }
}