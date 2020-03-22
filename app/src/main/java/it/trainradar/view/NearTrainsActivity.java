package it.trainradar.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ferfalk.simplesearchview.SimpleOnQueryTextListener;
import com.ferfalk.simplesearchview.SimpleSearchViewListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrListenerAdapter;
import com.r0adkll.slidr.util.ViewDragHelper;

import org.apache.commons.lang3.ArrayUtils;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import it.trainradar.R;
import it.trainradar.core.Train;
import it.trainradar.manager.TrainManager;
import it.trainradar.view.custom.CustomSearchView;
import it.trainradar.view.util.TrainAdapter;

public class NearTrainsActivity extends AppCompatActivity {
    private TrainAdapter adapter;
    private TextView lblNoTrain;
    private CustomSearchView searchView;
    private SlidrInterface slidrInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_trains);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Location location = getIntent().getParcelableExtra("location");

        lblNoTrain = findViewById(R.id.lblNoTrain);
        RecyclerView recyclerView = findViewById(R.id.listTrains);
        recyclerView.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.columns_per_row)));

        adapter = new TrainAdapter(location, v -> {
            Train train = (Train) v.getTag();
            LatLng position = train.getPosition();
            if (position != null) {
                Intent intent = new Intent();
                intent.putExtra("train", train);
                setResult(RESULT_OK, intent);
                finish();
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            } else {
                Snackbar.make(recyclerView, R.string.not_left_message, Snackbar.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        adapter.setFilterCategories(sharedPref.getStringSet("categoryFilter", new HashSet<>()));

        if (adapter.getTrains().isEmpty()) {
            lblNoTrain.setVisibility(View.VISIBLE);
        }

        slidrInterface = Slidr.attach(this, new SlidrConfig.Builder()
                .listener(new SlidrListenerAdapter() {
                    @Override
                    public void onSlideStateChanged(int state) {
                        if (state == ViewDragHelper.STATE_DRAGGING) {
                            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        }
                    }

                    @Override
                    public void onSlideOpened() {
                        getWindow().setBackgroundDrawableResource(R.color.colorBackgroundDark);
                    }
                }).build());

        searchView = findViewById(R.id.searchView);
        searchView.setHint(getString(R.string.search_message));
        searchView.setOnQueryTextListener(new SimpleOnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter.getTrains().size() == 1) {
                    Train train = adapter.getTrains().get(0);
                    LatLng position = train.getPosition();
                    if (position != null) {
                        Intent intent = new Intent();
                        intent.putExtra("train", train);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        Snackbar.make(recyclerView, R.string.not_left_message, Snackbar.LENGTH_SHORT).show();
                    }
                } else if (adapter.getTrains().isEmpty()) {
                    Snackbar.make(recyclerView, R.string.not_found_message, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(recyclerView, R.string.many_train_message, Snackbar.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                lblNoTrain.setVisibility(adapter.setFilterID(newText.isEmpty() ? null : newText) ? View.INVISIBLE : View.VISIBLE);
                return false;
            }
        });
        searchView.setOnSearchViewListener(new SimpleSearchViewListener() {
            private String oldQuery = "";

            @Override
            public void onSearchViewShown() {
                slidrInterface.lock();
                searchView.setQuery(oldQuery, false);
            }

            @Override
            public void onSearchViewClosed() {
                slidrInterface.unlock();
                oldQuery = adapter.getFilterID();
            }
        });

        if (getIntent().getBooleanExtra("search", false)) {
            searchView.showSearch(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("categoryFilter", new HashSet<>(adapter.getFilterCategories()));
        editor.apply();
    }

    @Override
    public void onAttachedToWindow() {
        getWindow().setBackgroundDrawableResource(R.color.colorBackgroundDark);
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_near_trains, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            List<String> categories = TrainManager.getCategories();
            List<Boolean> checked = categories.stream()
                    .map(c -> adapter.getFilterCategories().contains(c))
                    .collect(Collectors.toList());
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.select_message)
                    .setMultiChoiceItems(categories.toArray(new String[0]),
                            ArrayUtils.toPrimitive(checked.toArray(new Boolean[0])),
                            (dialog, which, isChecked) -> checked.set(which, isChecked))
                    .setPositiveButton(R.string.filter, (dialog, which) -> lblNoTrain.setVisibility(
                            adapter.setFilterCategories(
                                    IntStream.range(0, categories.size())
                                            .filter(checked::get)
                                            .mapToObj(categories::get)
                                            .collect(Collectors.toSet())) ?
                                    View.GONE : View.VISIBLE))
                    .setNegativeButton(android.R.string.cancel, null)
                    .create()
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
}
