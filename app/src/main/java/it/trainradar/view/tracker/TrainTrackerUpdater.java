package it.trainradar.view.tracker;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.trainradar.core.Train;
import it.trainradar.manager.StationManager;
import it.trainradar.manager.TimeManager;
import it.trainradar.manager.TrainManager;
import it.trainradar.view.tracker.Scheduler.SchedulerResult;

public class TrainTrackerUpdater {
    public final static int RENDER_TRAINS = 60;
    public final static int TRAIN_UPDATE_DELAY = 1000;
    public final static int UPDATE_SLOW_DELAY = 20 * 1000;
    public final static int UPDATE_FAST_DELAY = 1000;

    private final List<TrainTracker> trackers;
    private GoogleMap googleMap;
    private SchedulerResult updater;

    public TrainTrackerUpdater() {
        trackers = Collections.synchronizedList(new ArrayList<>());
    }

    public void onCreate(GoogleMap gMap) {
        googleMap = gMap;
    }

    public void setUpdateDelay(UpdateLevel delay) {
        if (googleMap == null) return;
        if (updater != null) {
            updater.cancel();
        }
        if (delay == UpdateLevel.UPDATE_FAST) {
            updater = Scheduler.scheduleUpdater(googleMap, trackers, UPDATE_FAST_DELAY);
        } else if (delay == UpdateLevel.UPDATE_SLOW) {
            updater = Scheduler.scheduleUpdater(googleMap, trackers, UPDATE_SLOW_DELAY);
        } else {
            updater = null;
        }
    }

    public TrainTracker forceGetTracker(Train train) {
        TrainTracker tracker = trackers.stream()
                .filter(t -> t.getTrain().equals(train))
                .findAny()
                .orElse(null);
        if (tracker == null) {
            LatLng pos = train.getPosition(TimeManager.now());
            if (pos == null) pos = StationManager.getStation(train.getIdArrival()).getPosition();
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(train.getName())
                    .icon(BitmapDescriptorFactory.fromBitmap(TrainManager.getTrainIcon())));
            marker.setTag(train);
            tracker = Scheduler.scheduleTracker(marker, TRAIN_UPDATE_DELAY);
            trackers.add(tracker);
        }
        return tracker;
    }

    public void onResume() {
        setUpdateDelay(UpdateLevel.UPDATE_SLOW);
        trackers.forEach(TrainTracker::onResume);
    }

    public void onPause() {
        setUpdateDelay(UpdateLevel.NO_UPDATE);
        trackers.forEach(TrainTracker::onPause);
    }

    public void onDestroy() {
        setUpdateDelay(UpdateLevel.NO_UPDATE);
        trackers.forEach(TrainTracker::onDestroy);
        trackers.clear();
    }

    public enum UpdateLevel {
        NO_UPDATE,
        UPDATE_SLOW,
        UPDATE_FAST
    }
}
