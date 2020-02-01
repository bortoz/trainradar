package it.trainradar.view;

import android.app.SharedElementCallback;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;

import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;
import androidx.viewpager.widget.ViewPager;
import it.trainradar.R;
import it.trainradar.core.Train;
import it.trainradar.manager.TrainDelayManager;
import it.trainradar.view.util.TrainPagerAdapter;

import static it.trainradar.view.util.TrainPagerAdapter.PAGE_MAP;
import static it.trainradar.view.util.TrainPagerAdapter.PAGE_ROUTE;

public class TrainActivity extends AppCompatActivity implements OnRefreshListener {
    private Train train;
    private ViewPager viewPager;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);
        Toolbar toolbar = findViewById(R.id.toolbarTrain);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        train = (Train) intent.getSerializableExtra("train");

        refreshLayout = findViewById(R.id.swipeRefreshLayout);
        refreshLayout.setColorSchemeColors(getColor(R.color.colorPrimary), getColor(R.color.colorAccent));
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setEnabled(false);

        viewPager = findViewById(R.id.trainViewPager);
        viewPager.setAdapter(new TrainPagerAdapter(this, train));
        viewPager.setCurrentItem(intent.getIntExtra("initTab", 0));

        TabLayout tabLayout = findViewById(R.id.trainTabLayout);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.getTabAt(PAGE_ROUTE).setText(getString(R.string.title_train_route_tab));
            tabLayout.getTabAt(PAGE_MAP).setText(getString(R.string.title_train_map_tab));
        }

        postponeEnterTransition();
        viewPager.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                viewPager.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return true;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        TrainMapFragment mapFragment = (TrainMapFragment) getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.trainViewPager + ":" + PAGE_MAP); // workaround
        if (mapFragment != null) {
            data.putExtra("exitCamera", mapFragment.getCameraPosition());
        }
        setResult(RESULT_OK, data);
        if (viewPager.getCurrentItem() != getIntent().getIntExtra("initTab", 0)) {
            setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    super.onMapSharedElements(names, sharedElements);
                    names.clear();
                    sharedElements.clear();
                }
            });
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_train, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            refreshLayout.setRefreshing(true);
            new Handler().postDelayed(this::onRefresh, 300);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        TrainDelayManager.forceRequestDelay(train, (t, d) -> refreshLayout.setRefreshing(false));
    }
}