package it.trainradar.manager;

import android.content.Context;
import android.util.Base64;

import com.android.volley.ClientError;
import com.android.volley.Request.Priority;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import it.trainradar.core.Realtime;
import it.trainradar.core.Train;
import it.trainradar.manager.util.OnRealtimeChangeListener;

public class TrainDelayManager extends BaseManager {
    public final static long MAX_CACHING_MILLIS = 5 * 60 * 1000;

    private static boolean isLoaded = false;
    private static Map<Train, Realtime> cache;
    private static Map<Train, Long> expire;
    private static RequestQueue queue;
    private static List<OnRealtimeChangeListener> listeners;

    public static void load(Context context) {
        if (isLoaded) return;
        isLoaded = true;

        cache = new HashMap<>();
        expire = new HashMap<>();
        queue = Volley.newRequestQueue(context);
        listeners = new ArrayList<>();
    }

    public static Realtime getRealtime(Train train) {
        return cache.get(train);
    }

    public static void requestRealtime(Train train, Priority priority) {
        Long cacheExpire = expire.get(train);
        if (cacheExpire == null || cacheExpire < TimeManager.timestamp()) {
            expire.put(train, TimeManager.timestamp() + MAX_CACHING_MILLIS);
            TrainStatusRequest request = new TrainStatusRequest(train,
                    priority,
                    realtimeTrain -> {
                        expire.put(train, TimeManager.timestamp() + MAX_CACHING_MILLIS);
                        Realtime realtime = realtimeTrain.getRealtime();
                        cache.put(train, realtime);
                        if (realtime != null) {
                            for (OnRealtimeChangeListener listener : listeners) {
                                listener.onRealtimeChange(realtimeTrain);
                            }
                        }
                    });
            queue.add(request);
        }
    }

    public static void forceRequestRealtime(Train train, OnRealtimeChangeListener callback) {
        AtomicBoolean requestFilled = new AtomicBoolean(false);
        TrainStatusRequest request = new TrainStatusRequest(train, Priority.IMMEDIATE, realtimeTrain -> {
            Realtime realtime = realtimeTrain.getRealtime();
            expire.put(train, TimeManager.timestamp());
            cache.put(train, realtime);
            if (realtime != null) {
                for (OnRealtimeChangeListener listener : listeners) {
                    listener.onRealtimeChange(realtimeTrain);
                }
            }
            if (train.equals(realtimeTrain) && !requestFilled.get()) {
                callback.onRealtimeChange(realtimeTrain);
                requestFilled.set(true);
            }
        });
        queue.add(request);
    }

    public static void addUpdateDelayListener(OnRealtimeChangeListener listener) {
        listeners.add(listener);
    }

    public static void removeUpdateDelayListener(OnRealtimeChangeListener listener) {
        listeners.remove(listener);
    }

    private static class TrainStatusRequest extends JsonArrayRequest {
        private final static String secret = "RDA3MTJFQjIwQjlGOTg1QThDMjZDRkFDNDIwNjg5NUFGNDkwQTM3NQ==";
        private final static String BASE_URL = "https://backend.orariotreni.app/api/train?date=%2$tF&time=%2$tH%%3A%2$tM&name=%1$s&realtime=true&ver=%3$d";
        private final static int apiVer = 620;

        private Priority priority;

        private TrainStatusRequest(Train train, Priority priority, OnRealtimeChangeListener listener) {
            super(String.format(Locale.ITALY, BASE_URL, train.getID(), LocalDateTime.now(), apiVer), response -> {
                boolean found = false;
                try {
                    for (int i = 0; i < response.length(); i++) {
                        Train realtimeTrain = gson.fromJson(response.get(i).toString(), Train.class);
                        listener.onRealtimeChange(realtimeTrain);
                        found = found || realtimeTrain.equals(train);
                    }
                } catch (Exception ignored) {
                }
                if (!found) listener.onRealtimeChange(train);
            }, error -> {
                if (error instanceof ClientError) {
                    train.setRealtime(new Realtime(true));
                }
                listener.onRealtimeChange(train);
            });
            this.priority = priority;
        }

        @Override
        public Map<String, String> getHeaders() {
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "OrarioTreni/" + apiVer + " Android");
            headers.put("Authorization", getAuthorization());
            return headers;
        }

        @Override
        public Priority getPriority() {
            return priority;
        }

        private String getAuthorization() {
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(Base64.decode(secret, Base64.NO_WRAP), "HmacSHA256"));
                long millis = System.currentTimeMillis();
                String value = String.format(Locale.ITALY, "GET%s%d", new URL(getUrl()).getPath(), millis);
                return String.format(Locale.ITALY, "HMAC=%d:%s", millis, Base64.encodeToString(mac.doFinal(value.getBytes()), Base64.NO_WRAP));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
