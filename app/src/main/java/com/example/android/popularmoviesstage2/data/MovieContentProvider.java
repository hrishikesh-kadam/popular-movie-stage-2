package com.example.android.popularmoviesstage2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.popularmoviesstage2.data.MovieContract.FavoriteMovieEntry;

/**
 * Created by Hrishikesh Kadam on 26/10/2017
 */

public class MovieContentProvider extends ContentProvider {

    private static final int FAVORITE_MOVIE = 1;
    private static final int FAVORITE_MOVIE_WITH_ID = 2;
    private static final UriMatcher uriMatcher = buildUriMatcher();
    private MovieDbHelper movieDbHelper;

    private static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_FAVORITE_MOVIE, FAVORITE_MOVIE);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_FAVORITE_MOVIE + "/#", FAVORITE_MOVIE_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        movieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor cursor;
        final SQLiteDatabase db = movieDbHelper.getReadableDatabase();

        switch (uriMatcher.match(uri)) {

            case FAVORITE_MOVIE:
                cursor = db.query(FavoriteMovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case FAVORITE_MOVIE_WITH_ID:
                cursor = db.query(FavoriteMovieEntry.TABLE_NAME,
                        projection,
                        FavoriteMovieEntry._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null,
                        null,
                        null);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri + " in query method");
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("getType not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        Uri returnUri;
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {

            case FAVORITE_MOVIE:
                long id = db.insert(FavoriteMovieEntry.TABLE_NAME, null, contentValues);
                if (id > 0)
                    returnUri = ContentUris.withAppendedId(FavoriteMovieEntry.CONTENT_URI, id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri + " in insert method");
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        int noOfRowsDeleted;
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {

            case FAVORITE_MOVIE:
                noOfRowsDeleted = db.delete(FavoriteMovieEntry.TABLE_NAME,
                        null,
                        null);
                break;

            case FAVORITE_MOVIE_WITH_ID:
                noOfRowsDeleted = db.delete(FavoriteMovieEntry.TABLE_NAME,
                        FavoriteMovieEntry._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri + " in delete method");
        }

        if (noOfRowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return noOfRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int noOfRowsUpdated;
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {

            case FAVORITE_MOVIE_WITH_ID:
                noOfRowsUpdated = db.update(FavoriteMovieEntry.TABLE_NAME,
                        values,
                        FavoriteMovieEntry._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri + " in update method");
        }

        return noOfRowsUpdated;

    }
}
