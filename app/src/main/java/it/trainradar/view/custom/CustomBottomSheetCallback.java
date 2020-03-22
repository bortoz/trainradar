package it.trainradar.view.custom;

import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public abstract class CustomBottomSheetCallback extends BottomSheetBehavior.BottomSheetCallback {
    private Marker marker;

    @CallSuper
    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN && marker != null) {
            marker.hideInfoWindow();
        }
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
