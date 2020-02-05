public class Scheduler {
    private static final Handler handler = new Handler();

    public static void scheduleTracker(Marker marker, long delay) {
        Train train = (Train) marker.getTag();
        handler.post(new Runnable() {
            @Override
            public void run() {
                LatLng pos = train.getPosition(TimeManager.now());
                marker.setPosition(pos);
                handler.postDelayed(this, delay);
            }
        });
    }
}
