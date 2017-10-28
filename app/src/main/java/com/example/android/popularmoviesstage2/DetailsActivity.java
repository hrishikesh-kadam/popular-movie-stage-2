package com.example.android.popularmoviesstage2;

import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmoviesstage2.data.MovieContract.FavoriteMovieEntry;
import com.example.android.popularmoviesstage2.model.Result;
import com.example.android.popularmoviesstage2.model.reviews.ReviewsResponse;
import com.example.android.popularmoviesstage2.model.reviews.ReviewsResult;
import com.example.android.popularmoviesstage2.model.video.VideosResponse;
import com.example.android.popularmoviesstage2.model.video.VideosResult;
import com.example.android.popularmoviesstage2.rest.TmdbAPIV3;
import com.example.android.popularmoviesstage2.rest.TmdbRetrofit;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks,
        TrailersAdapter.ItemClickListener, View.OnTouchListener {

    public static final String LOG_TAG = DetailsActivity.class.getSimpleName();
    public static final int VIDEOS_CALL = 101;
    public static final int REVIEWS_CALL = 102;
    private static final int INSERT_ACTION = 1;
    private static final int UPDATE_ACTION = 2;

    @BindView(R.id.textViewTitle)
    TextView textViewTitle;
    @BindView(R.id.imageViewPoster)
    ImageView imageViewPoster;
    @BindView(R.id.textViewRatings)
    TextView textViewRatings;
    @BindView(R.id.textViewReleaseDate)
    TextView textViewReleaseDate;
    @BindView(R.id.textViewOverview)
    TextView textViewOverview;
    @BindView(R.id.recyclerViewTrailer)
    RecyclerView recyclerViewTrailer;
    @BindView(R.id.recyclerViewReviews)
    RecyclerView recyclerViewReviews;
    @BindView(R.id.ratingBar)
    RatingBar ratingBar;

    private Result movieResult;
    private TmdbAPIV3 tmdbAPIV3;
    private VideosResponse videosResponse;
    private ReviewsResponse reviewsResponse;
    private TrailersAdapter trailersAdapter;
    private ReviewsAdapter reviewsAdapter;
    private int ratingAtActionDown;
    private boolean isVideosCallInProgress;
    private boolean isReviewsCallInProgress;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "-> onCreate");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        initRatingBar();

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageViewPoster.getLayoutParams();
        layoutParams.width = MainApplication.imageViewPosterWidth;
        layoutParams.height = MainApplication.imageViewPosterHeight;

        bindData();

        tmdbAPIV3 = TmdbRetrofit.getRetrofit().create(TmdbAPIV3.class);

        getMovieVideos();
        getMovieReviews();
    }

    private void initRatingBar() {
        Log.v(LOG_TAG, "-> initRatingBar");

        LayerDrawable layerDrawable = (LayerDrawable) ratingBar.getProgressDrawable();
        DrawableCompat.setTint(DrawableCompat.wrap(layerDrawable.getDrawable(0)), ContextCompat.getColor(this, R.color.ratingBarBackground));   // Empty star
        DrawableCompat.setTint(DrawableCompat.wrap(layerDrawable.getDrawable(1)), ContextCompat.getColor(this, R.color.ratingBarProgress)); // Partial star
        DrawableCompat.setTint(DrawableCompat.wrap(layerDrawable.getDrawable(2)), ContextCompat.getColor(this, R.color.ratingBarProgress));

       /* ratingBar.setOnTouchListener(new OnTouchListener() {

            int ratingAtActionDown;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    ratingAtActionDown = (int) ratingBar.getRating();
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    ratingBar.setRating(ratingAtActionDown == 0 ? 1 : 0);
                }

                return true;
            }
        });*/

        ratingBar.setOnTouchListener(this);
    }

    private void getMovieVideos() {
        Log.v(LOG_TAG, "-> getMovieVideos");

        recyclerViewTrailer.setLayoutManager(new LinearLayoutManager(this));
        trailersAdapter = new TrailersAdapter(this, null, TrailersAdapter.LOADING_VIEW);
        recyclerViewTrailer.setAdapter(trailersAdapter);

        isVideosCallInProgress = true;
        getSupportLoaderManager().initLoader(VIDEOS_CALL, null, this);
    }

    private void getMovieReviews() {
        Log.v(LOG_TAG, "-> getMovieReviews");

        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewsAdapter = new ReviewsAdapter(this, null, ReviewsAdapter.LOADING_VIEW);
        recyclerViewReviews.setAdapter(reviewsAdapter);

        isReviewsCallInProgress = true;
        getSupportLoaderManager().initLoader(REVIEWS_CALL, null, this);
    }

    private void bindData() {
        Log.v(LOG_TAG, "-> bindData");

        movieResult = getIntent().getParcelableExtra("movieResult");

        textViewTitle.setText(movieResult.getTitle());
        Picasso.with(this).load(movieResult.getFullPosterPath()).into(imageViewPoster);
        textViewRatings.setText(String.valueOf(movieResult.getVoteAverage()));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = simpleDateFormat.parse(movieResult.getReleaseDate());
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
            textViewReleaseDate.setText(dateFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        textViewOverview.setText(movieResult.getOverview());

        Cursor cursor = getContentResolver().query(
                ContentUris.withAppendedId(FavoriteMovieEntry.CONTENT_URI, movieResult.getId()),
                null, null, null, null);

        if (cursor != null && cursor.getCount() == 1)
            ratingBar.setRating(1.0f);

        if (cursor != null)
            cursor.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        switch (id) {

            case VIDEOS_CALL:
                Log.v(LOG_TAG, "-> onCreateLoader -> VIDEOS_CALL");
                return new DetailsActivityAsyncTaskLoader(this, tmdbAPIV3, VIDEOS_CALL, movieResult.getId());

            case REVIEWS_CALL:
                Log.v(LOG_TAG, "-> onCreateLoader -> REVIEWS_CALL");
                return new DetailsActivityAsyncTaskLoader(this, tmdbAPIV3, REVIEWS_CALL, movieResult.getId());

            default:
                Log.e(LOG_TAG, "-> unhandled onCreateLoader for id = " + id);
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {

        ContentValues contentValues = new ContentValues();
        int id = loader.getId();

        switch (id) {
            case VIDEOS_CALL:

                if (data == null || !(((Response<VideosResponse>) data).isSuccessful())) {

                    Log.e(LOG_TAG, "-> onLoadFinished -> onFailure for VIDEOS_CALL");

                    if (ratingBar.getRating() == 1.0f) {

                        Cursor cursor = getContentResolver().query(
                                ContentUris.withAppendedId(FavoriteMovieEntry.CONTENT_URI, movieResult.getId()),
                                new String[]{FavoriteMovieEntry.COLUMN_VIDEOS_RESPONSE},
                                null, null, null);

                        if (cursor != null && cursor.moveToFirst()) {

                            videosResponse = gson.fromJson(
                                    cursor.getString(cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_VIDEOS_RESPONSE_STRING)),
                                    VideosResponse.class);

                            cursor.close();
                        }

                        ArrayList<VideosResult> tempVideosResults = null;
                        if (videosResponse != null)
                            tempVideosResults = videosResponse.getVideosResults();

                        trailersAdapter = new TrailersAdapter(this, tempVideosResults, TrailersAdapter.NORMAL_VIEW);

                        trailersAdapter.setClickListener(this);
                        recyclerViewTrailer.setAdapter(trailersAdapter);

                    } else {

                        trailersAdapter = new TrailersAdapter(this, null, TrailersAdapter.FAILURE_VIEW);
                        recyclerViewTrailer.setAdapter(trailersAdapter);
                    }

                } else {
                    @SuppressWarnings("unchecked")
                    Response<VideosResponse> response = (Response<VideosResponse>) data;
                    Log.v(LOG_TAG, "-> onLoadFinished -> VIDEOS_CALL -> " + response.code());
                    videosResponse = response.body();

                    trailersAdapter = new TrailersAdapter(
                            this,
                            videosResponse.getVideosResults(),
                            TrailersAdapter.NORMAL_VIEW);

                    trailersAdapter.setClickListener(this);
                    recyclerViewTrailer.setAdapter(trailersAdapter);
                }

                if (ratingBar.getRating() == 1.0f) {

                    contentValues.put(FavoriteMovieEntry.COLUMN_VIDEOS_RESPONSE, gson.toJson(videosResponse));

                    getContentResolver().update(
                            ContentUris.withAppendedId(FavoriteMovieEntry.CONTENT_URI, movieResult.getId()),
                            contentValues,
                            null, null);
                }

                isVideosCallInProgress = false;
                break;

            case REVIEWS_CALL:

                if (data == null || !(((Response<ReviewsResponse>) data).isSuccessful())) {

                    Log.e(LOG_TAG, "-> onLoadFinished -> onFailure for REVIEWS_CALL");

                    if (ratingBar.getRating() == 1.0f) {

                        Cursor cursor = getContentResolver().query(
                                ContentUris.withAppendedId(FavoriteMovieEntry.CONTENT_URI, movieResult.getId()),
                                new String[]{FavoriteMovieEntry.COLUMN_REVIEWS_RESPONSE},
                                null, null, null);

                        if (cursor != null && cursor.moveToFirst()) {

                            reviewsResponse = gson.fromJson(
                                    cursor.getString(cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_REVIEWS_RESPONSE_STRING)),
                                    ReviewsResponse.class);

                            cursor.close();
                        }

                        ArrayList<ReviewsResult> tempReviewsResults = null;
                        if (reviewsResponse != null)
                            tempReviewsResults = reviewsResponse.getReviewsResults();

                        reviewsAdapter = new ReviewsAdapter(this, tempReviewsResults, ReviewsAdapter.NORMAL_VIEW);
                        recyclerViewReviews.setAdapter(reviewsAdapter);

                    } else {

                        reviewsAdapter = new ReviewsAdapter(this, null, ReviewsAdapter.FAILURE_VIEW);
                        recyclerViewReviews.setAdapter(reviewsAdapter);
                    }

                } else {
                    @SuppressWarnings("unchecked")
                    Response<ReviewsResponse> response = (Response<ReviewsResponse>) data;
                    Log.v(LOG_TAG, "-> onLoadFinished -> REVIEWS_CALL -> " + response.code());
                    reviewsResponse = response.body();

                    reviewsAdapter = new ReviewsAdapter(
                            this,
                            reviewsResponse.getReviewsResults(),
                            ReviewsAdapter.NORMAL_VIEW);

                    recyclerViewReviews.setAdapter(reviewsAdapter);
                }

                if (ratingBar.getRating() == 1.0f) {

                    contentValues.put(FavoriteMovieEntry.COLUMN_REVIEWS_RESPONSE, gson.toJson(reviewsResponse));

                    getContentResolver().update(
                            ContentUris.withAppendedId(FavoriteMovieEntry.CONTENT_URI, movieResult.getId()),
                            contentValues,
                            null, null);
                }

                isReviewsCallInProgress = false;
                break;

            default:
                Log.e(LOG_TAG, "-> unhandled onLoadFinished for id = " + id);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.v(LOG_TAG, "-> onLoaderReset");
    }

    @Override
    public void onItemClickTrailer(View itemView, int position) {
        VideosResult videosResult = videosResponse.getVideosResults().get(position);
        Log.v(LOG_TAG, "-> onItemClickTrailer -> trailer -> " + videosResult.getName());
        watchYoutubeVideo(videosResult.getKey());
    }

    @Override
    public void onClickShareTrailer(View itemView, int position) {
        VideosResult videosResult = videosResponse.getVideosResults().get(position);
        Log.v(LOG_TAG, "-> onClickShareTrailer -> trailer -> " + videosResult.getName());
        String subject = movieResult.getTitle();
        String text = videosResult.getName() + " - " +
                Uri.parse("https://www.youtube.com/watch?v=" + videosResult.getKey());
        shareUrl(subject, text);
    }

    private void shareUrl(String subject, String text) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, subject + "\n" + text);

        startActivity(Intent.createChooser(intent, getString(R.string.share_link)));
    }

    private void watchYoutubeVideo(String id) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/watch?v=" + id));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent motionEvent) {

        switch (v.getId()) {

            case R.id.ratingBar:

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    ratingAtActionDown = (int) ratingBar.getRating();

                else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    ratingBar.setRating(ratingAtActionDown == 0 ? 1 : 0);
                    onRatingChanged(ratingBar.getRating(), true);
                    v.performClick();
                }

                return true;
        }

        return false;
    }

    public void onRatingChanged(float rating, boolean fromUser) {
        Log.v(LOG_TAG, "-> onRatingChanged -> rating = " + rating + ", fromUser = " + fromUser);

        if (fromUser && rating == 1.0)
            insertOrUpdate(INSERT_ACTION);

        else if (fromUser && rating == 0.0) {

            int noOfRowsDeleted = getContentResolver().delete(
                    ContentUris.withAppendedId(FavoriteMovieEntry.CONTENT_URI, movieResult.getId()),
                    null,
                    null
            );
            Log.v(LOG_TAG, "-> onRatingChanged -> No of rows deleted = " + noOfRowsDeleted);
            Toast.makeText(this, R.string.deleted_from_favorite, Toast.LENGTH_SHORT).show();
        }
    }

    private void insertOrUpdate(int action) {

        switch (action) {

            case INSERT_ACTION:
                Log.v(LOG_TAG, "-> insertOrUpdate -> INSERT_ACTION");

                ContentValues contentValues = new ContentValues();
                contentValues.put(FavoriteMovieEntry._ID, movieResult.getId());
                contentValues.put(FavoriteMovieEntry.COLUMN_MOVIE_RESULT, gson.toJson(movieResult));

                if (!isVideosCallInProgress)
                    contentValues.put(FavoriteMovieEntry.COLUMN_VIDEOS_RESPONSE, gson.toJson(videosResponse));
                else
                    Log.d(LOG_TAG, "-> insertOrUpdate -> INSERT_ACTION -> VIDEOS_CALL is in progress so skipping insert");

                if (!isReviewsCallInProgress)
                    contentValues.put(FavoriteMovieEntry.COLUMN_REVIEWS_RESPONSE, gson.toJson(reviewsResponse));
                else
                    Log.d(LOG_TAG, "-> insertOrUpdate -> INSERT_ACTION -> REVIEWS_CALL is in progress so skipping insert");

                Uri uri = getContentResolver().insert(FavoriteMovieEntry.CONTENT_URI, contentValues);
                Log.v(LOG_TAG, "-> insertOrUpdate -> INSERT_ACTION -> row inserted at " + uri);
                Toast.makeText(this, R.string.added_to_favorite, Toast.LENGTH_SHORT).show();

                break;

            case UPDATE_ACTION:
                Log.v(LOG_TAG, "-> insertOrUpdate -> UPDATE_ACTION");


                break;

            default:
                throw new UnsupportedOperationException("Unknown action = " + action +
                        " found in insertOrUpdate method");
        }

    }
}
