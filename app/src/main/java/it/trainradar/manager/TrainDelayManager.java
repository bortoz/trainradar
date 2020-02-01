package it.trainradar.manager;

import android.content.Context;

import com.android.volley.Request.Priority;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.trainradar.core.Train;

public class TrainDelayManager implements Manager {
    public final static long MAX_CACHING_MILLIS = 5 * 60 * 1000;

    private static Map<String, Integer> cache;
    private static Map<String, Long> ttl;
    private static RequestQueue queue;
    private static List<UpdateDelayListener> listeners;

    public static void load(Context context) {
        cache = new HashMap<>();
        ttl = new HashMap<>();
        queue = Volley.newRequestQueue(context);
        listeners = new ArrayList<>();
    }

    public static Integer getDelay(Train train) {
        return cache.get(train.getName());
    }

    public static void requestDelay(Train train, Priority priority) {
        Long cacheTTL = ttl.get(train.getName());
        if (cacheTTL == null || cacheTTL + MAX_CACHING_MILLIS < TimeManager.timestamp()) {
            TrainStatusRequest request = new TrainStatusRequest(train,
                    priority,
                    response -> {
                        try {
                            int delay = response.getInt("ritardo");
                            cache.put(train.getName(), delay);
                            ttl.put(train.getName(), TimeManager.timestamp());
                            for (UpdateDelayListener listener : listeners) {
                                listener.onUpdateDelay(train, delay);
                            }
                        } catch (JSONException e) {
                            ttl.put(train.getName(), TimeManager.timestamp());
                        }
                    },
                    error -> ttl.put(train.getName(), TimeManager.timestamp()));
            queue.add(request);
        }
    }

    public static void forceRequestDelay(Train train, UpdateDelayListener callback) {
        TrainStatusRequest request = new TrainStatusRequest(train,
                Priority.IMMEDIATE,
                response -> {
                    try {
                        int delay = response.getInt("ritardo");
                        cache.put(train.getName(), delay);
                        ttl.put(train.getName(), TimeManager.timestamp());
                        callback.onUpdateDelay(train, delay);
                    } catch (JSONException e) {
                        ttl.put(train.getName(), TimeManager.timestamp());
                        callback.onUpdateDelay(train, null);
                    }
                },
                error -> {
                    ttl.put(train.getName(), TimeManager.timestamp());
                    callback.onUpdateDelay(train, null);
                });
        queue.add(request);
    }

    public static void addUpdateDelayListener(UpdateDelayListener listener) {
        listeners.add(listener);
    }

    public static void removeUpdateDelayListener(UpdateDelayListener listener) {
        listeners.remove(listener);
    }

    public interface UpdateDelayListener {
        void onUpdateDelay(Train train, Integer delay);
    }

    private static class TrainStatusRequest extends JsonObjectRequest {
        private final static String BASE_URL = "http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/andamentoTreno/%s/%d";

        private final Priority priority;

        private TrainStatusRequest(Train train, Priority priority, Response.Listener<JSONObject> listener, Response.ErrorListener error) {
            super(String.format(Locale.ITALY, BASE_URL, train.getIdDeparture(), train.getId()), null, listener, error);
            this.priority = priority;
            this.setTag(train.getName());
        }

        @Override
        public Priority getPriority() {
            return priority;
        }
    }
}
