package it.trainradar.core;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

public class Station implements Serializable {
    private String name;
    private LatLng position;
    private ArrayList<String> links;

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return position.latitude;
    }

    public double getLongitude() {
        return position.longitude;
    }

    public LatLng getPosition() {
        return position;
    }

    public Location getLocation() {
        LatLng pos = getPosition();
        Location location = new Location(name);
        location.setLatitude(pos.latitude);
        location.setLongitude(pos.longitude);
        return location;
    }

    public float distanceTo(Station station) {
        return getLocation().distanceTo(station.getLocation());
    }

    public ArrayList<String> getLinks() {
        return links;
    }
}
