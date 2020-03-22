package it.trainradar.core;

import java.io.Serializable;

public class Realtime implements Cloneable, Serializable {
    private boolean cancelled;
    private Integer minutes;

    public Realtime(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public Integer getDelay() {
        return minutes;
    }
}
