package it.trainradar.view.util;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.trainradar.R;
import it.trainradar.core.Realtime;
import it.trainradar.core.Train;
import it.trainradar.manager.TimeManager;
import it.trainradar.manager.TrainDelayManager;
import it.trainradar.manager.TrainManager;

public class TrainAdapter extends RecyclerView.Adapter<TrainAdapterViewHolder> {
    private Location location;
    private OnClickListener onClickListener;
    private List<Train> trains;
    private String filterID;
    private Set<String> filterCategories;

    public TrainAdapter(Location location, OnClickListener onClickListener) {
        this.location = location;
        this.onClickListener = onClickListener;
        this.filterCategories = new HashSet<>();
    }

    @NonNull
    @Override
    public TrainAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CardView view = (CardView) inflater.inflate(R.layout.item_train, parent, false);
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

    public boolean setFilterID(String filterID) {
        this.filterID = filterID;
        return setData();
    }

    public String getFilterID() {
        return filterID;
    }

    public Set<String> getFilterCategories() {
        return filterCategories;
    }

    public boolean setFilterCategories(Set<String> categories) {
        filterCategories = categories;
        return setData();
    }

    public List<Train> getTrains() {
        return trains;
    }

    private boolean setData() {
        LocalTime time = TimeManager.now();
        Stream<Train> stream = TrainManager.getTrains().parallelStream();
        if (filterID != null || location == null) {
            stream = stream.sorted((a, b) -> {
                if (!a.getID().equals(b.getID()))
                    return Integer.compare(Integer.parseInt(a.getID()), Integer.parseInt(b.getID()));
                if (location == null) return 0;
                Location la = a.getLocation(time);
                Location lb = b.getLocation(time);
                if (la == null && lb == null) return 0;
                if (la == null) return 1;
                if (lb == null) return -1;
                return Double.compare(location.distanceTo(a.getLocation(time)),
                        location.distanceTo(b.getLocation(time)));
            });
        } else {
            stream = stream.filter(t -> {
                Realtime realtime = TrainDelayManager.getRealtime(t);
                return (realtime == null || !realtime.isCancelled()) && t.getPosition(time) != null;
            }).sorted((a, b) -> Double.compare(location.distanceTo(a.getLocation(time)),
                    location.distanceTo(b.getLocation(time))));
        }
        if (filterID != null) {
            stream = stream.filter(t -> t.getAllIDs().stream().anyMatch(id -> id.startsWith(filterID)));
        }
        if (!filterCategories.isEmpty()) {
            stream = stream.filter(t -> filterCategories.contains(t.getCategory()));
        }
        trains = stream.collect(Collectors.toList());
        notifyDataSetChanged();
        return !trains.isEmpty();
    }
}