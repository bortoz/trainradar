package it.trainradar.view.util;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import it.trainradar.R;
import it.trainradar.core.Realtime;
import it.trainradar.core.Stop;
import it.trainradar.core.Train;
import it.trainradar.manager.TimeManager;
import it.trainradar.manager.TrainDelayManager;

public class StopAdapterViewHolder extends RecyclerView.ViewHolder {
    private ConstraintLayout layout;
    private int colorEnable;
    private int colorDisable;
    private String timeFormat;
    private TextView lblStation;
    private TextView lblArrival;
    private TextView lblDeparture;
    private FrameLayout dot1;
    private FrameLayout dot2;
    private FrameLayout dot3;
    private FrameLayout dot4;
    private FrameLayout dot5;
    private FrameLayout dot6;
    private RadioButton radio;
    private GradientDrawable shapeDot1;
    private GradientDrawable shapeDot2;
    private GradientDrawable shapeDot3;
    private GradientDrawable shapeDot4;
    private GradientDrawable shapeDot5;
    private GradientDrawable shapeDot6;

    public StopAdapterViewHolder(ConstraintLayout layout) {
        super(layout);
        this.layout = layout;

        colorEnable = layout.getContext().getColor(R.color.colorPrimary);
        colorDisable = layout.getContext().getColor(R.color.colorDisable);
        timeFormat = layout.getContext().getString(R.string.time_format);

        lblStation = layout.findViewById(R.id.lblItemStation);
        lblArrival = layout.findViewById(R.id.lblItemArrival);
        lblDeparture = layout.findViewById(R.id.lblItemDeparture);

        dot1 = layout.findViewById(R.id.dot1);
        dot2 = layout.findViewById(R.id.dot2);
        dot3 = layout.findViewById(R.id.dot3);
        dot4 = layout.findViewById(R.id.dot4);
        dot5 = layout.findViewById(R.id.dot5);
        dot6 = layout.findViewById(R.id.dot6);

        shapeDot1 = (GradientDrawable) dot1.getBackground();
        shapeDot2 = (GradientDrawable) dot2.getBackground();
        shapeDot3 = (GradientDrawable) dot3.getBackground();
        shapeDot4 = (GradientDrawable) dot4.getBackground();
        shapeDot5 = (GradientDrawable) dot5.getBackground();
        shapeDot6 = (GradientDrawable) dot6.getBackground();

        radio = this.layout.findViewById(R.id.radio);
        radio.setButtonTintList(new ColorStateList(
                new int[][]{{android.R.attr.state_checked}, {-android.R.attr.state_checked}},
                new int[]{colorEnable, colorDisable}
        ));
    }

    public void bind(Train train, int position) {
        Stop stop = train.getStops().get(position);
        if (stop != null) {
            LocalTime time = TimeManager.now();
            Realtime realtime = TrainDelayManager.getRealtime(train);
            if (realtime != null && realtime.getDelay() != null) {
                time = time.minus(Duration.ofMinutes(realtime.getDelay()));
            }

            lblStation.setText(stop.getStation().getName());
            disableStop();

            if (stop.equals(train.getStops().get(0))) {
                hideUpperDots();
                lblArrival.setText("");
                enableStop();
            } else {
                Stop prevStop = train.getStops().get(position - 1);
                showUpperDots(0);
                lblArrival.setText(stop.getArrival().format(DateTimeFormatter.ofPattern(timeFormat)));
                if (TimeManager.isTimeOnInterval(stop.getArrival(), train.getDepartureTime(), time)) {
                    enableStop();
                    showUpperDots(3);
                } else if (TimeManager.isTimeOnInterval(time, prevStop.getDeparture(), stop.getArrival())) {
                    int diff1 = TimeManager.timeDiff(prevStop.getDeparture(), time);
                    int diff2 = TimeManager.timeDiff(prevStop.getDeparture(), stop.getArrival());
                    showUpperDots(Integer.max(5 * diff1 / diff2, 2) - 2);
                }
            }

            if (stop.equals(train.getStops().get(train.getStops().size() - 1))) {
                hideLowerDots();
                lblDeparture.setText("");
            } else {
                Stop nextStop = train.getStops().get(position + 1);
                showLowerDots(0);
                lblDeparture.setText(stop.getDeparture().format(DateTimeFormatter.ofPattern(timeFormat)));
                if (TimeManager.isTimeOnInterval(nextStop.getArrival(), train.getDepartureTime(), time)) {
                    showLowerDots(3);
                } else if (TimeManager.isTimeOnInterval(time, stop.getDeparture(), nextStop.getArrival())) {
                    int diff1 = TimeManager.timeDiff(stop.getDeparture(), time);
                    int diff2 = TimeManager.timeDiff(stop.getDeparture(), nextStop.getArrival());
                    showLowerDots(Integer.min(5 * diff1 / diff2, 2) + 1);
                }
            }
        }
    }

    private void showUpperDots(int count) {
        if (layout != null) {
            dot1.setVisibility(FrameLayout.VISIBLE);
            dot2.setVisibility(FrameLayout.VISIBLE);
            dot3.setVisibility(FrameLayout.VISIBLE);

            shapeDot1.setColor(count >= 1 ? colorEnable : colorDisable);
            shapeDot2.setColor(count >= 2 ? colorEnable : colorDisable);
            shapeDot3.setColor(count >= 3 ? colorEnable : colorDisable);
        }
    }

    private void hideUpperDots() {
        if (layout != null) {
            dot1.setVisibility(FrameLayout.INVISIBLE);
            dot2.setVisibility(FrameLayout.INVISIBLE);
            dot3.setVisibility(FrameLayout.INVISIBLE);
        }
    }

    private void showLowerDots(int count) {
        if (layout != null) {
            dot4.setVisibility(FrameLayout.VISIBLE);
            dot5.setVisibility(FrameLayout.VISIBLE);
            dot6.setVisibility(FrameLayout.VISIBLE);

            shapeDot4.setColor(count >= 1 ? colorEnable : colorDisable);
            shapeDot5.setColor(count >= 2 ? colorEnable : colorDisable);
            shapeDot6.setColor(count >= 3 ? colorEnable : colorDisable);
        }
    }

    private void hideLowerDots() {
        if (layout != null) {
            dot4.setVisibility(FrameLayout.INVISIBLE);
            dot5.setVisibility(FrameLayout.INVISIBLE);
            dot6.setVisibility(FrameLayout.INVISIBLE);
        }
    }

    private void enableStop() {
        if (layout != null) {
            radio.setChecked(true);
        }
    }

    private void disableStop() {
        if (layout != null) {
            radio.setChecked(false);
        }
    }
}
