package it.trainradar.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

public class CustomNestedScrollView extends androidx.core.widget.NestedScrollView {
    private boolean scrolling = false;
    private boolean flinging = false;

    public CustomNestedScrollView(@NonNull Context context) {
        super(context);
    }

    public CustomNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        if (type == ViewCompat.TYPE_NON_TOUCH) flinging = true;
        else if (type == ViewCompat.TYPE_TOUCH) scrolling = true;
        return super.onStartNestedScroll(child, target, axes, type);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        if (type == ViewCompat.TYPE_NON_TOUCH) flinging = false;
        else if (type == ViewCompat.TYPE_TOUCH) scrolling = false;
        super.onStopNestedScroll(target, type);
    }

    public boolean isScrolling() {
        return scrolling || flinging;
    }
}
