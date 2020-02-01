package it.trainradar.view.util;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.widget.RadioButton;
import android.widget.TextView;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import it.trainradar.R;
import it.trainradar.core.Stop;
import it.trainradar.core.Train;
import it.trainradar.manager.StationManager;
import it.trainradar.manager.TimeManager;
import it.trainradar.manager.TrainDelayManager;

public class StopAdapterViewHolder extends RecyclerView.ViewHolder {
    private final ConstraintLayout layout;
    private final int colorEnable;
    private final int colorDisable;
    private final String timeFormat;

    public StopAdapterViewHolder(ConstraintLayout layout) {
        super(layout);
        this.layout = layout;
        this.colorEnable = layout.getContext().getColor(R.color.colorPrimary);
        this.colorDisable = layout.getContext().getColor(R.color.colorDisable);
        this.timeFormat = layout.getContext().getString(R.string.time_format);
        RadioButton radio = this.layout.findViewById(R.id.radio);
        radio.setButtonTintList(new ColorStateList(
                new int[][]{{android.R.attr.state_checked}, {-android.R.attr.state_checked}},
                new int[]{colorEnable, colorDisable}
        ));
    }

    public void bind(Train train, int position) {
        Stop stop = train.getStops().get(position);
        if (stop != null) {
            LocalTime time = TimeManager.now();
            Integer delay = TrainDelayManager.getDelay(train);
            if (delay != null) {
                time = time.minus(Duration.ofMinutes(delay));
            }

            TextView lblStation = layout.findViewById(R.id.lblItemStation);
            TextView lblArrival = layout.findViewById(R.id.lblItemArrival);
            TextView lblDeparture = layout.findViewById(R.id.lblItemDeparture);

            lblStation.setText(StationManager.getStation(stop.getId()).getName());
            disableStop();

            if (stop.getArrival() == null) {
                hideUpperDots();
                lblArrival.setText("");
                enableStop();
            } else {
                Stop prevStop = train.getStops().get(position - 1);
                showUpperDots(0);
                lblArrival.setText(stop.getArrival().format(DateTimeFormatter.ofPattern(timeFormat)));
                if (TimeManager.isTimeOnInterval(stop.getArrival(), train.getDeparture(), time)) {
                    enableStop();
                    showUpperDots(3);
                } else if (TimeManager.isTimeOnInterval(time, prevStop.getDeparture(), stop.getArrival())) {
                    int diff1 = TimeManager.timeDiff(prevStop.getDeparture(), time);
                    int diff2 = TimeManager.timeDiff(prevStop.getDeparture(), stop.getArrival());
                    showUpperDots(Integer.max(5 * diff1 / diff2, 2) - 2);
                }
            }

            if (stop.getDeparture() == null) {
                hideLowerDots();
                lblDeparture.setText("");
            } else {
                Stop nextStop = train.getStops().get(position + 1);
                showLowerDots(0);
                lblDeparture.setText(stop.getDeparture().format(DateTimeFormatter.ofPattern(timeFormat)));
                if (TimeManager.isTimeOnInterval(nextStop.getArrival(), train.getDeparture(), time)) {
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
            ConstraintLayout dot1 = layout.findViewById(R.id.dot1);
            ConstraintLayout dot2 = layout.findViewById(R.id.dot2);
            ConstraintLayout dot3 = layout.findViewById(R.id.dot3);

            dot1.setVisibility(ConstraintLayout.VISIBLE);
            dot2.setVisibility(ConstraintLayout.VISIBLE);
            dot3.setVisibility(ConstraintLayout.VISIBLE);

            GradientDrawable shapeDot1 = (GradientDrawable) dot1.getBackground();
            GradientDrawable shapeDot2 = (GradientDrawable) dot2.getBackground();
            GradientDrawable shapeDot3 = (GradientDrawable) dot3.getBackground();

            shapeDot1.setColor(count >= 1 ? colorEnable : colorDisable);
            shapeDot2.setColor(count >= 2 ? colorEnable : colorDisable);
            shapeDot3.setColor(count >= 3 ? colorEnable : colorDisable);
        }
    }

    private void hideUpperDots() {
        if (layout != null) {
            ConstraintLayout dot1 = layout.findViewById(R.id.dot1);
            ConstraintLayout dot2 = layout.findViewById(R.id.dot2);
            ConstraintLayout dot3 = layout.findViewById(R.id.dot3);

            dot1.setVisibility(ConstraintLayout.INVISIBLE);
            dot2.setVisibility(ConstraintLayout.INVISIBLE);
            dot3.setVisibility(ConstraintLayout.INVISIBLE);
        }
    }

    private void showLowerDots(int count) {
        if (layout != null) {
            ConstraintLayout dot5 = layout.findViewById(R.id.dot4);
            ConstraintLayout dot6 = layout.findViewById(R.id.dot5);
            ConstraintLayout dot7 = layout.findViewById(R.id.dot6);

            dot5.setVisibility(ConstraintLayout.VISIBLE);
            dot6.setVisibility(ConstraintLayout.VISIBLE);
            dot7.setVisibility(ConstraintLayout.VISIBLE);

            GradientDrawable shapeDot5 = (GradientDrawable) dot5.getBackground();
            GradientDrawable shapeDot6 = (GradientDrawable) dot6.getBackground();
            GradientDrawable shapeDot7 = (GradientDrawable) dot7.getBackground();

            shapeDot5.setColor(count >= 1 ? colorEnable : colorDisable);
            shapeDot6.setColor(count >= 2 ? colorEnable : colorDisable);
            shapeDot7.setColor(count >= 3 ? colorEnable : colorDisable);
        }
    }

    private void hideLowerDots() {
        if (layout != null) {
            ConstraintLayout dot5 = layout.findViewById(R.id.dot4);
            ConstraintLayout dot6 = layout.findViewById(R.id.dot5);
            ConstraintLayout dot7 = layout.findViewById(R.id.dot6);

            dot5.setVisibility(ConstraintLayout.INVISIBLE);
            dot6.setVisibility(ConstraintLayout.INVISIBLE);
            dot7.setVisibility(ConstraintLayout.INVISIBLE);
        }
    }

    private void enableStop() {
        if (layout != null) {
            RadioButton radio = layout.findViewById(R.id.radio);
            radio.setChecked(true);
        }
    }

    private void disableStop() {
        if (layout != null) {
            RadioButton radio = layout.findViewById(R.id.radio);
            radio.setChecked(false);
        }
    }
}
