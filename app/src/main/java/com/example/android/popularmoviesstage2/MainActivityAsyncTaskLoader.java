package com.example.android.popularmoviesstage2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.android.popularmoviesstage2.data.MovieContract.FavoriteMovieEntry;
import com.example.android.popularmoviesstage2.model.MoviesResponse;
import com.example.android.popularmoviesstage2.model.Result;
import com.example.android.popularmoviesstage2.rest.TmdbAPIV3;
import com.example.android.popularmoviesstage2.rest.TmdbApiKey;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

import static com.example.android.popularmoviesstage2.MainActivity.FAVORITE_CALL;
import static com.example.android.popularmoviesstage2.MainActivity.POPULAR_CALL;
import static com.example.android.popularmoviesstage2.MainActivity.TOP_RATED_CALL;
import static com.example.android.popularmoviesstage2.MainActivity.getCallTypeString;

/**
 * Created by Hrishikesh Kadam on 28/10/2017
 */

public class MainActivityAsyncTaskLoader extends AsyncTaskLoader {

    public static final String LOG_TAG = MainActivityAsyncTaskLoader.class.getSimpleName();
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

        if (CALL_TYPE == FAVORITE_CALL) {

            if (response == null)
                forceLoad();
            else {
                response = queryOfflineData();
                deliverResult(response);
            }

        } else {

            if (response == null)
                forceLoad();
            else
                deliverResult(response);

        }
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

                return response;

            case FAVORITE_CALL:
                return favoriteCall();

            default:
                throw new UnsupportedOperationException("Unknown CALL_TYPE: " + CALL_TYPE + " in loadInBackground");
        }
    }

    private Object favoriteCall() {

        Cursor cursor = getContext().getContentResolver().query(
                FavoriteMovieEntry.CONTENT_URI,
                new String[]{FavoriteMovieEntry._ID,
                        FavoriteMovieEntry.COLUMN_MOVIE_RESULT},
                null,
                null,
                null);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
            Log.v(LOG_TAG, "-> loadInBackground -> " + getCallTypeString(CALL_TYPE) + " -> Offline data");

            response = cursor;
            return response;

        } else
            Log.v(LOG_TAG, "-> loadInBackground -> " + getCallTypeString(CALL_TYPE) + " -> Updating data");

        Gson gson = new Gson();

        while (cursor.moveToNext()) {

            long movieId = cursor.getLong(0);
            Result tempMovieResult = gson.fromJson(cursor.getString(
                    cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_MOVIE_RESULT_STRING)),
                    Result.class);

            Log.d(LOG_TAG, "-> FAVORITE_CALL -> " + tempMovieResult.getTitle());

            Call<Result> resultCall = tmdbAPIV3.getMovieDetails(
                    (int) movieId, TmdbApiKey.api_key);

            networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected())
                continue;

            Response<Result> tempResponse = null;
            try {
                tempResponse = resultCall.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (tempResponse != null && tempResponse.isSuccessful()) {
                Log.d(LOG_TAG, "-> FAVORITE_CALL -> " + tempMovieResult.getTitle() + " -> updating");

                String tempNewMovieResult = gson.toJson(tempResponse.body());

                ContentValues contentValues = new ContentValues();
                contentValues.put(FavoriteMovieEntry.COLUMN_MOVIE_RESULT, tempNewMovieResult);

                getContext().getContentResolver().update(
                        ContentUris.withAppendedId(FavoriteMovieEntry.CONTENT_URI, movieId),
                        contentValues,
                        null, null);
            }
        }

        response = getContext().getContentResolver().query(
                FavoriteMovieEntry.CONTENT_URI,
                new String[]{FavoriteMovieEntry._ID,
                        FavoriteMovieEntry.COLUMN_MOVIE_RESULT},
                null,
                null,
                null);

        return response;
    }

    private Object queryOfflineData() {
        Log.d(LOG_TAG, "-> queryOfflineData");

        return getContext().getContentResolver().query(
                FavoriteMovieEntry.CONTENT_URI,
                new String[]{FavoriteMovieEntry._ID,
                        FavoriteMovieEntry.COLUMN_MOVIE_RESULT},
                null,
                null,
                null);
    }
}
