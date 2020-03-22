package it.trainradar.view.tracker;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static android.os.AsyncTask.Status.FINISHED;

public class TrainUpdaterTask extends SchedulableRunnable {
    public final static int UPDATE_SLOW_DELAY = 20 * 1000;
    public final static int UPDATE_FAST_DELAY = 1000;

    private TrainUpdaterExecutor executor;
    private GoogleMap googleMap;
    private List<Marker> markers;
    private AtomicInteger removedMarker;

    public TrainUpdaterTask(GoogleMap googleMap, List<Marker> markers, AtomicInteger removedMarker) {
        this.googleMap = googleMap;
        this.markers = markers;
        this.removedMarker = removedMarker;
    }

    @Override
    public void run() {
        removedMarker.set(0);
        if (executor != null && executor.getStatus() != FINISHED) executor.cancel(true);
        executor = new TrainUpdaterExecutor(googleMap, markers, getDelay() == UpdateLevel.UPDATE_SLOW);
        executor.execute();
    }

    @Override
    public void onCancel() {
        if (executor != null) executor.cancel(true);
    }

    @Override
    public long getSlowDelay() {
        return UPDATE_SLOW_DELAY;
    }

    @Override
    public long getFastDelay() {
        return UPDATE_FAST_DELAY;
    }
}
