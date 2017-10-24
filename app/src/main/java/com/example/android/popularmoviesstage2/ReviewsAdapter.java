package com.example.android.popularmoviesstage2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmoviesstage2.model.reviews.ReviewsResult;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Hrishikesh Kadam on 25/10/2017
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {

    private static final String LOG_TAG = ReviewsAdapter.class.getSimpleName();
    public static final int LOADING_VIEW = 1;
    public static final int EMPTY_VIEW = 2;
    public static final int NORMAL_VIEW = 3;
    private Context context;
    private ArrayList<ReviewsResult> reviewsResults = new ArrayList<>();
    private int currentViewType;

    public ReviewsAdapter(Context context, ArrayList<ReviewsResult> reviewsResults, int currentViewType) {
        this.context = context;
        this.reviewsResults = reviewsResults;
        this.currentViewType = currentViewType;
    }

    @Override
    public int getItemCount() {
        if (reviewsResults == null || reviewsResults.size() == 0)
            return 1;
        else
            return reviewsResults.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (currentViewType == NORMAL_VIEW && reviewsResults == null)
            return EMPTY_VIEW;
        else if (currentViewType == NORMAL_VIEW && reviewsResults.size() != 0)
            return NORMAL_VIEW;
        else if (currentViewType == NORMAL_VIEW && reviewsResults.size() == 0)
            return EMPTY_VIEW;
        else if (currentViewType == LOADING_VIEW)
            return LOADING_VIEW;
        else if (currentViewType == EMPTY_VIEW)
            return EMPTY_VIEW;
        else {
            Log.e(LOG_TAG, "-> getItemViewType -> Unhandled scope");
            return 0;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ViewHolder viewHolder;
        View itemView;

        switch(viewType) {

            case NORMAL_VIEW:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_reviews, parent, false);
                viewHolder = new NormalViewHolder(itemView);
                return viewHolder;

            case LOADING_VIEW:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_layout, parent, false);
                viewHolder = new LoadingViewHolder(itemView);
                return viewHolder;

            case EMPTY_VIEW:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_view_layout, parent, false);
                viewHolder = new EmptyViewHolder(itemView);
                return viewHolder;

            default:
                Log.e(LOG_TAG, "-> Unknown currentViewType = " + viewType);
                return null;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        int viewType = getItemViewType(position);

        switch (viewType) {

            case NORMAL_VIEW:
                NormalViewHolder normalViewHolder = (NormalViewHolder) viewHolder;
                normalViewHolder.textViewReviewAuthor.setText(reviewsResults.get(position).getAuthor());
                normalViewHolder.textViewReviewContent.setText(reviewsResults.get(position).getContent());
                break;

            case EMPTY_VIEW:
                EmptyViewHolder emptyViewHolder = (EmptyViewHolder) viewHolder;
                emptyViewHolder.textViewEmpty.setText(context.getString(R.string.empty_view_reviews));
                break;

            case LOADING_VIEW:
                break;

            default:
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class NormalViewHolder extends ViewHolder {

        @BindView(R.id.textViewReviewAuthor)
        public TextView textViewReviewAuthor;

        @BindView(R.id.textViewReviewContent)
        public TextView textViewReviewContent;

        public NormalViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class LoadingViewHolder extends ViewHolder {

        @BindView(R.id.progressBar)
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class EmptyViewHolder extends ViewHolder {

        @BindView(R.id.textViewEmpty)
        public TextView textViewEmpty;

        public EmptyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
