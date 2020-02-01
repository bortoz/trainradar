package it.trainradar.view.tracker;

import android.os.AsyncTask;

import com.android.volley.Request;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import it.trainradar.core.Train;
import it.trainradar.manager.StationManager;
import it.trainradar.manager.TimeManager;
import it.trainradar.manager.TrainDelayManager;
import it.trainradar.manager.TrainManager;

import static it.trainradar.view.tracker.TrainTrackerUpdater.RENDER_TRAINS;
import static it.trainradar.view.tracker.TrainTrackerUpdater.TRAIN_UPDATE_DELAY;

public class TrainTrackerUpdaterExecutor extends AsyncTask<Void, Void, List<TrainTrackerUpdaterExecutor.Progress>> {
    private final List<TrainTracker> trackers;
    private LatLngBounds bounds;
    private final GoogleMap googleMap;
    private TrainTracker focusTracker;

    public TrainTrackerUpdaterExecutor(GoogleMap googleMap, List<TrainTracker> trackers) {
        this.googleMap = googleMap;
        this.trackers = trackers;
    }

    @Override
    protected void onPreExecute() {
        bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
        focusTracker = trackers.stream()
                .filter(TrainTracker::hasFocus)
                .findAny()
                .orElse(null);
    }

    @Override
    protected List<Progress> doInBackground(Void... voids) {
        int render = 0;
        List<Train> trains = TrainManager.getTrains();
        LocalTime time = TimeManager.now();
        List<Progress> progresses = new ArrayList<>();
        for (Train train : trains) {
            if (isCancelled()) break;
            LatLng position = train.getPosition(time);
            TrainTracker tracker = trackers
                    .stream()
                    .filter(t -> train.equals(t.getTrain()))
                    .findAny()
                    .orElse(null);
            if ((tracker != null && tracker == focusTracker) || (position != null && bounds.contains(position) && render < RENDER_TRAINS)) {
                LatLng pos = position != null ? position : StationManager.getStation(train.getIdArrival()).getPosition();
                TrainDelayManager.requestDelay(train, Request.Priority.NORMAL);
                if (tracker == null) {
                    progresses.add(() -> {
                        Marker marker = googleMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title(train.getName())
                                .icon(BitmapDescriptorFactory.fromBitmap(TrainManager.getTrainIcon())));
                        marker.setTag(train);
                        TrainTracker tr = Scheduler.scheduleTracker(marker, TRAIN_UPDATE_DELAY);
                        trackers.add(tr);
                    });
                }
                render++;
            } else if (tracker != null) {
                progresses.add(() -> {
                    trackers.remove(tracker);
                    tracker.onDestroy();
                });
            }
        }
        // TODO: lazy update
        return progresses;
    }

    @Override
    protected void onPostExecute(List<Progress> result) {
        for (Progress progress : result) {
            progress.onProgress();
        }
    }

    public interface Progress {
        void onProgress();
    }
}
