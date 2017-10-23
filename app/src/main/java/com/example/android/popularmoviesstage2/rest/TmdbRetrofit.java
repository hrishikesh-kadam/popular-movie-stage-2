package com.example.android.popularmoviesstage2.rest;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Hrishikesh Kadam on 11/10/2017
 */

public class TmdbRetrofit {

    public static final String BASE_URL = "https://api.themoviedb.org/3/";
    public static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
