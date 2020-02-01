package it.trainradar.view.tracker;

import android.os.Handler;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

public class Scheduler {
    private static final Handler handler = new Handler();

    public static TrainTracker scheduleTracker(Marker marker, long delay) {
        TrainTracker tracker = new TrainTracker(marker);
        SchedulerResult taskWrapper = new SchedulerResult() {
            private boolean pause = false;

            @Override
            public void run() {
                tracker.run();
                handler.postDelayed(this, delay);
            }

            @Override
            public void resume() {
                if (pause) handler.postDelayed(this, delay);
                pause = false;
            }

            @Override
            public void cancel() {
                if (!pause) handler.removeCallbacks(this);
                pause = true;
            }
        };
        tracker.future = taskWrapper;
        handler.post(taskWrapper);
        return tracker;
    }

    public static SchedulerResult scheduleUpdater(GoogleMap googleMap, List<TrainTracker> trackers, long delay) {
        SchedulerResult taskWrapper = new SchedulerResult() {
            private boolean pause = false;
            private TrainTrackerUpdaterExecutor executor;

            @Override
            public void run() {
                executor = new TrainTrackerUpdaterExecutor(googleMap, trackers);
                executor.execute();
                handler.postDelayed(this, delay);
            }

            @Override
            public void resume() {
                if (pause) handler.postDelayed(this, delay);
                pause = false;
            }

            @Override
            public void cancel() {
                if (executor != null) executor.cancel(true);
                if (!pause) handler.removeCallbacks(this);
                pause = true;
            }
        };
        handler.post(taskWrapper);
        return taskWrapper;
    }

    public interface SchedulerResult extends Runnable {
        void resume();
        void cancel();
    }
}
