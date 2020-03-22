package it.trainradar.view;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

import com.ferfalk.simplesearchview.utils.DimensUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.stream.Collectors;

import it.trainradar.R;
import it.trainradar.core.Station;
import it.trainradar.core.Stop;
import it.trainradar.core.Train;
import it.trainradar.manager.StationManager;
import it.trainradar.manager.TrainManager;
import it.trainradar.view.util.PageFragment;

public class PageMapFragment extends PageFragment implements OnMapReadyCallback,
        OnMapClickListener,
        OnMarkerClickListener {
    private MapView mapView;
    private Marker trainMarker;
    private GoogleMap googleMap;

    public PageMapFragment() {
        super(1, R.layout.page_map);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(null);
        mapView.getMapAsync(this);
    }

    @Override
    public void onStart() {
        mapView.onStart();
        super.onStart();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        mapView.onStart();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        mapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        mapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mapView.getContext(), R.raw.map_style));

        UiSettings ui = googleMap.getUiSettings();
        ui.setMapToolbarEnabled(false);
        ui.setAllGesturesEnabled(false);

        googleMap.setOnMapClickListener(this);
        googleMap.setOnMarkerClickListener(this);

        if (train != null) onTrainChange(train);
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
    public void onTrainChange(Train train) {
        if (googleMap == null) return;

        googleMap.clear();

        LatLng pos = train.getPosition();
        if (pos == null) {
            pos = train.getArrivalStation().getPosition();
        }
        trainMarker = googleMap.addMarker(new MarkerOptions()
                .position(pos)
                .title(train.getName())
                .icon(TrainManager.getTrainIcon())
                .zIndex(1));
        trainMarker.showInfoWindow();
        List<Station> stops = train.getStops()
                .stream()
                .map(Stop::getStation)
                .collect(Collectors.toList());
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (int i = 0; i < stops.size(); i++) {
            Station station = stops.get(i);
            googleMap.addMarker(new MarkerOptions()
                    .position(station.getPosition())
                    .title(station.getName()));
            if (i > 0) {
                Station prevStation = stops.get(i - 1);
                List<Station> path = StationManager.getPath(prevStation, station);
                googleMap.addPolyline(new PolylineOptions().add(path.stream()
                        .map(Station::getPosition)
                        .toArray(LatLng[]::new)))
                        .getPoints()
                        .forEach(boundsBuilder::include);
            }
        }

        LatLngBounds bounds = boundsBuilder.build();
        mapView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mapView.getViewTreeObserver().removeOnPreDrawListener(this);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, DimensUtils.convertDpToPx(48, getContext())));
                return true;
            }
        });
    }

    @Override
    public void onRealtimeChange(Train train) {
        if (train.equals(this.train)) {
            LatLng pos = train.getPosition();
            if (pos != null) {
                trainMarker.setPosition(pos);
            } else {
                trainMarker.remove();
            }
        }
    }
}
