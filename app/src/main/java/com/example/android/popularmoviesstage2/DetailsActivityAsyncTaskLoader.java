package com.example.android.popularmoviesstage2;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.example.android.popularmoviesstage2.model.reviews.ReviewsResponse;
import com.example.android.popularmoviesstage2.model.video.VideosResponse;
import com.example.android.popularmoviesstage2.rest.TmdbAPIV3;
import com.example.android.popularmoviesstage2.rest.TmdbApiKey;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

import static com.example.android.popularmoviesstage2.DetailsActivity.VIDEO_CALL;

/**
 * Created by Hrishikesh Kadam on 24/10/2017
 */

public class DetailsActivityAsyncTaskLoader extends AsyncTaskLoader {

    private TmdbAPIV3 tmdbAPIV3;
    private Integer id;
    private int CALL_TYPE;

    public DetailsActivityAsyncTaskLoader(Context context, TmdbAPIV3 tmdbAPIV3, int CALL_TYPE, Integer id) {
        super(context);
        this.tmdbAPIV3 = tmdbAPIV3;
        this.CALL_TYPE = CALL_TYPE;
        this.id = id;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public Object loadInBackground() {

        if (CALL_TYPE == VIDEO_CALL) {

            Call<VideosResponse> videosResponseCall = tmdbAPIV3.getMovieVideos(id, TmdbApiKey.api_key);
            Response<VideosResponse> response = null;

            try {
                response = videosResponseCall.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;

        } else {

            Call<ReviewsResponse> reviewsResponseCall = tmdbAPIV3.getMovieReviews(id, TmdbApiKey.api_key);
            Response<ReviewsResponse> response = null;

            try {
                response = reviewsResponseCall.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }
    }
}
