package com.example.android.popularmoviesstage2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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
import com.google.gson.Gson;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements GridAdapter.ItemClickListener,
        CursorGridAdapter.ItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final int POPULAR_CALL = 1;
    public static final int TOP_RATED_CALL = 2;
    public static final int FAVORITE_CALL = 3;

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private TmdbAPIV3 tmdbAPIV3;
    private GridAdapter gridAdapter;
    private AlertDialog alertDialogNetwork, alertDialogKeyNotFound;
    private SharedPreferences sharedPreferences;
    private boolean isKeyEntered, isRefreshFromUser;
    private ArrayList<Result> results;
    private int CURRENT_CALL_TYPE;
    private Cursor cursor;

    public static String getCallTypeString(int CALL_TYPE) {
        if (CALL_TYPE == POPULAR_CALL)
            return "POPULAR_CALL";
        else if (CALL_TYPE == TOP_RATED_CALL)
            return "TOP_RATED_CALL";
        else if (CALL_TYPE == FAVORITE_CALL)
            return "FAVORITE_CALL";
        else
            return "UNKNOWN_CALL";
    }

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

        if (savedInstanceState == null)
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.settings_sort_key));
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

        Result itemResult;

        if (CURRENT_CALL_TYPE == FAVORITE_CALL) {

            cursor.moveToPosition(position);
            String tempResult = cursor.getString(
                    cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_MOVIE_RESULT_STRING));
            Gson gson = new Gson();
            itemResult = gson.fromJson(tempResult, Result.class);

        } else
            itemResult = results.get(position);

        Log.v(LOG_TAG, "-> onItemClick -> " + itemResult.getOriginalTitle());

        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("movieResult", itemResult);
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

            swipeRefreshLayout.setRefreshing(true);

            String sortPreference = sharedPreferences.getString(key, getString(R.string.settings_sort_default_value));
            Log.v(LOG_TAG, "-> onSharedPreferenceChanged -> " + sortPreference);

            if (sortPreference.equals(getString(R.string.settings_sort_popular_value)))
                checkNetwork(POPULAR_CALL);

            else if (sortPreference.equals(getString(R.string.settings_sort_top_rated_value)))
                checkNetwork(TOP_RATED_CALL);

            else if (sortPreference.equals(getString(R.string.settings_sort_favorite_value)))
                checkNetwork(FAVORITE_CALL);
        }
    }

    private void checkNetwork(int CALL_TYPE) {
        Log.v(LOG_TAG, "-> checkNetwork");

        CURRENT_CALL_TYPE = CALL_TYPE;

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
            Log.w(LOG_TAG, "-> checkNetwork -> not connected");
            if (CALL_TYPE != FAVORITE_CALL) alertDialogNetwork.show();

        } else
            Log.v(LOG_TAG, "-> checkNetwork -> is connected");

        if (getSupportLoaderManager().getLoader(CALL_TYPE) == null) {

            getSupportLoaderManager().destroyLoader(POPULAR_CALL);
            getSupportLoaderManager().destroyLoader(TOP_RATED_CALL);
            getSupportLoaderManager().destroyLoader(FAVORITE_CALL);
        }

        if (isRefreshFromUser)
            getSupportLoaderManager().restartLoader(CALL_TYPE, null, this);
        else
            getSupportLoaderManager().initLoader(CALL_TYPE, null, this);

        isRefreshFromUser = false;
    }

    @Override
    public void onRefresh() {
        Log.v(LOG_TAG, "-> onRefresh");

        isRefreshFromUser = true;
        onSharedPreferenceChanged(sharedPreferences, getString(R.string.settings_sort_key));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v(LOG_TAG, "-> onSaveInstanceState");

        outState.putBoolean("isKeyEntered", isKeyEntered);
        if (!isKeyEntered)
            return;

        outState.putBoolean("isAlertDialogNetworkShowing", alertDialogNetwork.isShowing());

        outState.putInt("CURRENT_CALL_TYPE", CURRENT_CALL_TYPE);
        outState.putBoolean("isRefreshing", swipeRefreshLayout.isRefreshing());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.v(LOG_TAG, "-> onRestoreInstanceState");

        isKeyEntered = savedInstanceState.getBoolean("isKeyEntered");
        if (!isKeyEntered)
            return;

        if (savedInstanceState.getBoolean("isRefreshing"))
            swipeRefreshLayout.setRefreshing(true);

        CURRENT_CALL_TYPE = savedInstanceState.getInt("CURRENT_CALL_TYPE");
        getSupportLoaderManager().initLoader(CURRENT_CALL_TYPE, null, this);

        if (savedInstanceState.getBoolean("isAlertDialogNetworkShowing"))
            alertDialogNetwork.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "-> onDestroy");

        if (alertDialogKeyNotFound.isShowing())
            alertDialogKeyNotFound.cancel();

        if (alertDialogNetwork.isShowing())
            alertDialogNetwork.cancel();

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        switch (id) {

            case POPULAR_CALL:
                Log.v(LOG_TAG, "-> onCreateLoader -> POPULAR_CALL");

                return new MainActivityAsyncTaskLoader(this, tmdbAPIV3, POPULAR_CALL);

            case TOP_RATED_CALL:
                Log.v(LOG_TAG, "-> onCreateLoader -> TOP_RATED_CALL");

                return new MainActivityAsyncTaskLoader(this, tmdbAPIV3, TOP_RATED_CALL);

            case FAVORITE_CALL:
                Log.v(LOG_TAG, "-> onCreateLoader -> FAVORITE_CALL");

                return new MainActivityAsyncTaskLoader(this, tmdbAPIV3, FAVORITE_CALL);

            default:
                throw new UnsupportedOperationException("Unknown id: " + id + " in onCreateLoader");
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {

        int id = loader.getId();

        switch (id) {

            case POPULAR_CALL:
            case TOP_RATED_CALL:

                if (data == null || !(((Response<MoviesResponse>) data).isSuccessful())) {

                    if (data != null) {
                        @SuppressWarnings("unchecked")
                        Response<MoviesResponse> response = (Response<MoviesResponse>) data;
                        Log.e(LOG_TAG, "-> onLoadFinished -> " + getCallTypeString(id) + " onResponse -> " + response.code());
                    } else {
                        Log.e(LOG_TAG, "-> onLoadFinished -> " + getCallTypeString(id) + " onFailure");
                    }

                    gridAdapter = new GridAdapter(this, new ArrayList<Result>(), getString(R.string.please_try_again));
                    recyclerView.setAdapter(gridAdapter);

                } else {

                    @SuppressWarnings("unchecked")
                    Response<MoviesResponse> response = (Response<MoviesResponse>) data;
                    Log.v(LOG_TAG, "-> onLoadFinished -> " + getCallTypeString(id) + " -> " + response.code());

                    if (response.body().getResults().size() == 0)
                        gridAdapter = new GridAdapter(this, new ArrayList<Result>(), getString(R.string.no_movies_found));
                    else {
                        results = response.body().getResults();
                        gridAdapter = new GridAdapter(this, results);
                    }

                    gridAdapter.setClickListener(this);
                    recyclerView.setAdapter(gridAdapter);
                }

                break;

            case FAVORITE_CALL:

                if (data == null || ((Cursor) data).getCount() == 0) {
                    Log.e(LOG_TAG, "-> onLoadFinished -> " + getCallTypeString(id) + " -> No favorite movies found");

                    CursorGridAdapter cursorGridAdapter = new CursorGridAdapter(
                            this, (Cursor) data, getString(R.string.no_favorite_movie_found));
                    recyclerView.setAdapter(cursorGridAdapter);

                } else {
                    Log.v(LOG_TAG, "-> onLoadFinished -> " + getCallTypeString(id) + " -> favorite movies found");

                    cursor = (Cursor) data;
                    CursorGridAdapter cursorGridAdapter = new CursorGridAdapter(
                            this, cursor);
                    cursorGridAdapter.setClickListener(this);
                    recyclerView.setAdapter(cursorGridAdapter);
                }

                break;
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.v(LOG_TAG, "-> onLoaderReset");
    }
}
