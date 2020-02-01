package it.trainradar.view.util;

import com.android.volley.Request;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.time.LocalTime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import it.trainradar.R;
import it.trainradar.core.Train;
import it.trainradar.manager.StationManager;
import it.trainradar.manager.TimeManager;
import it.trainradar.manager.TrainDelayManager;
import it.trainradar.view.TrainDetailFragment;

public class TrainDetailFragmentManager {
    private final AppCompatActivity activity;
    private final FragmentManager fragmentManager;
    private TrainDetailFragment prevFragment;

    public TrainDetailFragmentManager(AppCompatActivity activity) {
        this.activity = activity;
        this.fragmentManager = activity.getSupportFragmentManager();
    }

    public void addOrReplace(Marker marker) {
        Train train = (Train) marker.getTag();
        TrainDelayManager.requestDelay(train, Request.Priority.IMMEDIATE);
        LocalTime time = TimeManager.now();
        TrainDetailFragment nextFragment = new TrainDetailFragment(marker);
        if (prevFragment == null) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
                    .add(R.id.trainDetailContainer, nextFragment, "trainDetail")
                    .commit();
        } else if (prevFragment.getTrain() != train) {
            LatLng oldPos = prevFragment.getTrain().getPosition(TimeManager.now());
            boolean direction = Double.compare(train.getPosition(time).longitude,
                    oldPos == null ? StationManager.getStation(prevFragment.getTrain().getIdArrival()).getPosition().longitude : oldPos.longitude) <= 0;
            fragmentManager.beginTransaction()
                    .setCustomAnimations(direction ? R.anim.slide_left_in : R.anim.slide_right_in, direction ? R.anim.slide_left_out : R.anim.slide_right_out)
                    .replace(R.id.trainDetailContainer, nextFragment, "trainDetail")
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.trainDetailContainer, nextFragment, "trainDetail")
                    .commit();
        }
        prevFragment = nextFragment;
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void remove() {
        if (prevFragment != null) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
                    .remove(prevFragment)
                    .commit();
            prevFragment.getMarker().hideInfoWindow();
            prevFragment = null;
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    public boolean onSupportNavigateUp() {
        if (prevFragment != null) {
            remove();
            return true;
        }
        return false;
    }

    public Train getCurrentTrain() {
        return prevFragment.getTrain();
    }
}
