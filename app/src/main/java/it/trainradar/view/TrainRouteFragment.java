package it.trainradar.view;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.format.DateTimeFormatter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.trainradar.R;
import it.trainradar.core.Train;
import it.trainradar.manager.StationManager;
import it.trainradar.manager.TrainDelayManager;
import it.trainradar.manager.TrainDelayManager.UpdateDelayListener;
import it.trainradar.manager.TrainManager;
import it.trainradar.view.util.StopAdapter;

public class TrainRouteFragment extends Fragment implements UpdateDelayListener {
    private Train train;

    public TrainRouteFragment() {
        this.train = null;
    }

    public TrainRouteFragment(Train train) {
        this.train = train;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Train savedTrain = (Train) savedInstanceState.getSerializable("train");
            if (savedTrain != null) train = savedTrain;
        }
        TrainDelayManager.addUpdateDelayListener(this);
        return inflater.inflate(R.layout.fragment_train_route, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Train savedTrain = (Train) savedInstanceState.getSerializable("train");
            if (savedTrain != null) train = savedTrain;
        }

        TextView lblTrainName = view.findViewById(R.id.lblTrainName);
        TextView lblTrainStDeparture = view.findViewById(R.id.lblTrainStDeparture);
        TextView lblTrainStArrival = view.findViewById(R.id.lblTrainStArrival);
        TextView lblTrainDeparture = view.findViewById(R.id.lblTrainDeparture);
        TextView lblTrainArrival = view.findViewById(R.id.lblTrainArrival);

        lblTrainName.setText(TrainManager.getFormattedName(train));
        lblTrainStDeparture.setText(StationManager.getStation(train.getIdDeparture()).getName());
        lblTrainStArrival.setText(StationManager.getStation(train.getIdArrival()).getName());
        lblTrainDeparture.setText(train.getDeparture().format(DateTimeFormatter.ofPattern(getString(R.string.time_format))));
        lblTrainArrival.setText(train.getArrival().format(DateTimeFormatter.ofPattern(getString(R.string.time_format))));

        Integer delay = TrainDelayManager.getDelay(train);
        if (delay != null) {
            updateDelay(view, delay);
        }

        RecyclerView recyclerView = view.findViewById(R.id.listStops);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(new StopAdapter(train));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("train", train);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TrainDelayManager.removeUpdateDelayListener(this);
    }

    @Override
    public void onUpdateDelay(Train train, Integer delay) {
        if (train.equals(this.train) && getView() != null) {
            updateDelay(getView(), delay);
        }
    }

    private void updateDelay(View view, int delay) {
        SpannableString delayFormat;
        if (delay > 0) {
            delayFormat = new SpannableString(getString(R.string.delay_message_compact, delay));
            delayFormat.setSpan(new ForegroundColorSpan(view.getContext().getColor(R.color.train_delay)), 0, delayFormat.length(), 0);
        } else {
            delayFormat = new SpannableString(getString(R.string.advance_message_compact, delay));
            delayFormat.setSpan(new ForegroundColorSpan(view.getContext().getColor(R.color.train_advance)), 0, delayFormat.length(), 0);
        }
        TextView lblTrainDelay = view.findViewById(R.id.lblTrainDelay);
        lblTrainDelay.setText(delayFormat);
    }
}
