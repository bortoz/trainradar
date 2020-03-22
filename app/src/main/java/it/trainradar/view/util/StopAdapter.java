package it.trainradar.view.util;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import it.trainradar.R;
import it.trainradar.core.Train;

public class StopAdapter extends RecyclerView.Adapter<StopAdapterViewHolder> {
    private Train train;

    public StopAdapter() {
        this.train = null;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StopAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.item_stop, parent, false);
        return new StopAdapterViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull StopAdapterViewHolder holder, int position) {
        holder.bind(train, position);
    }

    @Override
    public int getItemCount() {
        return train == null ? 0 : train.getStops().size();
    }
}

