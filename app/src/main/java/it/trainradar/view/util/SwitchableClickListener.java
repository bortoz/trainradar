package it.trainradar.view.util;

import android.view.View.OnClickListener;

public abstract class SwitchableClickListener implements OnClickListener {
    private boolean enabled = true;

    protected boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
