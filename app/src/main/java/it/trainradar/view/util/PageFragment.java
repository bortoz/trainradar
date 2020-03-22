package it.trainradar.view.util;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import it.trainradar.core.Train;
import it.trainradar.manager.TrainDelayManager;
import it.trainradar.manager.util.OnRealtimeChangeListener;

public abstract class PageFragment extends Fragment implements OnRealtimeChangeListener {
    protected Train train;
    private int position;

    public PageFragment(int position, int layoutId) {
        super(layoutId);
        this.position = position;
    }

    public void setTrain(Train train) {
        this.train = train;
        if (getView() != null) onTrainChange(train);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TrainDelayManager.addUpdateDelayListener(this);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            Train savedTrain = (Train) savedInstanceState.getSerializable("train");
            if (savedTrain != null) {
                setTrain(savedTrain);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (train != null) {
            outState.putSerializable("train", train);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TrainDelayManager.removeUpdateDelayListener(this);
    }

    public int getPosition() {
        return position;
    }

    public abstract void onTrainChange(Train train);
}
