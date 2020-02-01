package it.trainradar.view.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import it.trainradar.R;
import it.trainradar.core.Train;
import it.trainradar.manager.TrainManager;

public class TrainSearchAdapter extends RecyclerView.Adapter<TrainSearchAdapterViewHolder> {
    private List<Train> trains;
    private final OnClickListener clickListener;

    public TrainSearchAdapter(OnClickListener clickListener) {
        this.trains = new ArrayList<>();
        this.clickListener = clickListener;
    }

    public void setFilter(String filter) {
        if (filter.equals("")) {
            trains.clear();
        } else {
            trains = TrainManager.getTrains()
                    .stream()
                    .filter(t -> Integer.toString(t.getId()).startsWith(filter))
                    .sorted((a, b) -> Integer.compare(a.getId(), b.getId()))
                    .collect(Collectors.toList());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrainSearchAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_train_search, parent, false);
        view.setOnClickListener(clickListener);
        return new TrainSearchAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainSearchAdapterViewHolder holder, int position) {
        holder.bind(trains.get(position));
    }

    @Override
    public int getItemCount() {
        return trains.size();
    }

    @Override
    public long getItemId(int position) {
        return trains.get(position).hashCode();
    }
}
