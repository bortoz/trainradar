package it.trainradar.manager;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.concurrent.Semaphore;

public class TimeManager extends BaseManager {
    public final static int SEC_PER_DAY = 24 * 60 * 60;

    private static boolean isLoaded = false;
    private static long initTimestamp;
    private static LocalDateTime realTime;
    private static Semaphore requestLock = new Semaphore(1);

    public static void load(Context context) {
        if (isLoaded) return;
        isLoaded = true;

        initTimestamp = System.currentTimeMillis();
        realTime = LocalDateTime.now();
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest("https://worldtimeapi.org/api/timezone/Europe/Rome", null,
                response -> {
                    try {
                        realTime = OffsetDateTime.parse(response.getString("datetime")).toLocalDateTime();
                        initTimestamp = System.currentTimeMillis();
                    } catch (Exception ignored) {
                    }
                    requestLock.release();
                }, error -> requestLock.release());
        requestLock.acquireUninterruptibly();
        executeTask(() -> {
            requestLock.acquireUninterruptibly();
        });
        queue.add(request);
    }

    public static LocalTime now() {
        return realTime.plus(Duration.ofMillis(System.currentTimeMillis() - initTimestamp)).toLocalTime();
    }

    public static long timestamp() {
        return System.currentTimeMillis() / 1000;
    }

    public static Boolean isTimeOnInterval(LocalTime t, LocalTime a, LocalTime b) {
        if (a.compareTo(b) <= 0) {
            return a.compareTo(t) <= 0 && b.compareTo(t) > 0;
        } else {
            return a.compareTo(t) <= 0 || b.compareTo(t) > 0;
        }
    }

    public static int timeDiff(LocalTime a, LocalTime b) {
        return (b.toSecondOfDay() - a.toSecondOfDay() + SEC_PER_DAY) % SEC_PER_DAY;
    }
}
