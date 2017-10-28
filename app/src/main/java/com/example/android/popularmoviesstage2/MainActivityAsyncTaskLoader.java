package com.example.android.popularmoviesstage2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.android.popularmoviesstage2.model.MoviesResponse;
import com.example.android.popularmoviesstage2.rest.TmdbAPIV3;
import com.example.android.popularmoviesstage2.rest.TmdbApiKey;

import java.io.IOException;

import retrofit2.Call;

import static com.example.android.popularmoviesstage2.MainActivity.FAVORITE_CALL;
import static com.example.android.popularmoviesstage2.MainActivity.LOG_TAG;
import static com.example.android.popularmoviesstage2.MainActivity.POPULAR_CALL;
import static com.example.android.popularmoviesstage2.MainActivity.TOP_RATED_CALL;
import static com.example.android.popularmoviesstage2.MainActivity.getCallTypeString;

/**
 * Created by Hrishikesh Kadam on 28/10/2017
 */

public class MainActivityAsyncTaskLoader extends AsyncTaskLoader {

    private TmdbAPIV3 tmdbAPIV3;
    private int CALL_TYPE;
    private Object response;
    private ConnectivityManager connMgr;

    public MainActivityAsyncTaskLoader(Context context, TmdbAPIV3 tmdbAPIV3, int CALL_TYPE) {
        super(context);
        this.tmdbAPIV3 = tmdbAPIV3;
        this.CALL_TYPE = CALL_TYPE;
        connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        if (response == null)
            forceLoad();
        else
            deliverResult(response);
    }

    @Override
    public Object loadInBackground() {
        Log.v(LOG_TAG, "-> loadInBackground -> " + getCallTypeString(CALL_TYPE));

        NetworkInfo networkInfo;

        switch (CALL_TYPE) {

            case POPULAR_CALL:

                networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo == null || !networkInfo.isConnected())
                    return null;

                Call<MoviesResponse> popularMoviesResponseCall = tmdbAPIV3.getPopularMovies(TmdbApiKey.api_key);

                try {
                    response = popularMoviesResponseCall.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return response;

            case TOP_RATED_CALL:

                networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo == null || !networkInfo.isConnected())
                    return null;

                Call<MoviesResponse> topRatedMoviesResponseCall = tmdbAPIV3.getTopRatedMovies(TmdbApiKey.api_key);

                try {
                    response = topRatedMoviesResponseCall.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return response;

            case FAVORITE_CALL:
                return null;

            default:
                throw new UnsupportedOperationException("Unknown CALL_TYPE: " + CALL_TYPE + " in loadInBackground");
        }
    }
}
