package it.trainradar.manager;


import android.content.Context;

import androidx.core.util.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.trainradar.R;
import it.trainradar.core.Station;
import it.trainradar.manager.util.PathCache;

public class StationManager extends BaseManager {
    private static boolean isLoaded = false;
    private static Map<String, Station> stations;
    private static PathCache paths;

    public static void load(Context context) {
        if (isLoaded) return;
        isLoaded = true;

        stations = new HashMap<>();
        paths = new PathCache();

        executeTask(() -> {
            Station[] sts = gson.fromJson(getRawResources(context, R.raw.stations), Station[].class);
            Arrays.stream(sts).forEach(st -> stations.put(st.getName(), st));
        });
    }

    public static Station getStation(String name) {
        return stations.get(name);
    }

    public static List<Station> getPath(Station departure, Station arrival) {
        return paths.get(new Pair<>(departure, arrival));
    }
}
