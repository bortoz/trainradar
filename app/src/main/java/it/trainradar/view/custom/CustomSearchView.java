package it.trainradar.view.custom;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.ferfalk.simplesearchview.SimpleSearchView;
import com.ferfalk.simplesearchview.utils.DimensUtils;

public class CustomSearchView extends SimpleSearchView {
    private boolean initRevealAnimation = false;

    public CustomSearchView(Context context) {
        super(context);
    }

    public CustomSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public Point getRevealAnimationCenter() {
        Point revealCenter = super.getRevealAnimationCenter();
        if (!initRevealAnimation) {
            revealCenter.x -= DimensUtils.convertDpToPx(48, getContext());
            initRevealAnimation = true;
        }
        return revealCenter;
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            closeSearch();
            return true;
        }
        return super.dispatchKeyEventPreIme(event);
    }
}
