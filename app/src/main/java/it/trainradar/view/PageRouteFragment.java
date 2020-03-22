package it.trainradar.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator;

import com.ferfalk.simplesearchview.utils.DimensUtils;
import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import it.trainradar.R;
import it.trainradar.core.Realtime;
import it.trainradar.core.Train;
import it.trainradar.manager.TimeManager;
import it.trainradar.manager.TrainDelayManager;
import it.trainradar.manager.TrainManager;
import it.trainradar.view.custom.CustomNestedScrollView;
import it.trainradar.view.util.PageFragment;
import it.trainradar.view.util.StopAdapter;

public class PageRouteFragment extends PageFragment {
    private TextView lblTrainName;
    private TextView lblTrainDelay;
    private TextView lblTrainStDeparture;
    private TextView lblTrainStArrival;
    private TextView lblTrainDeparture;
    private TextView lblTrainArrival;
    private CardView cardDescription;
    private CardView cardList;
    private SeekBar seekBar;
    private ToolTipsManager toolTipsManager;
    private ToolTip.Builder tooltipBuilder;
    private CustomNestedScrollView nestedScrollView;
    private StopAdapter stopAdapter;
    private ArgbEvaluator argbEvaluator;
    private int colorBackground;
    private int colorBackgroundDark;

    public PageRouteFragment() {
        super(0, R.layout.page_route);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        lblTrainName = view.findViewById(R.id.lblTrainName);
        lblTrainDelay = view.findViewById(R.id.lblTrainDelay);
        lblTrainStDeparture = view.findViewById(R.id.lblTrainStDeparture);
        lblTrainStArrival = view.findViewById(R.id.lblTrainStArrival);
        lblTrainDeparture = view.findViewById(R.id.lblTrainDeparture);
        lblTrainArrival = view.findViewById(R.id.lblTrainArrival);
        cardDescription = view.findViewById(R.id.cardDescription);
        cardList = view.findViewById(R.id.cardList);
        seekBar = view.findViewById(R.id.seekBar);
        nestedScrollView = view.findViewById(R.id.scrollView);

        toolTipsManager = new ToolTipsManager();
        tooltipBuilder = new ToolTip.Builder(getContext(), seekBar,
                view.findViewById(R.id.layoutDescription),
                "",
                ToolTip.POSITION_ABOVE)
                .setBackgroundColor(getContext().getColor(R.color.colorTooltip));

        GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (toolTipsManager.find(R.id.seekBar) == null) {
                    toolTipsManager.show(tooltipBuilder.build());
                } else {
                    toolTipsManager.findAndDismiss(seekBar);
                }
                return true;
            }
        });
        seekBar.setOnTouchListener((v, ev) -> {
            gestureDetector.onTouchEvent(ev);
            return true;
        });

        stopAdapter = new StopAdapter();
        RecyclerView recyclerView = view.findViewById(R.id.listStops);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(stopAdapter);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        argbEvaluator = new ArgbEvaluator();
        colorBackground = getContext().getColor(R.color.colorBackground);
        colorBackgroundDark = getContext().getColor(R.color.colorBackgroundDark);
    }

    @Override
    public void onTrainChange(Train train) {
        toolTipsManager.findAndDismiss(seekBar);
        stopAdapter.setTrain(train);

        lblTrainName.setText(TrainManager.getFormattedName(train));
        lblTrainStDeparture.setText(train.getDepartureStation().getName());
        lblTrainStArrival.setText(train.getArrivalStation().getName());
        lblTrainDeparture.setText(train.getDepartureTime().format(DateTimeFormatter.ofPattern(getString(R.string.time_format))));
        lblTrainArrival.setText(train.getArrivalTime().format(DateTimeFormatter.ofPattern(getString(R.string.time_format))));

        LocalTime time = TimeManager.now();
        Realtime realtime = TrainDelayManager.getRealtime(train);
        if (realtime != null && realtime.getDelay() != null) {
            time = time.minus(Duration.ofMinutes(realtime.getDelay()));
            updateDelay(realtime);
        } else {
            lblTrainDelay.setText("");
        }
        float progress = 1f * TimeManager.timeDiff(train.getDepartureTime(), time) / TimeManager.timeDiff(train.getDepartureTime(), train.getArrivalTime());
        seekBar.setProgress((int) (100 * progress));

        int eta = TimeManager.timeDiff(time, train.getArrivalTime()) / 60;
        float seekBarWidth = seekBar.getWidth() - DimensUtils.convertDpToPx(32f, getContext());

        try {
            if (eta < 60) {
                FieldUtils.writeField(tooltipBuilder, "mMessage", getString(R.string.minute_eta_label, eta), true);
            } else {
                FieldUtils.writeField(tooltipBuilder, "mMessage", getString(R.string.hour_eta_label, eta / 60, eta % 60), true);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (progress < 0.2) {
            tooltipBuilder.setAlign(ToolTip.ALIGN_LEFT);
            tooltipBuilder.setOffsetX((int) (progress * seekBarWidth));
        } else if (progress > 0.8) {
            tooltipBuilder.setAlign(ToolTip.ALIGN_RIGHT);
            tooltipBuilder.setOffsetX((int) ((progress - 1) * seekBarWidth));
        } else {
            tooltipBuilder.setAlign(ToolTip.ALIGN_CENTER);
            tooltipBuilder.setOffsetX((int) ((progress - .5) * seekBarWidth));
        }
    }

    @Override
    public void onRealtimeChange(Train train) {
        Realtime realtime = train.getRealtime();
        if (train.equals(this.train)) {
            if (realtime != null) {
                updateDelay(realtime);
            } else {
                lblTrainDelay.setText("");
            }
        }
    }

    public void onSlide(float slideOffset) {
        float fraction = Math.max(slideOffset, 0);
        float elevation = DimensUtils.convertDpToPx(2f * fraction, getContext());
        cardDescription.setCardElevation(elevation);
        cardList.setCardElevation(elevation);
        nestedScrollView.setBackgroundColor((int) argbEvaluator.evaluate(fraction, colorBackground, colorBackgroundDark));
    }

    public boolean isScrolling() {
        return nestedScrollView.isScrolling();
    }

    public void resetScrolling() {
        nestedScrollView.scrollTo(0, 0);
    }

    public int getPeekHeight() {
        ViewGroup.MarginLayoutParams cardParams = (ViewGroup.MarginLayoutParams) cardDescription.getLayoutParams();
        return cardDescription.getHeight() + cardParams.topMargin + cardParams.bottomMargin;
    }

    private void updateDelay(Realtime realtime) {
        SpannableString delayFormat;
        if (realtime == null || realtime.getDelay() == null) {
            delayFormat = new SpannableString("");
        } else if (realtime.isCancelled()) {
            delayFormat = new SpannableString(getContext().getString(R.string.cancelled));
            delayFormat.setSpan(new ForegroundColorSpan(getContext().getColor(R.color.train_delay)), 0, delayFormat.length(), 0);
        } else {
            int delay = realtime.getDelay();
            if (delay > 0) {
                delayFormat = new SpannableString(getContext().getString(R.string.delay_message_compact, delay));
                delayFormat.setSpan(new ForegroundColorSpan(getContext().getColor(R.color.train_delay)), 0, delayFormat.length(), 0);
            } else {
                delayFormat = new SpannableString(getContext().getString(R.string.advance_message_compact, delay));
                delayFormat.setSpan(new ForegroundColorSpan(getContext().getColor(R.color.train_advance)), 0, delayFormat.length(), 0);
            }
        }
        lblTrainDelay.setText(delayFormat);
    }
}