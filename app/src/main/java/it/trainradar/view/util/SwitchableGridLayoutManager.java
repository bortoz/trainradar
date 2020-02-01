package it.trainradar.view.util;

import android.content.Context;

import androidx.recyclerview.widget.GridLayoutManager;

public class SwitchableGridLayoutManager extends GridLayoutManager {
    private boolean isScrollEnabled = true;

    public SwitchableGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public void setScrollEnabled(boolean enabled) {
        this.isScrollEnabled = enabled;
    }

    @Override
    public boolean canScrollVertically() {
        return isScrollEnabled && super.canScrollVertically();
    }
}
