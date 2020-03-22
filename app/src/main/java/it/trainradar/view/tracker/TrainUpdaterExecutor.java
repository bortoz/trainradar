package it.trainradar.view.tracker;

import android.os.AsyncTask;

import androidx.core.util.Pair;

import com.android.volley.Request;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.trainradar.core.Realtime;
import it.trainradar.core.Train;
import it.trainradar.manager.TimeManager;
import it.trainradar.manager.TrainDelayManager;
import it.trainradar.manager.TrainManager;

// TODO: avoid async task
public class TrainUpdaterExecutor extends AsyncTask<Void, Void, Void> {
    private final static int RENDER_TRAINS = 60;

    private List<Marker> markers;
    private boolean updateDelay;
    private List<Pair<Train, LatLng>> addedTrains;
    private List<Train> removedTrains;
    private Map<Train, Marker> markersMap;
    private LatLngBounds bounds;
    private GoogleMap googleMap;
    private Marker focusMarker;

    public TrainUpdaterExecutor(GoogleMap googleMap, List<Marker> markers, boolean updateDelay) {
        this.googleMap = googleMap;
        this.markers = markers;
        this.updateDelay = updateDelay;
        this.markersMap = new HashMap<>();
        this.addedTrains = new ArrayList<>();
        this.removedTrains = new ArrayList<>();
    }

    @Override
    protected void onPreExecute() {
        bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
        focusMarker = markers.stream()
                .filter(Marker::isInfoWindowShown)
                .findAny()
                .orElse(null);
        markers.forEach(m -> markersMap.put((Train) m.getTag(), m));
    }

    @Override
    protected Void doInBackground(Void... voids) {
        int render = 0;
        List<Train> trains = TrainManager.getTrains();
        LocalTime time = TimeManager.now();
        for (Train train : trains) {
            if (isCancelled()) break;
            LatLng position = train.getPosition(time);
            Marker marker = markersMap.get(train);
            Realtime realtime = TrainDelayManager.getRealtime(train);
            if ((realtime == null || !realtime.isCancelled()) && ((marker != null && marker == focusMarker) || (position != null && bounds.contains(position) && render < RENDER_TRAINS))) {
                if (marker == null) addedTrains.add(Pair.create(train, position));
                render++;
            } else if (marker != null) {
                removedTrains.add(train);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        addedTrains.forEach(pair -> {
            Train train = pair.first;
            LatLng position = pair.second;
            if (updateDelay) TrainDelayManager.requestRealtime(train, Request.Priority.NORMAL);
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(train.getName())
                    .icon(TrainManager.getTrainIcon()));
            marker.setTag(train);
            markers.add(marker);
        });
        markers.removeIf(marker -> {
            if (removedTrains.contains(marker.getTag())) {
                marker.remove();
                return true;
            }
            return false;
        });
    }
}
