package com.example.android.popularmoviesstage2;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.popularmoviesstage2.model.Result;
import com.example.android.popularmoviesstage2.model.reviews.ReviewsResponse;
import com.example.android.popularmoviesstage2.model.video.VideosResponse;
import com.example.android.popularmoviesstage2.model.video.VideosResult;
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
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks,
        TrailersAdapter.ItemClickListener {

    public static final String LOG_TAG = DetailsActivity.class.getSimpleName();
    public static final int VIDEOS_CALL = 101;
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
    @BindView(R.id.recyclerViewTrailer)
    RecyclerView recyclerViewTrailer;
    @BindView(R.id.recyclerViewReviews)
    RecyclerView recyclerViewReviews;

    private Result movieDetails;
    private TmdbAPIV3 tmdbAPIV3;
    private VideosResponse videosResponse;
    private ReviewsResponse reviewsResponse;
    private TrailersAdapter trailersAdapter;
    private ReviewsAdapter reviewsAdapter;

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

        recyclerViewTrailer.setLayoutManager(new LinearLayoutManager(this));
        trailersAdapter = new TrailersAdapter(this, null, TrailersAdapter.LOADING_VIEW);
        recyclerViewTrailer.setAdapter(trailersAdapter);

        getSupportLoaderManager().initLoader(VIDEOS_CALL, null, this);
    }

    private void getMovieReviews() {
        Log.v(LOG_TAG, "-> getMovieReviews");

        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewsAdapter = new ReviewsAdapter(this, null, ReviewsAdapter.LOADING_VIEW);
        recyclerViewReviews.setAdapter(reviewsAdapter);

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

            case VIDEOS_CALL:
                Log.v(LOG_TAG, "-> onCreateLoader -> VIDEOS_CALL");
                return new DetailsActivityAsyncTaskLoader(this, tmdbAPIV3, VIDEOS_CALL, movieDetails.getId());

            case REVIEWS_CALL:
                Log.v(LOG_TAG, "-> onCreateLoader -> REVIEWS_CALL");
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
            case VIDEOS_CALL:

                if (data == null || !(((Response<VideosResponse>) data).isSuccessful())) {

                    Log.e(LOG_TAG, "-> onLoadFinished -> onFailure for VIDEOS_CALL");
                    trailersAdapter = new TrailersAdapter(this, null, TrailersAdapter.EMPTY_VIEW);
                    recyclerViewTrailer.setAdapter(trailersAdapter);

                } else {
                    @SuppressWarnings("unchecked")
                    Response<VideosResponse> response = (Response<VideosResponse>) data;
                    Log.v(LOG_TAG, "-> onLoadFinished -> Response<VideosResponse> -> " + response.code());
                    videosResponse = response.body();

                    trailersAdapter = new TrailersAdapter(
                            this,
                            videosResponse.getVideosResults(),
                            TrailersAdapter.NORMAL_VIEW);

                    trailersAdapter.setClickListener(this);
                    recyclerViewTrailer.setAdapter(trailersAdapter);
                }
                break;

            case REVIEWS_CALL:

                if (data == null || !(((Response<ReviewsResponse>) data).isSuccessful())) {

                    Log.e(LOG_TAG, "-> onLoadFinished -> onFailure for REVIEWS_CALL");
                    reviewsAdapter = new ReviewsAdapter(this, null, ReviewsAdapter.EMPTY_VIEW);
                    recyclerViewReviews.setAdapter(reviewsAdapter);

                } else {
                    @SuppressWarnings("unchecked")
                    Response<ReviewsResponse> response = (Response<ReviewsResponse>) data;
                    Log.v(LOG_TAG, "-> onLoadFinished -> Response<ReviewsResponse> -> " + response.code());
                    reviewsResponse = response.body();

                    reviewsAdapter = new ReviewsAdapter(
                            this,
                            reviewsResponse.getReviewsResults(),
                            ReviewsAdapter.NORMAL_VIEW);

                    recyclerViewReviews.setAdapter(reviewsAdapter);
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
        String subject = movieDetails.getTitle();
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

    public void watchYoutubeVideo(String id) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/watch?v=" + id));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }
}
