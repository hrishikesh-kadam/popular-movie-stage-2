package com.example.android.popularmoviesstage1;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmoviesstage1.model.Result;
import com.example.android.popularmoviesstage1.rest.TmdbRetrofit;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Hrishikesh Kadam on 11/10/2017
 */

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    public static final String LOG_TAG = GridAdapter.class.getSimpleName();
    private static final int EMPTY_VIEW = 1;
    private static final int NORMAL_VIEW = 2;
    private Context context;
    private List<Result> results;
    private ItemClickListener itemClickListener;

    public GridAdapter(Context context, List<Result> results) {
        this.context = context;
        this.results = results;
    }

    @Override
    public GridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        GridAdapter.ViewHolder viewHolder = null;

        if (viewType == EMPTY_VIEW) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_view, parent, false);
            viewHolder = new EmptyViewHolder(itemView);
        } else if (viewType == NORMAL_VIEW) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
            viewHolder = new NormalViewHolder(itemView);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(GridAdapter.ViewHolder viewHolder, int position) {

        int viewType = getItemViewType(position);

        if (viewType == EMPTY_VIEW) {

            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) viewHolder;
            emptyViewHolder.textView.setText(context.getString(R.string.no_movies_found));

        } else if (viewType == NORMAL_VIEW) {

            NormalViewHolder normalViewHolder = (NormalViewHolder) viewHolder;
            Uri imageUri = Uri.parse(TmdbRetrofit.IMAGE_BASE_URL)
                    .buildUpon()
                    .appendPath("w185")
                    .appendEncodedPath(results.get(position).getPosterPath())
                    .build();
            Log.i(LOG_TAG, "-> " + imageUri.toString());
            Picasso.with(context).load(imageUri).into(normalViewHolder.imageView);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (results.size() == 0)
            return EMPTY_VIEW;
        else
            return NORMAL_VIEW;
    }

    @Override
    public int getItemCount() {
        if (results.size() == 0)
            return 1;
        else
            return results.size();
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class NormalViewHolder extends ViewHolder implements View.OnClickListener {

        @BindView(R.id.imageView)
        ImageView imageView;

        public NormalViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null)
                itemClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public class EmptyViewHolder extends ViewHolder {

        @BindView(R.id.textView)
        TextView textView;

        public EmptyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
