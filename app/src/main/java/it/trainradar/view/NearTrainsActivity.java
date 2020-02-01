package it.trainradar.view;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.android.volley.Request.Priority;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;
import it.trainradar.R;
import it.trainradar.core.Train;
import it.trainradar.manager.TrainDelayManager;
import it.trainradar.view.util.SwitchableClickListener;
import it.trainradar.view.util.SwitchableGridLayoutManager;
import it.trainradar.view.util.TrainAdapter;

public class NearTrainsActivity extends AppCompatActivity {
    private SwitchableClickListener clickListener;
    private SwitchableGridLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_trains);
        Toolbar toolbar = findViewById(R.id.toolbarTrain);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        Location location = intent.getParcelableExtra("location");

        layoutManager = new SwitchableGridLayoutManager(this, getResources().getInteger(R.integer.near_trains_columns));
        RecyclerView recyclerView = findViewById(R.id.listTrains);
        recyclerView.setLayoutManager(layoutManager);

        clickListener = new SwitchableClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnabled()) {
                    Train train = (Train) v.getTag();
                    TrainDelayManager.requestDelay(train, Priority.IMMEDIATE);
                    Intent intent = new Intent(NearTrainsActivity.this, TrainActivity.class);
                    intent.putExtra("train", train);
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(NearTrainsActivity.this, v, "trainDetail");
                    startActivity(intent, options.toBundle());
                    layoutManager.setScrollEnabled(false);
                    setEnabled(false);
                }
            }
        };
        TrainAdapter adapter = new TrainAdapter(location, clickListener);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        clickListener.setEnabled(true);
        layoutManager.setScrollEnabled(true);
    }
}
