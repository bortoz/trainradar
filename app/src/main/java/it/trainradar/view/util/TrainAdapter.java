package it.trainradar.view.util;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import it.trainradar.R;
import it.trainradar.core.Train;
import it.trainradar.manager.TimeManager;
import it.trainradar.manager.TrainManager;

public class TrainAdapter extends RecyclerView.Adapter<TrainAdapterViewHolder> {
    private final Location location;
    private final OnClickListener onClickListener;
    private final List<Train> trains;

    public TrainAdapter(Location location, OnClickListener onClickListener) {
        this.location = location;
        this.onClickListener = onClickListener;
        this.trains = TrainManager.getTrains()
                .stream()
                .filter(train -> train.getLocation(TimeManager.now()) != null)
                .sorted((a, b) -> Double.compare(location.distanceTo(a.getLocation(TimeManager.now())), location.distanceTo(b.getLocation(TimeManager.now()))))
                .collect(Collectors.toList());
    }

    @NonNull
    @Override
    public TrainAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView view = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_train, parent, false);
        return new TrainAdapterViewHolder(view, onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainAdapterViewHolder holder, int position) {
        holder.bind(trains.get(position), location);
    }

    @Override
    public int getItemCount() {
        return trains.size();
    }
}