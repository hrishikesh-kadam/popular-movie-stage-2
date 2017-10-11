package com.example.android.popularmoviesstage1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.android.popularmoviesstage1.model.MoviesResponse;
import com.example.android.popularmoviesstage1.rest.TmdbAPIV3;
import com.example.android.popularmoviesstage1.rest.TmdbApiKey;
import com.example.android.popularmoviesstage1.rest.TmdbRetrofit;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements GridAdapter.ItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener, Callback<MoviesResponse> {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private TmdbAPIV3 tmdbAPIV3;
    private Call<MoviesResponse> popularResponseCall, topRatedResponseCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "-> onCreate");

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.CENTER);
        recyclerView.setLayoutManager(layoutManager);

        tmdbAPIV3 = TmdbRetrofit.getRetrofit().create(TmdbAPIV3.class);
        popularResponseCall = tmdbAPIV3.getPopularMovies(TmdbApiKey.api_key);
        topRatedResponseCall = tmdbAPIV3.getTopRatedMovies(TmdbApiKey.api_key);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(sharedPreferences, getString(R.string.settings_sort_key));
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.v(LOG_TAG, "-> onItemClick");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(LOG_TAG, "-> onOptionsItemSelected -> " + item.getTitle());

        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
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
                popularResponseCall.enqueue(this);
            } else if (sortPreference.equals(getString(R.string.settings_sort_top_rated_value))) {
                topRatedResponseCall.enqueue(this);
            }
        }
    }

    @Override
    public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {

        if (response.isSuccessful()) {
            Log.v(LOG_TAG, "-> onResponse -> " + response.code() + "\n" + response.body());

            recyclerView.setAdapter(new GridAdapter(this, response.body().getResults()));
        } else {
            Log.e(LOG_TAG, "-> onResponse -> " + response.code() );
            recyclerView.setAdapter(null);
        }

    }

    @Override
    public void onFailure(Call<MoviesResponse> call, Throwable t) {
        Log.e(LOG_TAG, "-> onFailure -> " + t);
        recyclerView.setAdapter(null);
    }
}
