package it.trainradar.core;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import it.trainradar.manager.StationManager;
import it.trainradar.manager.TimeManager;
import it.trainradar.manager.TrainDelayManager;

public class Train implements Serializable {
    public final static double MAX_RAND_OFFSET = 2e-4;

    private String agency;
    private String category;
    private String name;
    private String internationalDeparture;
    private String internationalArrival;
    private ArrayList<Stop> stops;
    private Realtime realtime;

    public String getName() {
        return String.format(Locale.ITALY, "%s %s %s", agency, category, name);
    }

    public List<String> getAllIDs() {
        return Arrays.asList(name.split("/"));
    }

    public String getID() {
        return getAllIDs().get(0);
    }

    public String getAgency() {
        return agency;
    }

    public String getCategory() {
        return category;
    }

    public Station getDepartureStation() {
        return stops.get(0).getStation();
    }

    public Station getArrivalStation() {
        return stops.get(stops.size() - 1).getStation();
    }

    public LocalTime getDepartureTime() {
        return stops.get(0).getDeparture();
    }

    public LocalTime getArrivalTime() {
        return stops.get(stops.size() - 1).getArrival();
    }

    public ArrayList<Stop> getStops() {
        return stops;
    }

    public LatLng getPosition(LocalTime time) {
        Realtime realtime = TrainDelayManager.getRealtime(this);
        if (realtime != null && realtime.getDelay() != null) {
            time = time.minus(Duration.ofMinutes(realtime.getDelay()));
        }
        for (int i = 0; i < stops.size() - 1; i++) {
            if (i > 0 && !stops.get(i).getArrival().equals(stops.get(i).getDeparture()) && TimeManager.isTimeOnInterval(time, stops.get(i).getArrival(), stops.get(i).getDeparture())) {
                Station st = stops.get(i).getStation();
                Random rng = new Random(name.hashCode());
                return new LatLng(st.getLatitude() + 2 * MAX_RAND_OFFSET * rng.nextDouble() - MAX_RAND_OFFSET,
                        st.getLongitude() + 2 * MAX_RAND_OFFSET * rng.nextDouble() - MAX_RAND_OFFSET);
            }
            if (TimeManager.isTimeOnInterval(time, stops.get(i).getDeparture(), stops.get(i + 1).getArrival())) {
                List<Station> path = StationManager.getPath(stops.get(i).getStation(), stops.get(i + 1).getStation());
                int elapsed = TimeManager.timeDiff(stops.get(i).getDeparture(), time);
                int total = TimeManager.timeDiff(stops.get(i).getDeparture(), stops.get(i + 1).getArrival());
                float dist = 0;
                for (int j = 0; j < path.size() - 1; j++) {
                    dist += path.get(j).distanceTo(path.get(j + 1));
                }
                float currDist = dist * elapsed / total;
                for (int j = 0; j < path.size() - 1; j++) {
                    float nextDist = path.get(j).distanceTo(path.get(j + 1));
                    if (currDist < nextDist) {
                        double lat = (path.get(j).getLatitude() * (nextDist - currDist) + path.get(j + 1).getLatitude() * currDist) / nextDist;
                        double lng = (path.get(j).getLongitude() * (nextDist - currDist) + path.get(j + 1).getLongitude() * currDist) / nextDist;
                        return new LatLng(lat, lng);
                    }
                    currDist -= nextDist;
                }
                return null;
            }
        }
        return null;
    }

    public LatLng getPosition() {
        return getPosition(TimeManager.now());
    }

    public Location getLocation(LocalTime time) {
        LatLng pos = getPosition(time);
        if (pos == null) return null;
        Location location = new Location(name);
        location.setLatitude(pos.latitude);
        location.setLongitude(pos.longitude);
        return location;
    }

    public Location getLocation() {
        return getLocation(TimeManager.now());
    }

    public Realtime getRealtime() {
        return realtime;
    }

    public void setRealtime(Realtime realtime) {
        this.realtime = realtime;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Train)) return false;
        Train train = (Train) object;
        return agency.equals(train.agency)
                && category.equals(train.category)
                && getAllIDs().stream().anyMatch(id -> train.getAllIDs().contains(id));
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "Train[" + getName() + "]@" + hashCode();
    }
}
