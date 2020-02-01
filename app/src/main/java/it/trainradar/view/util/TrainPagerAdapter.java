package it.trainradar.view.util;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import it.trainradar.R;
import it.trainradar.core.Train;
import it.trainradar.view.TrainMapFragment;
import it.trainradar.view.TrainRouteFragment;

public class TrainPagerAdapter extends FragmentPagerAdapter {
    public final static int PAGE_ROUTE = 0;
    public final static int PAGE_MAP = 1;

    private final Train train;
    private final int[] pageWidth;

    public TrainPagerAdapter(FragmentActivity activity, Train train) {
        super(activity.getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.train = train;
        this.pageWidth = activity.getResources().getIntArray(R.array.train_page_width);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == PAGE_ROUTE) {
            return new TrainRouteFragment(train);
        } else if (position == PAGE_MAP) {
            return new TrainMapFragment(train);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public float getPageWidth(int position) {
        return 1f / pageWidth[position];
    }
}
