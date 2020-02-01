package it.trainradar.view.util;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import it.trainradar.R;
import it.trainradar.core.Train;
import it.trainradar.manager.StationManager;
import it.trainradar.manager.TrainManager;

public class TrainSearchAdapterViewHolder extends RecyclerView.ViewHolder {
    private final View view;

    public TrainSearchAdapterViewHolder(@NonNull View view) {
        super(view);
        this.view = view;
    }

    public void bind(Train train) {
        view.setTag(train);
        TextView trainName = view.findViewById(R.id.lblSearchTrainName);
        TextView trainDeparture = view.findViewById(R.id.lblSearchTrainDeparture);
        TextView trainArrival = view.findViewById(R.id.lblSearchTrainArrival);
        trainName.setText(TrainManager.getFormattedName(train));
        trainDeparture.setText(StationManager.getStation(train.getIdDeparture()).getName());
        trainArrival.setText(StationManager.getStation(train.getIdArrival()).getName());
    }
}
