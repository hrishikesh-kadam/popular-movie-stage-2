package com.example.android.popularmoviesstage2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.android.popularmoviesstage2.data.MovieContract.FavoriteMovieEntry;
import com.example.android.popularmoviesstage2.model.MoviesResponse;
import com.example.android.popularmoviesstage2.model.Result;
import com.example.android.popularmoviesstage2.rest.TmdbAPIV3;
import com.example.android.popularmoviesstage2.rest.TmdbApiKey;
import com.example.android.popularmoviesstage2.rest.TmdbRetrofit;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements GridAdapter.ItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener, Callback<MoviesResponse>,
        SwipeRefreshLayout.OnRefreshListener {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private TmdbAPIV3 tmdbAPIV3;
    private Call<MoviesResponse> popularResponseCall, topRatedResponseCall;
    private GridAdapter gridAdapter;
    private AlertDialog alertDialogNetwork, alertDialogKeyNotFound;
    private SharedPreferences sharedPreferences;
    private boolean isKeyEntered;
    private ArrayList<Result> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "-> onCreate");

        initAlertDialogs();

        isKeyEntered = checkKeyEntered();
        if (!isKeyEntered) {
            alertDialogKeyNotFound.show();
            return;
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        swipeRefreshLayout.setOnRefreshListener(this);

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.CENTER);
        recyclerView.setLayoutManager(layoutManager);

        tmdbAPIV3 = TmdbRetrofit.getRetrofit().create(TmdbAPIV3.class);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        if (savedInstanceState == null) {
            swipeRefreshLayout.setRefreshing(true);
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.settings_sort_key));
        }
    }

    private void initAlertDialogs() {
        Log.v(LOG_TAG, "-> initAlertDialogs");

        alertDialogKeyNotFound = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alert_dialog_title_attention))
                .setMessage(String.format(getString(R.string.alert_dialog_message_key), "String api_key", TmdbApiKey.class.getName()))
                .setPositiveButton(getString(R.string.ok), null)
                .create();

        alertDialogNetwork = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alert_dialog_title_attention))
                .setMessage(getString(R.string.alert_dialog_message_network))
                .setPositiveButton(getString(R.string.ok), null)
                .create();
    }

    private boolean checkKeyEntered() {
        Log.v(LOG_TAG, "-> checkIfKeyIsEntered");

        if (TmdbApiKey.api_key == null || TmdbApiKey.api_key.isEmpty()) {
            Log.e(LOG_TAG, "-> checkIfKeyIsEntered -> "
            + String.format(getString(R.string.alert_dialog_message_key), "String api_key", TmdbApiKey.class.getName()));
            return false;
        }

        return true;
    }

    @Override
    public void onItemClick(View imageView, int position) {
        Log.v(LOG_TAG, "-> onItemClick -> " + results.get(position).getOriginalTitle());

        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("movieResult", results.get(position));
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(LOG_TAG, "-> onOptionsItemSelected -> " + item.getTitle());

        switch (item.getItemId()) {

            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.deleteAll:
                getContentResolver().delete(
                        FavoriteMovieEntry.CONTENT_URI,
                        null, null);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.v(LOG_TAG, "-> onSharedPreferenceChanged");

        if (key.equals(getString(R.string.settings_sort_key))) {

            String sortPreference = sharedPreferences.getString(key, getString(R.string.settings_sort_default_value));
            Log.v(LOG_TAG, "-> onSharedPreferenceChanged -> " + sortPreference);

            if (sortPreference.equals(getString(R.string.settings_sort_popular_value))) {
                popularResponseCall = tmdbAPIV3.getPopularMovies(TmdbApiKey.api_key);
                checkNetwork(popularResponseCall);
            } else if (sortPreference.equals(getString(R.string.settings_sort_top_rated_value))) {
                topRatedResponseCall = tmdbAPIV3.getTopRatedMovies(TmdbApiKey.api_key);
                checkNetwork(topRatedResponseCall);
            }
        }
    }

    private void checkNetwork(Call<MoviesResponse> call) {
        Log.v(LOG_TAG, "-> checkNetwork");

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            Log.v(LOG_TAG, "-> checkNetwork -> is connected");
            call.enqueue(this);
        } else {
            Log.w(LOG_TAG, "-> checkNetwork -> not connected");

            gridAdapter = new GridAdapter(this, new ArrayList<Result>(), getString(R.string.please_try_again));
            recyclerView.setAdapter(gridAdapter);
            swipeRefreshLayout.setRefreshing(false);
            alertDialogNetwork.show();
        }
    }

    @Override
    public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {

        if (response.isSuccessful()) {
            Log.v(LOG_TAG, "-> onResponse -> " + response.code());

            if (response.body().getResults().size() == 0)
                gridAdapter = new GridAdapter(this, new ArrayList<Result>(), getString(R.string.no_movies_found));
            else {
                results = response.body().getResults();
                gridAdapter = new GridAdapter(this, results);
            }

            gridAdapter.setClickListener(this);
            recyclerView.setAdapter(gridAdapter);

        } else {
            Log.e(LOG_TAG, "-> onResponse -> " + response.code() );

            gridAdapter = new GridAdapter(this, new ArrayList<Result>(), getString(R.string.please_try_again));
            recyclerView.setAdapter(gridAdapter);
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onFailure(Call<MoviesResponse> call, Throwable t) {
        Log.e(LOG_TAG, "-> onFailure -> " + t);

        gridAdapter = new GridAdapter(this, new ArrayList<Result>(), getString(R.string.please_try_again));
        recyclerView.setAdapter(gridAdapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        Log.v(LOG_TAG, "-> onRefresh");

        onSharedPreferenceChanged(sharedPreferences, getString(R.string.settings_sort_key));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v(LOG_TAG, "-> onSaveInstanceState");

        outState.putBoolean("isKeyEntered", isKeyEntered);
        if (!isKeyEntered)
            return;

        outState.putParcelableArrayList("lastFetchedResults", gridAdapter.results);
        outState.putString("emptyViewMessage", gridAdapter.emptyViewMessage);
        outState.putBoolean("isAlertDialogNetworkShowing", alertDialogNetwork.isShowing());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.v(LOG_TAG, "-> onRestoreInstanceState");

        isKeyEntered = savedInstanceState.getBoolean("isKeyEntered");
        if (!isKeyEntered)
            return;

        results = savedInstanceState.getParcelableArrayList("lastFetchedResults");
        String emptyViewMessage = savedInstanceState.getString("emptyViewMessage");

        gridAdapter = new GridAdapter(this, results, emptyViewMessage);
        gridAdapter.setClickListener(this);
        recyclerView.setAdapter(gridAdapter);

        if (savedInstanceState.getBoolean("isAlertDialogNetworkShowing"))
            alertDialogNetwork.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "-> onDestroy");

        if(alertDialogKeyNotFound.isShowing())
            alertDialogKeyNotFound.cancel();

        if (alertDialogNetwork.isShowing())
            alertDialogNetwork.cancel();

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
