package it.trainradar.view.util;

import android.location.Location;
import android.view.View.OnClickListener;
import android.widget.TextView;

import java.time.format.DateTimeFormatter;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import it.trainradar.R;
import it.trainradar.core.Train;
import it.trainradar.manager.StationManager;
import it.trainradar.manager.TimeManager;
import it.trainradar.manager.TrainManager;

public class TrainAdapterViewHolder extends RecyclerView.ViewHolder {
    private final CardView view;
    private final String timeFormat;

    public TrainAdapterViewHolder(CardView view, OnClickListener onClickListener) {
        super(view);
        this.timeFormat = view.getContext().getString(R.string.time_format);
        this.view = view;
        this.view.setOnClickListener(onClickListener);
    }

    public void bind(Train train, Location location) {
        view.setTag(train);

        TextView lblTrainName = view.findViewById(R.id.lblTrainName);
        TextView lblTrainStDeparture = view.findViewById(R.id.lblTrainStDeparture);
        TextView lblTrainStArrival = view.findViewById(R.id.lblTrainStArrival);
        TextView lblTrainDeparture = view.findViewById(R.id.lblTrainDeparture);
        TextView lblTrainArrival = view.findViewById(R.id.lblTrainArrival);
        TextView lblTrainDist = view.findViewById(R.id.lblTrainDist);

        lblTrainName.setText(TrainManager.getFormattedName(train));
        lblTrainStDeparture.setText(StationManager.getStation(train.getIdDeparture()).getName());
        lblTrainStArrival.setText(StationManager.getStation(train.getIdArrival()).getName());
        lblTrainDeparture.setText(train.getDeparture().format(DateTimeFormatter.ofPattern(timeFormat)));
        lblTrainArrival.setText(train.getArrival().format(DateTimeFormatter.ofPattern(timeFormat)));

        Location trainLocation = train.getLocation(TimeManager.now());
        float dist = location.distanceTo(trainLocation != null ? trainLocation : StationManager.getStation(train.getIdArrival()).getLocation());
        lblTrainDist.setText(view.getResources().getString(R.string.distance_label, dist / 1000));
    }
}