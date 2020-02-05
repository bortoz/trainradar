Set<Station> visited = new HashSet<>();
Map<Station, Double> dist = new HashMap<>();
Map<Station, Station> prev = new HashMap<>();
Queue<Pair<Double, Station>> queue = new PriorityQueue<>((a, b) -> Double.compare(a.first, b.first));
queue.add(new Pair<>(0d, first));
dist.put(first, 0d);

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
