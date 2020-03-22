package it.trainradar.view.tracker;

import android.os.Handler;
import android.os.Looper;

public abstract class SchedulableRunnable implements Runnable {
    private static Handler handler = new Handler(Looper.getMainLooper());
    private Runnable task;
    private UpdateLevel delay = UpdateLevel.NO_UPDATE;

    public UpdateLevel getDelay() {
        return delay;
    }

    public void setDelay(UpdateLevel newDelay) {
        this.delay = newDelay;
        cancel();
        if (delay == UpdateLevel.UPDATE_SLOW) {
            task = new Runnable() {
                @Override
                public void run() {
                    SchedulableRunnable.this.run();
                    handler.postDelayed(this, getSlowDelay());
                }
            };
            handler.post(task);
        } else if (delay == UpdateLevel.UPDATE_FAST) {
            task = new Runnable() {
                @Override
                public void run() {
                    SchedulableRunnable.this.run();
                    handler.postDelayed(this, getFastDelay());
                }
            };
            handler.post(task);
        }
    }

    public final void cancel() {
        if (task != null) handler.removeCallbacks(task);
        onCancel();
    }

    public void onCancel() {

    }

    public abstract long getSlowDelay();

    public abstract long getFastDelay();

    public enum UpdateLevel {
        NO_UPDATE,
        UPDATE_SLOW,
        UPDATE_FAST
    }
}