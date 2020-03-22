package it.trainradar.view.util;

import android.location.Location;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;

import it.trainradar.R;
import it.trainradar.core.Train;
import it.trainradar.manager.TrainManager;

public class TrainAdapterViewHolder extends RecyclerView.ViewHolder {
    private CardView view;
    private String timeFormat;

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
        lblTrainStDeparture.setText(train.getDepartureStation().getName());
        lblTrainStArrival.setText(train.getArrivalStation().getName());
        lblTrainDeparture.setText(train.getDepartureTime().format(DateTimeFormatter.ofPattern(timeFormat)));
        lblTrainArrival.setText(train.getArrivalTime().format(DateTimeFormatter.ofPattern(timeFormat)));

        Location trainLocation = train.getLocation();
        if (trainLocation != null) {
            float dist = location.distanceTo(trainLocation);
            lblTrainDist.setText(view.getResources().getString(R.string.distance_label, dist / 1000));
        } else {
            lblTrainDist.setText("");
        }
    }
}