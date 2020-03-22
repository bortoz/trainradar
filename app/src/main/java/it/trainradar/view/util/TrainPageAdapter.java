package it.trainradar.view.util;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;
import java.util.stream.Collectors;

import it.trainradar.core.Train;
import it.trainradar.view.PageMapFragment;
import it.trainradar.view.PageRouteFragment;

public class TrainPageAdapter extends FragmentStateAdapter {
    private FragmentManager fragmentManager;
    private Train lastTrain;
    private Train train;
    private int peekHeight;
    private ViewPager2 viewPager;
    private int currentPosition;
    private boolean scrolling;

    public TrainPageAdapter(@NonNull FragmentActivity fragmentActivity, ViewPager2 viewPager) {
        super(fragmentActivity);
        fragmentManager = fragmentActivity.getSupportFragmentManager();
        this.viewPager = viewPager;
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(this);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                scrolling = (state != ViewPager.SCROLL_STATE_IDLE);
            }
        });
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PageRouteFragment();
            case 1:
                return new PageMapFragment();
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    public List<PageFragment> getAllPages() {
        return fragmentManager.getFragments()
                .stream()
                .filter(p -> p instanceof PageFragment)
                .map(p -> (PageFragment) p)
                .collect(Collectors.toList());
    }

    public PageFragment getPage(int position) {
        return getAllPages().stream()
                .filter(p -> p.getPosition() == position)
                .findAny()
                .orElse(null);
    }

    public Fragment getCurrentFragment() {
        return getPage(currentPosition);
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
        getAllPages().forEach(p -> p.setTrain(train));
    }

    public void onSlide(float slideOffset) {
        PageRouteFragment pageRoute = (PageRouteFragment) getPage(0);
        pageRoute.onSlide(slideOffset);
    }

    public int getPeekHeight() {
        if (lastTrain != train) {
            PageRouteFragment pageRoute = (PageRouteFragment) getPage(0);
            peekHeight = pageRoute.getPeekHeight();
            lastTrain = train;
        }
        return peekHeight;
    }

    public boolean onBackPressed() {
        if (scrolling) {
            return true;
        }
        if (currentPosition > 0) {
            viewPager.setCurrentItem(currentPosition - 1, true);
            return true;
        }
        PageRouteFragment pageRoute = (PageRouteFragment) getPage(0);
        return pageRoute.isScrolling();
    }

    public void reset() {
        viewPager.setCurrentItem(0, false);
        PageRouteFragment pageRoute = (PageRouteFragment) getPage(0);
        pageRoute.resetScrolling();
    }
}
