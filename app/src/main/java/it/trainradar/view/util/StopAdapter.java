package it.trainradar.view.util;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import it.trainradar.R;
import it.trainradar.core.Train;

public class StopAdapter extends RecyclerView.Adapter<StopAdapterViewHolder> {
    private final Train train;

    public StopAdapter(Train train) {
        this.train = train;
    }

    @NonNull
    @Override
    public StopAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stop, parent, false);
        return new StopAdapterViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull StopAdapterViewHolder holder, int position) {
        holder.bind(train, position);
    }

    @Override
    public int getItemCount() {
        return train.getStops().size();
    }
}

