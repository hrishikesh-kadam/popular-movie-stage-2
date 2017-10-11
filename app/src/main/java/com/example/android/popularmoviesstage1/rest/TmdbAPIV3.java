package com.example.android.popularmoviesstage1.rest;

import com.example.android.popularmoviesstage1.model.MoviesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Hrishikesh Kadam on 11/10/2017
 */

public interface TmdbAPIV3 {

    @GET("movie/popular")
    Call<MoviesResponse> getPopularMovies(@Query("api_key") String apiKey);

    @GET("movie/top_rated")
    Call<MoviesResponse> getTopRatedMovies(@Query("api_key") String apiKey);
}
