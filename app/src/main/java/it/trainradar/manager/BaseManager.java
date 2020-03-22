package it.trainradar.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BaseManager {
    protected final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>) (json, type, jsonDeserializationContext) -> json.getAsString().isEmpty() ? null : LocalTime.parse(json.getAsString()))
            .create();
    private final static AtomicInteger pendingTask = new AtomicInteger(0);

    protected static String getRawResources(Context context, int id) {
        return new BufferedReader(new InputStreamReader(context.getResources().openRawResource(id))).lines().collect(Collectors.joining("\n"));
    }

    public static void executeTask(Runnable task) {
        new Thread(() -> {
            pendingTask.incrementAndGet();
            task.run();
            pendingTask.decrementAndGet();
            synchronized (pendingTask) {
                pendingTask.notifyAll();
            }
        }).start();
    }

    public static void waitAllTasks(Runnable callback) {
        new Thread(() -> {
            try {
                synchronized (pendingTask) {
                    while (pendingTask.get() > 0) {
                        pendingTask.wait();
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            new Handler(Looper.getMainLooper()).post(callback);
        }).start();
    }
}
