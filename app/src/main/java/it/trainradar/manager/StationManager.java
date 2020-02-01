package it.trainradar.manager;


import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import androidx.collection.LruCache;
import androidx.core.util.Pair;
import it.trainradar.R;
import it.trainradar.core.Station;

public class StationManager extends JsonManager {
    private static Map<String, Station> stations;
    private static PathCache paths;

    public static void load(Context context) {
        stations = new HashMap<>();
        Station[] lst = gson.fromJson(getRawResources(context, R.raw.stations), Station[].class);
        for (Station station : lst) {
            stations.put(station.getId(), station);
            if (station.getAltIds() != null) {
                for (String id : station.getAltIds()) {
                    stations.put(id, station);
                }
            }
        }
        paths = new PathCache();
    }

    public static Station getStation(String id) {
        return stations.get(id);
    }

    public static List<Station> getPath(String idDeparture, String idArrival) {
        return paths.get(new Pair<>(idDeparture, idArrival));
    }

    private static class PathCache extends LruCache<Pair<String, String>, List<Station>> {
        private final static int MAX_SIZE = 1000;

        private PathCache() {
            super(MAX_SIZE);
        }

        @Override
        protected List<Station> create(Pair<String, String> ids) {
            Station first = StationManager.getStation(ids.first);
            Station last = StationManager.getStation(ids.second);

            Set<Station> visited = new HashSet<>();
            Map<Station, Double> dist = new HashMap<>();
            Map<Station, Station> prev = new HashMap<>();
            Queue<Pair<Double, Station>> queue = new PriorityQueue<>((a, b) -> Double.compare(a.first, b.first));
            queue.add(new Pair<>(0., first));
            dist.put(first, 0.);

            while (!queue.isEmpty()) {
                Pair<Double, Station> curr = queue.poll();
                if (visited.contains(curr.second)) continue;
                visited.add(curr.second);
                if (curr.second == last) break;
                for (String id : curr.second.getLinks()) {
                    Station nextSt = StationManager.getStation(id);
                    double nextDist = curr.first + curr.second.getLocation().distanceTo(nextSt.getLocation());
                    Double currNextDist = dist.get(nextSt);
                    if (currNextDist == null || nextDist < currNextDist) {
                        dist.put(nextSt, nextDist);
                        prev.put(nextSt, curr.second);
                        queue.add(new Pair<>(nextDist, nextSt));
                    }
                }
            }

            List<Station> path = new ArrayList<>();
            if (visited.contains(last)) {
                while (last != first) {
                    path.add(last);
                    last = prev.get(last);
                }
                path.add(first);
                Collections.reverse(path);
            } else {
                path.add(first);
                path.add(last);
            }

            return path;
        }
    }
}
