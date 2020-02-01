package it.trainradar.core;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

public class Station implements Serializable {
    private String id;
    private ArrayList<String> altIds;
    private String name;
    private LatLng position;
    private ArrayList<String> links;

    public String getId() {
        return id;
    }

    public ArrayList<String> getAltIds() {
        return altIds;
    }

    public String getName() {
        return name;
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

    public ArrayList<String> getLinks() {
        return links;
    }
}
