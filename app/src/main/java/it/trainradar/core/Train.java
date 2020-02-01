package it.trainradar.core;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import it.trainradar.manager.StationManager;
import it.trainradar.manager.TimeManager;
import it.trainradar.manager.TrainDelayManager;

public class Train implements Serializable {
    public final static double MAX_RAND_OFFSET = 2e-4;

    private int id;
    private String name;
    private String idDeparture;
    private String idArrival;
    private LocalTime departure;
    private LocalTime arrival;
    private ArrayList<Stop> stops;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIdDeparture() {
        return idDeparture;
    }

    public String getIdArrival() {
        return idArrival;
    }

    public LocalTime getDeparture() {
        return departure;
    }

    public LocalTime getArrival() {
        return arrival;
    }

    public ArrayList<Stop> getStops() {
        return stops;
    }

    public LatLng getPosition(LocalTime time) {
        Integer delay = TrainDelayManager.getDelay(this);
        if (delay != null) {
            time = time.minus(Duration.ofMinutes(delay));
        }
        for (int i = 0; i < stops.size() - 1; i++) {
            if (i > 0 && TimeManager.isTimeOnInterval(time, stops.get(i).getArrival(), stops.get(i).getDeparture())) {
                Station st = StationManager.getStation(stops.get(i).getId());
                Random rng = new Random(id);
                return new LatLng(st.getPosition().latitude + 2 * MAX_RAND_OFFSET * rng.nextDouble() - MAX_RAND_OFFSET,
                        st.getPosition().longitude + 2 * MAX_RAND_OFFSET * rng.nextDouble() - MAX_RAND_OFFSET);
            }
            if (TimeManager.isTimeOnInterval(time, stops.get(i).getDeparture(), stops.get(i + 1).getArrival())) {
                List<Station> path = StationManager.getPath(stops.get(i).getId(), stops.get(i + 1).getId());
                int elapsed = TimeManager.timeDiff(stops.get(i).getDeparture(), time);
                int total = TimeManager.timeDiff(stops.get(i).getDeparture(), stops.get(i + 1).getArrival());
                float dist = 0;
                for (int j = 0; j < path.size() - 1; j++) {
                    dist += path.get(j).getLocation().distanceTo(path.get(j + 1).getLocation());
                }
                float currDist = dist * elapsed / total;
                for (int j = 0; j < path.size() - 1; j++) {
                    float nextDist = path.get(j).getLocation().distanceTo(path.get(j + 1).getLocation());
                    if (currDist < nextDist) {
                        double lat = (path.get(j).getPosition().latitude * (nextDist - currDist) + path.get(j + 1).getPosition().latitude * currDist) / nextDist;
                        double lng = (path.get(j).getPosition().longitude * (nextDist - currDist) + path.get(j + 1).getPosition().longitude * currDist) / nextDist;
                        return new LatLng(lat, lng);
                    }
                    currDist -= nextDist;
                }
                return null;
            }
        }
        return null;
    }

    public Location getLocation(LocalTime time) {
        LatLng pos = getPosition(time);
        if (pos == null) return null;
        Location location = new Location(name);
        location.setLatitude(pos.latitude);
        location.setLongitude(pos.longitude);
        return location;
    }

    @Override
    public boolean equals(Object train) {
        return train instanceof Train && name.equals(((Train) train).name) && idDeparture.equals(((Train) train).idDeparture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, idDeparture);
    }
}
