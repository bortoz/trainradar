package it.trainradar.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.ref.WeakReference;

import it.trainradar.view.util.TrainPageAdapter;

public class CustomBottomSheetBehavior<V extends View> extends BottomSheetBehavior<V> {
    private boolean enable = true;

    public CustomBottomSheetBehavior() {
        super();
    }

    public CustomBottomSheetBehavior(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
        return enable && super.onInterceptTouchEvent(parent, child, event);
    }

    @Override
    public boolean onTouchEvent(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
        return enable && super.onTouchEvent(parent, child, event);
    }

    private View findScrollingChild(View view) {
        if (view == null) return null;
        if (ViewCompat.isNestedScrollingEnabled(view)) {
            return view;
        }
        if (view instanceof ViewPager2) {
            ViewPager2 viewPager = (ViewPager2) view;
            RecyclerView.Adapter<?> adapter = viewPager.getAdapter();
            if (adapter instanceof TrainPageAdapter) {
                Fragment fragment = ((TrainPageAdapter) adapter).getCurrentFragment();
                return fragment == null ? null : findScrollingChild(fragment.getView());
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0, count = group.getChildCount(); i < count; i++) {
                View scrollingChild = findScrollingChild(group.getChildAt(i));
                if (scrollingChild != null) {
                    return scrollingChild;
                }
            }
        }
        return null;
    }

    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull V child, int layoutDirection) {
        super.onLayoutChild(parent, child, layoutDirection);
        WeakReference<View> nestedScrollingChild = new WeakReference<>(findScrollingChild(child));
        try {
            FieldUtils.writeField(this, "nestedScrollingChildRef", nestedScrollingChild, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
