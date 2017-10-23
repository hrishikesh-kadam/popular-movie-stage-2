package com.example.android.popularmoviesstage2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.popularmoviesstage2.model.Result;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity {

    public static final String LOG_TAG = DetailsActivity.class.getSimpleName();
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
}
