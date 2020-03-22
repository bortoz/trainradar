package it.trainradar.manager.util;

import androidx.collection.LruCache;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import it.trainradar.core.Station;
import it.trainradar.manager.StationManager;

public class PathCache extends LruCache<Pair<Station, Station>, List<Station>> {
    private final static int MAX_SIZE = 1000;

    public PathCache() {
        super(MAX_SIZE);
    }

    @Override
    protected List<Station> create(Pair<Station, Station> sts) {
        Station first = sts.first;
        Station last = sts.second;

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
            for (String link : curr.second.getLinks()) {
                Station nextSt = StationManager.getStation(link);
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
