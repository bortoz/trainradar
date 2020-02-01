package it.trainradar.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import it.trainradar.R;
import it.trainradar.core.Station;
import it.trainradar.core.Train;
import it.trainradar.manager.StationManager;
import it.trainradar.manager.TimeManager;
import it.trainradar.manager.TrainDelayManager;
import it.trainradar.manager.TrainDelayManager.UpdateDelayListener;

public class TrainMapFragment extends Fragment implements OnMapReadyCallback,
        OnMapClickListener,
        OnMarkerClickListener,
        OnMapLoadedCallback,
        UpdateDelayListener {
    public final static int MAP_PADDING = 128;

    private Train train;
    private MapView mapView;
    private GoogleMap googleMap;
    private Bitmap trainIcon;
    private Marker trainMarker;
    private LatLngBounds bounds;

    public TrainMapFragment() {
        this.train = null;
    }

    public TrainMapFragment(Train train) {
        this.train = train;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Train savedTrain = (Train) savedInstanceState.getSerializable("train");
            if (savedTrain != null) train = savedTrain;
        }

        Drawable trainDrawable = inflater.getContext().getDrawable(R.drawable.ic_train);
        trainIcon = Bitmap.createBitmap(trainDrawable.getIntrinsicWidth(), trainDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(trainIcon);
        trainDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        trainDrawable.draw(canvas);

        TrainDelayManager.addUpdateDelayListener(this);

        return inflater.inflate(R.layout.fragment_train_map, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TrainDelayManager.removeUpdateDelayListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mapView = view.findViewById(R.id.pathMap);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mapView.getContext(), R.raw.map_style));

        UiSettings ui = googleMap.getUiSettings();
        ui.setRotateGesturesEnabled(false);
        ui.setTiltGesturesEnabled(false);
        ui.setMapToolbarEnabled(false);
        ui.setMyLocationButtonEnabled(false);
        ui.setAllGesturesEnabled(false);

        googleMap.setOnMapClickListener(this);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapLoadedCallback(this);

        LatLng pos = train.getPosition(TimeManager.now());
        if (pos == null) {
            pos = StationManager.getStation(train.getIdArrival()).getPosition();
        }
        trainMarker = googleMap.addMarker(new MarkerOptions()
                .position(pos)
                .title(train.getName())
                .icon(BitmapDescriptorFactory.fromBitmap(trainIcon))
                .zIndex(1));
        trainMarker.showInfoWindow();
        List<Station> stops = train.getStops()
                .stream()
                .map(stop -> StationManager.getStation(stop.getId()))
                .collect(Collectors.toList());
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (int i = 0; i < stops.size(); i++) {
            Station station = stops.get(i);
            googleMap.addMarker(new MarkerOptions()
                    .position(station.getPosition())
                    .title(station.getName()));
            if (i > 0) {
                Station prevStation = stops.get(i - 1);
                List<Station> path = StationManager.getPath(prevStation.getId(), station.getId());
                googleMap.addPolyline(new PolylineOptions().add(path.stream()
                        .map(Station::getPosition)
                        .toArray(LatLng[]::new)))
                        .getPoints()
                        .forEach(boundsBuilder::include);
            }
        }

        bounds = boundsBuilder.build();
        Intent intent = getActivity().getIntent();
        if (intent.hasExtra("initCamera")) {
            CameraPosition camera = intent.getParcelableExtra("initCamera");
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camera));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, mapView.getWidth(), mapView.getHeight(), MAP_PADDING));
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        trainMarker.showInfoWindow();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        trainMarker.hideInfoWindow();
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onMapLoaded() {
        if (!googleMap.getProjection().getVisibleRegion().latLngBounds.equals(bounds)) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, mapView.getWidth(), mapView.getHeight(), MAP_PADDING));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        updateTrainPosition();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
        outState.putSerializable("train", train);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onUpdateDelay(Train train, Integer delay) {
        updateTrainPosition();
    }

    public CameraPosition getCameraPosition() {
        return googleMap.getCameraPosition();
    }

    private void updateTrainPosition() {
        if (trainMarker != null) {
            LatLng pos = train.getPosition(TimeManager.now());
            if (pos == null) {
                pos = StationManager.getStation(train.getIdArrival()).getPosition();
            }
            trainMarker.setPosition(pos);
        }
    }
}
