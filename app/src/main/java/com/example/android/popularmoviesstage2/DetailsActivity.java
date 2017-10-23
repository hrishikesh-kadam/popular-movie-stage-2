package com.example.android.popularmoviesstage2;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.popularmoviesstage2.model.Result;
import com.example.android.popularmoviesstage2.model.reviews.ReviewsResponse;
import com.example.android.popularmoviesstage2.model.video.VideosResponse;
import com.example.android.popularmoviesstage2.rest.TmdbAPIV3;
import com.example.android.popularmoviesstage2.rest.TmdbRetrofit;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks {

    public static final String LOG_TAG = DetailsActivity.class.getSimpleName();
    public static final int VIDEO_CALL = 101;
    public static final int REVIEWS_CALL = 102;
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
    private Result movieDetails;
    private TmdbAPIV3 tmdbAPIV3;
    private Call<VideosResponse> videosResponseCall;
    private Call<ReviewsResponse> reviewsResponseCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "-> onCreate");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageViewPoster.getLayoutParams();
        layoutParams.width = MainApplication.imageViewPosterWidth;
        layoutParams.height = MainApplication.imageViewPosterHeight;

        bindData();

        tmdbAPIV3 = TmdbRetrofit.getRetrofit().create(TmdbAPIV3.class);

        getMovieVideos();
        getMovieReviews();
    }

    private void getMovieVideos() {
        Log.v(LOG_TAG, "-> getMovieVideos");

        getSupportLoaderManager().initLoader(VIDEO_CALL, null, this);
    }

    private void getMovieReviews() {
        Log.v(LOG_TAG, "-> getMovieReviews");

        getSupportLoaderManager().initLoader(REVIEWS_CALL, null, this);
    }

    private void bindData() {
        Log.v(LOG_TAG, "-> bindData");

        movieDetails = getIntent().getParcelableExtra("movieDetails");

        textViewTitle.setText(movieDetails.getTitle());
        Picasso.with(this).load(movieDetails.getFullPosterPath()).into(imageViewPoster);
        textViewRatings.setText(String.valueOf(movieDetails.getVoteAverage()));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = simpleDateFormat.parse(movieDetails.getReleaseDate());
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
            textViewReleaseDate.setText(dateFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        textViewOverview.setText(movieDetails.getOverview());
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
            case VIDEO_CALL:
                return new DetailsActivityAsyncTaskLoader(this, tmdbAPIV3, VIDEO_CALL, movieDetails.getId());
            case REVIEWS_CALL:
                return new DetailsActivityAsyncTaskLoader(this, tmdbAPIV3, REVIEWS_CALL, movieDetails.getId());
            default:
                Log.e(LOG_TAG, "-> unhandled onCreateLoader for id = " + id);
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {

        int id = loader.getId();

        switch (id) {
            case VIDEO_CALL:

                if (data == null) {
                    Log.e(LOG_TAG, "-> onLoadFinished -> onFailure for VIDEO_CALL");
                } else {
                    @SuppressWarnings("unchecked")
                    Response<VideosResponse> response = (Response<VideosResponse>) data;
                    Log.v(LOG_TAG, "-> onLoadFinished -> Response<VideosResponse> -> " + response.code());
                }
                break;

            case REVIEWS_CALL:

                if (data == null) {
                    Log.e(LOG_TAG, "-> onLoadFinished -> onFailure for REVIEWS_CALL");
                } else {
                    @SuppressWarnings("unchecked")
                    Response<ReviewsResponse> response = (Response<ReviewsResponse>) data;
                    Log.v(LOG_TAG, "-> onLoadFinished -> Response<ReviewsResponse> -> " + response.code());
                }
                break;

            default:
                Log.e(LOG_TAG, "-> unhandled onLoadFinished for id = " + id);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.v(LOG_TAG, "-> onLoaderReset");
    }
}
