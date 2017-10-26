package com.example.android.popularmoviesstage2.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Hrishikesh Kadam on 26/10/2017
 */

public class MovieContract {

    public static final String AUTHORITY = "com.example.android.popularmoviesstage2";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_FAVORITE_MOVIE = "favorite-movies";

    public static final class FavoriteMovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE_MOVIE).build();

        public static final String TABLE_NAME = "\"favorite-movies\"";

        public static final String COLUMN_MOVIE_RESULT = "\"movies-result\"";
        public static final String COLUMN_VIDEOS_RESPONSE = "\"videos-response\"";
        public static final String COLUMN_REVIEWS_RESPONSE = "\"reviews-response\"";

        public static final String COLUMN_MOVIE_RESULT_STRING = "movies-result";
        public static final String COLUMN_VIDEOS_RESPONSE_STRING = "videos-response";
        public static final String COLUMN_REVIEWS_RESPONSE_STRING = "reviews-response";
    }
}
