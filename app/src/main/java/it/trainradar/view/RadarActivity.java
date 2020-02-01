package it.trainradar.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request.Priority;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.trainradar.R;
import it.trainradar.core.Train;
import it.trainradar.manager.StationManager;
import it.trainradar.manager.TimeManager;
import it.trainradar.manager.TrainDelayManager;
import it.trainradar.manager.TrainManager;
import it.trainradar.manager.TrainRadarProvider;
import it.trainradar.view.tracker.TrainTrackerUpdater;
import it.trainradar.view.tracker.TrainTrackerUpdater.UpdateLevel;
import it.trainradar.view.util.TrainDetailFragmentManager;
import it.trainradar.view.util.TrainSearchAdapter;

public class RadarActivity extends AppCompatActivity implements OnMapReadyCallback,
        OnCameraIdleListener,
        OnInfoWindowClickListener,
        OnMarkerClickListener,
        OnMapClickListener,
        OnMyLocationClickListener,
        OnCameraMoveStartedListener {

    public final static LatLngBounds DEFAULT_BOUNDS = LatLngBounds.builder()
            .include(new LatLng(36.619987291, 6.7499552751))
            .include(new LatLng(47.1153931748, 18.4802470232))
            .build();
    public final static int POSITION_REQUEST_CODE = 42;

    private GoogleMap googleMap;
    private MapView mapView;
    private TrainTrackerUpdater trackerManager;
    private TrainDetailFragmentManager fragmentManager;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLngBounds bounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar);

        mapView = findViewById(R.id.radarMap);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        trackerManager = new TrainTrackerUpdater();
        fragmentManager = new TrainDetailFragmentManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Toolbar toolbar = findViewById(R.id.toolbarRadar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setOverflowIcon(getDrawable(R.drawable.ic_more_vert));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if (sharedPref.contains("radar_bounds")) {
            bounds = new Gson().fromJson(sharedPref.getString("radar_bounds", null), LatLngBounds.class);
        } else {
            bounds = DEFAULT_BOUNDS;
        }

        TrainRadarProvider.load(this, StationManager.class);
        TrainRadarProvider.load(this, TrainManager.class);
        TrainRadarProvider.load(this, TimeManager.class);
        TrainRadarProvider.load(this, TrainDelayManager.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        trackerManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        trackerManager.onPause();
        fragmentManager.remove();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("radar_bounds", new Gson().toJson(bounds));
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        trackerManager.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return fragmentManager.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (!fragmentManager.onSupportNavigateUp()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        CameraPosition camera = data.getParcelableExtra("exitCamera");
        if (camera != null && googleMap != null) {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camera));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_radar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_detail) {
            Train train = fragmentManager.getCurrentTrain();
            TrainDelayManager.requestDelay(train, Priority.IMMEDIATE);
            Intent intent = new Intent(this, TrainActivity.class);
            intent.putExtra("train", train);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_path) {
            Train train = fragmentManager.getCurrentTrain();
            TrainDelayManager.requestDelay(train, Priority.IMMEDIATE);
            Intent intent = new Intent(this, TrainActivity.class);
            intent.putExtra("train", train);
            intent.putExtra("initTab", 1);
            intent.putExtra("initCamera", googleMap.getCameraPosition());
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this, mapView, "mapTransition");
            startActivity(intent, options.toBundle());
            return true;
        } else if (item.getItemId() == R.id.action_my_position) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                Intent intent = new Intent(RadarActivity.this, NearTrainsActivity.class);
                                intent.putExtra("location", location);
                                startActivity(intent);
                            } else {
                                new AlertDialog.Builder(this)
                                        .setMessage(getString(R.string.position_error_message))
                                        .setPositiveButton(getString(android.R.string.ok), (dialog, id) -> {
                                        })
                                        .create()
                                        .show();
                            }
                        });
            } else {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.position_request_message))
                        .setPositiveButton(getString(android.R.string.yes), (dialog, id) -> ActivityCompat.requestPermissions(RadarActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, POSITION_REQUEST_CODE))
                        .setNegativeButton(getString(android.R.string.no), null)
                        .create()
                        .show();
            }
        } else if (item.getItemId() == R.id.action_search) {
            View dialogView = View.inflate(this, R.layout.dialog_search, null);

            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            EditText editText = dialogView.findViewById(R.id.txtSearchTrainNumber);
            RecyclerView recyclerView = dialogView.findViewById(R.id.listSearchTrain);
            ProgressBar spinner = dialogView.findViewById(R.id.searchSpinner);

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            TrainSearchAdapter adapter = new TrainSearchAdapter(v -> {
                Train train = (Train) v.getTag();
                spinner.setVisibility(ProgressBar.VISIBLE);
                spinner.setScaleX(0);
                spinner.setScaleY(0);
                spinner.animate()
                        .scaleX(1)
                        .scaleY(1)
                        .setDuration(600)
                        .withEndAction(() -> TrainDelayManager.forceRequestDelay(train, (tr, delay) -> {
                            spinner.animate()
                                    .scaleX(0)
                                    .scaleY(0)
                                    .setDuration(600)
                                    .withEndAction(() -> spinner.setVisibility(ProgressBar.GONE))
                                    .start();
                            LatLng pos = train.getPosition(TimeManager.now());
                            if (pos != null) {
                                alertDialog.dismiss();
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 12));
                                Marker marker = trackerManager.forceGetTracker(train).getMarker();
                                marker.showInfoWindow();
                                fragmentManager.addOrReplace(marker);
                            } else {
                                Toast.makeText(this, getString(R.string.not_found_message), Toast.LENGTH_SHORT).show();
                            }
                        }))
                        .start();
            });
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    adapter.setFilter(s.toString());
                }
            });
            editText.setOnEditorActionListener((textView, actionId, event) -> {
                Log.e("bortoz", "Action: " + actionId);
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                    case EditorInfo.IME_ACTION_NEXT:
                    case EditorInfo.IME_ACTION_SEARCH:
                    case EditorInfo.IME_ACTION_GO:
                    case EditorInfo.IME_ACTION_SEND:
                        View itemView = layoutManager.findViewByPosition(0);
                        if (itemView != null && ((Train) itemView.getTag()).getId() == Integer.parseInt(textView.getText().toString())) {
                            itemView.performClick();
                        } else {
                            Toast.makeText(this, getString(R.string.not_found_message), Toast.LENGTH_SHORT).show();
                        }
                        return true;
                }
                return false;
            });

            alertDialog.setOnShowListener(dialog -> {
                editText.setOnFocusChangeListener((v, hasFocus) -> editText.post(() -> {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }));
                editText.requestFocus();
            });
            alertDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        UiSettings ui = googleMap.getUiSettings();
        ui.setRotateGesturesEnabled(false);
        ui.setTiltGesturesEnabled(false);
        ui.setMapToolbarEnabled(false);
        ui.setMyLocationButtonEnabled(false);

        googleMap.setOnCameraIdleListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(this);
        googleMap.setOnCameraMoveStartedListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMyLocationClickListener(this);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.position_request_message))
                        .setPositiveButton(getString(android.R.string.yes), (dialog, id) -> ActivityCompat.requestPermissions(RadarActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, POSITION_REQUEST_CODE))
                        .setNegativeButton(getString(android.R.string.no), null)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, POSITION_REQUEST_CODE);
            }
        }

        googleMap.setLatLngBoundsForCameraTarget(DEFAULT_BOUNDS);
        mapView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mapView.getViewTreeObserver().removeOnPreDrawListener(this);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
                return true;
            }
        });

        trackerManager.onCreate(googleMap);
        trackerManager.setUpdateDelay(UpdateLevel.UPDATE_SLOW);
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == OnCameraMoveStartedListener.REASON_GESTURE) {
            trackerManager.setUpdateDelay(UpdateLevel.UPDATE_FAST);
        }
    }

    @Override
    public void onCameraIdle() {
        bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
        trackerManager.setUpdateDelay(UpdateLevel.UPDATE_SLOW);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        fragmentManager.addOrReplace(marker);
        return false;
    }

    @Override
    public void onMapClick(LatLng point) {
        fragmentManager.remove();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Train train = (Train) marker.getTag();
        TrainDelayManager.requestDelay(train, Priority.IMMEDIATE);
        Intent intent = new Intent(this, TrainActivity.class);
        intent.putExtra("train", train);
        startActivity(intent);
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Intent intent = new Intent(RadarActivity.this, NearTrainsActivity.class);
        intent.putExtra("location", location);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == POSITION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
                googleMap.setOnMyLocationClickListener(this);
            }
        }
    }
}
