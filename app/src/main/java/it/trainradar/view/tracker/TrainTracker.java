package it.trainradar.view.tracker;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import it.trainradar.core.Train;
import it.trainradar.manager.TimeManager;
import it.trainradar.view.tracker.Scheduler.SchedulerResult;

public class TrainTracker implements Runnable {
    private final Train train;
    private final Marker marker;
    SchedulerResult future;

    TrainTracker(Marker marker) {
        this.marker = marker;
        this.train = (Train) marker.getTag();
    }

    @Override
    public void run() {
        LatLng pos = train.getPosition(TimeManager.now());
        if (pos != null) {
                marker.setPosition(pos);
        }
    }

    public Train getTrain() {
        return train;
    }

    public Marker getMarker() {
        return marker;
    }

    public void onResume() {
        future.resume();
    }

    public void onPause() {
        future.cancel();
    }

    public void onDestroy() {
        marker.remove();
        future.cancel();
    }

    public boolean hasFocus() {
        return marker.isInfoWindowShown();
    }
}
