package it.trainradar.view.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class AppUpdateChecker {
    public static void checkForUpdate(Context context, NewUpdateCallback callback) {
        Double version;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = Double.parseDouble(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }
        JsonArrayRequest request = new JsonArrayRequest("https://api.github.com/repos/bortoz/trainradar/releases", response -> {
            try {
                for (int i = 0; i < response.length(); i++) {
                    JSONObject object = response.getJSONObject(i);
                    String newVersion = object.getString("tag_name").substring(1);
                    String url = object.getString("html_url");
                    if (Double.parseDouble(newVersion) > version) {
                        callback.onNewUpdate(newVersion, Uri.parse(url));
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
        }, null);
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    public interface NewUpdateCallback {
        void onNewUpdate(String version, Uri uri);
    }
}
