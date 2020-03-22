package it.trainradar.view.tracker;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import it.trainradar.core.Train;
import it.trainradar.manager.TrainDelayManager;
import it.trainradar.manager.TrainManager;
import it.trainradar.manager.util.OnRealtimeChangeListener;
import it.trainradar.view.tracker.SchedulableRunnable.UpdateLevel;

public class TrackerManager implements OnRealtimeChangeListener {
    private final static int THRESHOLD_REMOVED_MARKER = 10;

    private List<Marker> markers;
    private GoogleMap googleMap;
    private List<SchedulableRunnable> runnables;
    private AtomicInteger removedMarker;
    private UpdateLevel updateLevel;

    public void onCreate(GoogleMap gMap) {
        googleMap = gMap;
        markers = Collections.synchronizedList(new ArrayList<>());
        runnables = new ArrayList<>();
        removedMarker = new AtomicInteger(0);
        runnables.add(new TrainUpdaterTask(googleMap, markers, removedMarker));
        runnables.add(new MarkerUpdaterTask(markers));
        updateLevel = UpdateLevel.NO_UPDATE;
        TrainDelayManager.addUpdateDelayListener(this);
    }

    public void onDestroy() {
        TrainDelayManager.removeUpdateDelayListener(this);
    }

    public void setUpdateDelay(UpdateLevel delay) {
        if (runnables == null) return;
        updateLevel = delay;
        for (SchedulableRunnable runnable : runnables) {
            runnable.setDelay(delay);
        }
    }

    public Marker forceShowTrain(Train train) {
        Marker marker = markers.stream()
                .filter(m -> train.equals(m.getTag()))
                .findAny()
                .orElse(null);
        if (marker == null) {
            LatLng pos = train.getPosition();
            if (pos == null) pos = train.getArrivalStation().getPosition();
            marker = googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(train.getName())
                    .icon(TrainManager.getTrainIcon()));
            marker.setTag(train);
            markers.add(marker);
        }
        return marker;
    }

    public void onResume() {
        setUpdateDelay(UpdateLevel.NO_UPDATE);
    }

    public void onPause() {
        setUpdateDelay(UpdateLevel.UPDATE_SLOW);
    }

    @Override
    public void onRealtimeChange(Train train) {
        markers.removeIf(marker -> {
            Train t = (Train) marker.getTag();
            if (train.equals(t)) {
                removedMarker.incrementAndGet();
                marker.remove();
                return true;
            }
            return false;
        });
        if (removedMarker.get() > THRESHOLD_REMOVED_MARKER) {
            setUpdateDelay(updateLevel);
        }
    }
}
