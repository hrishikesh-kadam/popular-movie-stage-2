package com.example.android.popularmoviesstage2.rest;

import com.example.android.popularmoviesstage2.model.MoviesResponse;
import com.example.android.popularmoviesstage2.model.Result;
import com.example.android.popularmoviesstage2.model.reviews.ReviewsResponse;
import com.example.android.popularmoviesstage2.model.video.VideosResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Hrishikesh Kadam on 11/10/2017
 */

public interface TmdbAPIV3 {

    @GET("movie/popular")
    Call<MoviesResponse> getPopularMovies(@Query("api_key") String apiKey);

    @GET("movie/top_rated")
    Call<MoviesResponse> getTopRatedMovies(@Query("api_key") String apiKey);

    @GET("movie/{movie_id}")
    Call<Result> getMovieDetails(@Path("movie_id") Integer id, @Query("api_key") String apiKey);

    @GET("movie/{movie_id}/videos")
    Call<VideosResponse> getMovieVideos(@Path("movie_id") Integer id, @Query("api_key") String apiKey);

    @GET("movie/{movie_id}/reviews")
    Call<ReviewsResponse> getMovieReviews(@Path("movie_id") Integer id, @Query("api_key") String apiKey);
}
