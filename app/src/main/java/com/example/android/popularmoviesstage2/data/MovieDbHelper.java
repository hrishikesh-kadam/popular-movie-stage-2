package com.example.android.popularmoviesstage2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.popularmoviesstage2.data.MovieContract.FavoriteMovieEntry;

/**
 * Created by Hrishikesh Kadam on 26/10/2017
 */

public class MovieDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "moviesDb.db";
    private static final int VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String CREATE_TABLE = "CREATE TABLE " + FavoriteMovieEntry.TABLE_NAME + " (" +
                FavoriteMovieEntry._ID + " INTEGER PRIMARY KEY, " +
                FavoriteMovieEntry.COLUMN_MOVIE_RESULT + " TEXT, " +
                FavoriteMovieEntry.COLUMN_VIDEOS_RESPONSE + " TEXT, " +
                FavoriteMovieEntry.COLUMN_REVIEWS_RESPONSE + " TEXT);";

        sqLiteDatabase.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
