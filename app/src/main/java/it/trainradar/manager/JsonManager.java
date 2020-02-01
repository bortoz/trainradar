package it.trainradar.manager;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.stream.Collectors;

abstract public class JsonManager implements Manager {
    protected final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>) (json, type, jsonDeserializationContext) -> LocalTime.parse(json.getAsString()))
            .create();

    protected static String getRawResources(Context context, int id) {
        return new BufferedReader(new InputStreamReader(context.getResources().openRawResource(id))).lines().collect(Collectors.joining("\n"));
    }
}
