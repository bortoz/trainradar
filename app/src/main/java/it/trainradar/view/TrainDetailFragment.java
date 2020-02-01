package it.trainradar.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;

import java.time.format.DateTimeFormatter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import it.trainradar.R;
import it.trainradar.core.Train;
import it.trainradar.manager.StationManager;
import it.trainradar.manager.TimeManager;
import it.trainradar.manager.TrainDelayManager;
import it.trainradar.manager.TrainDelayManager.UpdateDelayListener;
import it.trainradar.manager.TrainManager;

public class TrainDetailFragment extends Fragment implements UpdateDelayListener {
    private final Marker marker;

    public TrainDetailFragment() {
        this.marker = null;
    }

    public TrainDetailFragment(Marker marker) {
        this.marker = marker;
    }

    public Marker getMarker() {
        return marker;
    }

    public Train getTrain() {
        return (Train) marker.getTag();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_train_detail, container, false);
        view.setOnTouchListener((v, event) -> true);

        Train train = (Train) marker.getTag();

        TextView lblTrain = view.findViewById(R.id.lblDetailTrain);
        TextView lblStDeparture = view.findViewById(R.id.lblDetailStDeparture);
        TextView lblStArrival = view.findViewById(R.id.lblDetailStArrival);
        TextView lblDeparture = view.findViewById(R.id.lblDetailDeparture);
        TextView lblArrival = view.findViewById(R.id.lblDetailArrival);
        SeekBar statusBar = view.findViewById(R.id.trainStatusBar);

        lblStDeparture.setText(StationManager.getStation(train.getIdDeparture()).getName());
        lblStArrival.setText(StationManager.getStation(train.getIdArrival()).getName());
        lblDeparture.setText(train.getDeparture().format(DateTimeFormatter.ofPattern(getString(R.string.time_format))));
        lblArrival.setText(train.getArrival().format(DateTimeFormatter.ofPattern(getString(R.string.time_format))));
        lblTrain.setText(TrainManager.getFormattedName(train));

        Integer delay = TrainDelayManager.getDelay(train);
        if (delay != null) {
            updateDelay(view, delay);
        }

        int diff1 = TimeManager.timeDiff(train.getDeparture(), TimeManager.now());
        int diff2 = TimeManager.timeDiff(TimeManager.now(), train.getArrival());
        statusBar.setProgress(100 * diff1 / (diff1 + diff2));
        statusBar.setOnTouchListener((v, event) -> true);

        TrainDelayManager.addUpdateDelayListener(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TrainDelayManager.removeUpdateDelayListener(this);
    }

    @Override
    public void onUpdateDelay(Train train, Integer delay) {
        if (train.equals(marker.getTag()) && getView() != null) {
            updateDelay(getView(), delay);
        }
    }

    private void updateDelay(View view, int delay) {
        SpannableString delayFormat;
        if (delay > 0) {
            delayFormat = new SpannableString(view.getContext().getResources().getQuantityString(R.plurals.delay_message, delay, delay));
            delayFormat.setSpan(new ForegroundColorSpan(view.getContext().getColor(R.color.train_delay)), 0, delayFormat.length(), 0);
        } else if (delay < 0) {
            delayFormat = new SpannableString(view.getContext().getResources().getQuantityString(R.plurals.advance_message, -delay, -delay));
            delayFormat.setSpan(new ForegroundColorSpan(view.getContext().getColor(R.color.train_advance)), 0, delayFormat.length(), 0);
        } else {
            delayFormat = new SpannableString(view.getContext().getString(R.string.on_time_message));
            delayFormat.setSpan(new ForegroundColorSpan(view.getContext().getColor(R.color.train_advance)), 0, delayFormat.length(), 0);
        }
        TextView lblDelay = view.findViewById(R.id.lblDetailDelay);
        lblDelay.setText(delayFormat);
    }
}
