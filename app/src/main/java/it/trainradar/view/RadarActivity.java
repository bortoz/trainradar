package it.trainradar.view;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.ferfalk.simplesearchview.utils.DimensUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

import it.trainradar.R;
import it.trainradar.core.Realtime;
import it.trainradar.core.Train;
import it.trainradar.manager.BaseManager;
import it.trainradar.manager.StationManager;
import it.trainradar.manager.TimeManager;
import it.trainradar.manager.TrainDelayManager;
import it.trainradar.manager.TrainManager;
import it.trainradar.view.custom.CustomBottomSheetBehavior;
import it.trainradar.view.custom.CustomBottomSheetCallback;
import it.trainradar.view.tracker.SchedulableRunnable.UpdateLevel;
import it.trainradar.view.tracker.TrackerManager;
import it.trainradar.view.util.AppUpdateChecker;
import it.trainradar.view.util.TrainPageAdapter;

public class RadarActivity extends AppCompatActivity implements OnMapReadyCallback,
        OnCameraIdleListener,
        OnInfoWindowClickListener,
        OnMarkerClickListener,
        OnMapClickListener,
        OnMyLocationClickListener,
        OnCameraMoveStartedListener,
        OnMapLoadedCallback {

    private final static LatLngBounds DEFAULT_BOUNDS = LatLngBounds.builder()
            .include(new LatLng(36.619987291, 6.7499552751))
            .include(new LatLng(47.1153931748, 18.4802470232))
            .build();
    private final static float DEFAULT_ZOOM = 5.3f;
    private final static int POSITION_REQUEST_CODE = 4279;
    private final static int SELECT_TRAIN_REQUEST_CODE = 9691;

    private GoogleMap googleMap;
    private MapView mapView;
    private Semaphore loadMapLock;
    private TrackerManager trackerManager;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLngBounds bounds;
    private ActionBar actionBar;
    private CustomBottomSheetBehavior<?> sheetBehavior;
    private CustomBottomSheetCallback sheetCallback;
    private TrainPageAdapter pageAdapter;
    private boolean isRefreshing;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // when restart activity mapView and viewPager have the same ids
        // WORKAROUND: drop some ids
        for (int i = 0; i < 100; i++) ViewCompat.generateViewId();

        setContentView(R.layout.activity_radar);
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.hide();

        mapView = findViewById(R.id.radarMap);
        mapView.onCreate(savedInstanceState);
        mapView.setAlpha(0);
        bounds = DEFAULT_BOUNDS;

        trackerManager = new TrackerManager();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        pageAdapter = new TrainPageAdapter(this, viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(getResources().getStringArray(R.array.tab_titles)[position])).attach();

        sheetBehavior = (CustomBottomSheetBehavior<?>) BottomSheetBehavior.from(viewPager);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                sheetBehavior.setEnable(state == ViewPager2.SCROLL_STATE_IDLE && pageAdapter.getCurrentPosition() == 0);
            }
        });

        sheetCallback = new CustomBottomSheetCallback() {
            private float m48dp = DimensUtils.convertDpToPx(48f, RadarActivity.this);
            private float m36dp = DimensUtils.convertDpToPx(36f, RadarActivity.this);

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                super.onStateChanged(bottomSheet, newState);
                invalidateOptionsMenu();
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    actionBar.setDisplayHomeAsUpEnabled(false);
                }
                pageAdapter.reset();
                viewPager.setUserInputEnabled(newState == BottomSheetBehavior.STATE_EXPANDED);
                tabLayout.setEnabled(newState == BottomSheetBehavior.STATE_EXPANDED);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                mapView.setTranslationY(-pageAdapter.getPeekHeight() * (Math.max(Math.min(slideOffset, 0), -1) + 1) / 2);
                pageAdapter.onSlide(slideOffset);
                float translation = 0.5f * (mapView.getHeight() - pageAdapter.getPeekHeight()) * (1 - slideOffset);
                appBarLayout.setTranslationY(-Math.min(translation, m48dp));
                toolbar.setTranslationY(Math.min(translation, m48dp));
                if (translation < m36dp) {
                    tabLayout.setVisibility(View.VISIBLE);
                    tabLayout.setAlpha(1 - translation / m36dp);
                } else {
                    tabLayout.setVisibility(View.INVISIBLE);
                }
            }
        };
        sheetBehavior.addBottomSheetCallback(sheetCallback);

        AppUpdateChecker.checkForUpdate(this, (version, uri) -> new AlertDialog.Builder(this)
                .setMessage(getString(R.string.update_message, version))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(i);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show());

        StationManager.load(this);
        TrainManager.load(this);
        TimeManager.load(this);
        TrainDelayManager.load(this);

        loadMapLock = new Semaphore(1);
        loadMapLock.acquireUninterruptibly();
        BaseManager.executeTask(() -> loadMapLock.acquireUninterruptibly());
        mapView.getMapAsync(this);

        BaseManager.waitAllTasks(() -> {
            trackerManager.onCreate(googleMap);
            trackerManager.setUpdateDelay(UpdateLevel.UPDATE_SLOW);
            onCameraIdle();
            mapView.animate()
                    .alpha(1)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        }
                    })
                    .start();
            actionBar.show();
        });
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (googleMap != null) {
            editor.putFloat("latitude", (float) googleMap.getCameraPosition().target.latitude);
            editor.putFloat("longitude", (float) googleMap.getCameraPosition().target.longitude);
            editor.putFloat("zoom", googleMap.getCameraPosition().zoom);
            editor.apply();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        trackerManager.onDestroy();
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
    public boolean onSupportNavigateUp() {
        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED && !pageAdapter.onBackPressed()) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            super.onBackPressed();
        } else if (sheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED && !pageAdapter.onBackPressed()) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_TRAIN_REQUEST_CODE && resultCode == RESULT_OK) {
            Train train = (Train) data.getSerializableExtra("train");
            moveToTrain(train);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemRefresh = menu.findItem(R.id.action_refresh);
        MenuItem itemPosition = menu.findItem(R.id.action_my_position);
        MenuItem itemSearch = menu.findItem(R.id.action_search);
        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            itemRefresh.setVisible(false);
            itemPosition.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            itemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        } else {
            itemRefresh.setVisible(true);
            itemPosition.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            itemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        if (isRefreshing) itemRefresh.setActionView(R.layout.progress_bar);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_radar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            if (!isRefreshing) {
                isRefreshing = true;
                invalidateOptionsMenu();
                TrainDelayManager.forceRequestRealtime(pageAdapter.getTrain(), t -> {
                    isRefreshing = false;
                    invalidateOptionsMenu();
                });
            }
            return true;
        } else if (item.getItemId() == R.id.action_my_position) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                Intent intent = new Intent(RadarActivity.this, NearTrainsActivity.class);
                                intent.putExtra("location", location);
                                startActivityForResult(intent, SELECT_TRAIN_REQUEST_CODE);
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
                        .setPositiveButton(getString(android.R.string.ok), (dialog, id) -> ActivityCompat.requestPermissions(RadarActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, POSITION_REQUEST_CODE))
                        .setNegativeButton(getString(android.R.string.cancel), null)
                        .create()
                        .show();
            }
            return true;
        } else if (item.getItemId() == R.id.action_search) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        Intent intent = new Intent(RadarActivity.this, NearTrainsActivity.class);
                        intent.putExtra("location", location);
                        intent.putExtra("search", true);
                        startActivityForResult(intent, SELECT_TRAIN_REQUEST_CODE);
                    });
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
        googleMap.setOnMapLoadedCallback(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMyLocationClickListener(this);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.position_request_message))
                        .setPositiveButton(getString(android.R.string.ok), (dialog, id) -> ActivityCompat.requestPermissions(RadarActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, POSITION_REQUEST_CODE))
                        .setNegativeButton(getString(android.R.string.cancel), null)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, POSITION_REQUEST_CODE);
            }
        }

        googleMap.setLatLngBoundsForCameraTarget(DEFAULT_BOUNDS);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        float latitude = sharedPref.getFloat("latitude", (float) DEFAULT_BOUNDS.getCenter().latitude);
        float longitude = sharedPref.getFloat("longitude", (float) DEFAULT_BOUNDS.getCenter().longitude);
        float zoom = sharedPref.getFloat("zoom", DEFAULT_ZOOM);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));

        loadMapLock.release();
    }

    @Override
    public void onMapLoaded() {
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            try {
                Uri uri = intent.getData();
                String[] parameters = uri.getPathSegments().get(0).split("_");
                String agency = parameters[0];
                String category = parameters[1];
                String name = String.join("/", Arrays.copyOfRange(parameters, 2, parameters.length));
                moveToTrain(TrainManager.getTrain(agency, category, name));
            } catch (Exception ignored) {
            }
        }
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
        marker.showInfoWindow();
        actionBar.setDisplayHomeAsUpEnabled(true);
        focusTrain((Train) marker.getTag());
        sheetCallback.setMarker(marker);
        return true;
    }

    @Override
    public void onMapClick(LatLng point) {
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Intent intent = new Intent(RadarActivity.this, NearTrainsActivity.class);
        intent.putExtra("location", location);
        startActivityForResult(intent, SELECT_TRAIN_REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == POSITION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
                googleMap.setOnMyLocationClickListener(this);
            }
        }
    }

    private void moveToTrain(Train train) {
        TrainDelayManager.forceRequestRealtime(train, realtimeTrain -> {
            Realtime realtime = train.getRealtime();
            LatLng pos = train.getPosition();
            if ((realtime == null || !realtime.isCancelled()) && pos != null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 12));
                Marker marker = trackerManager.forceShowTrain(train);
                onMarkerClick(marker);
            } else {
                focusTrain(train);
            }
        });
    }

    private void focusTrain(Train train) {
        pageAdapter.setTrain(train);
        sheetBehavior.setPeekHeight(pageAdapter.getPeekHeight());
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
}
