package it.trainradar.view.tracker;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

import it.trainradar.core.Train;

public class MarkerUpdaterTask extends SchedulableRunnable {
    public final static int TRAIN_UPDATE_DELAY = 1000;

    private List<Marker> markers;

    public MarkerUpdaterTask(List<Marker> markers) {
        this.markers = markers;
    }

    @Override
    public void run() {
        for (Marker marker : markers) {
            Train train = (Train) marker.getTag();
            LatLng pos = train.getPosition();
            if (pos != null) {
                marker.setPosition(pos);
            } else {
                marker.setPosition(train.getArrivalStation().getPosition());
            }
        }
    }

    @Override
    public long getSlowDelay() {
        return TRAIN_UPDATE_DELAY;
    }

    @Override
    public long getFastDelay() {
        return TRAIN_UPDATE_DELAY;
    }
}
