package com.example.android.popularmoviesstage2;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmoviesstage2.data.MovieContract.FavoriteMovieEntry;
import com.example.android.popularmoviesstage2.model.Result;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Hrishikesh Kadam on 11/10/2017
 */

public class CursorGridAdapter extends RecyclerView.Adapter<CursorGridAdapter.ViewHolder> {

    public static final String LOG_TAG = CursorGridAdapter.class.getSimpleName();
    private static final int EMPTY_VIEW = 1;
    private static final int NORMAL_VIEW = 2;
    private Context context;
    public Cursor cursor;
    public String emptyViewMessage;
    private ItemClickListener itemClickListener;
    private Gson gson = new Gson();

    public CursorGridAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    public CursorGridAdapter(Context context, Cursor cursor, String emptyViewMessage) {
        this.context = context;
        this.cursor = cursor;
        this.emptyViewMessage = emptyViewMessage;
    }

    @Override
    public CursorGridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        CursorGridAdapter.ViewHolder viewHolder = null;

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
    public void onBindViewHolder(CursorGridAdapter.ViewHolder viewHolder, int position) {

        int viewType = getItemViewType(position);

        if (viewType == EMPTY_VIEW) {

            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) viewHolder;
            emptyViewHolder.textView.setText(emptyViewMessage);

        } else if (viewType == NORMAL_VIEW) {

            cursor.moveToPosition(position);

            Result result = gson.fromJson(cursor.getString(
                    cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_MOVIE_RESULT_STRING)),
                    Result.class);

            NormalViewHolder normalViewHolder = (NormalViewHolder) viewHolder;

            Uri imageUri = result.getFullPosterPath();
            Log.i(LOG_TAG, "-> " + imageUri.toString());
            Picasso.with(context).load(imageUri).into(normalViewHolder.imageView);
        }

    }

    @Override
    public int getItemViewType(int position) {

        if (cursor.getCount() == 0)
            return EMPTY_VIEW;
        else
            return NORMAL_VIEW;
    }

    @Override
    public int getItemCount() {

        if (cursor.getCount() == 0)
            return 1;
        else
            return cursor.getCount();
    }

    public void setClickListener(ItemClickListener itemClickListener) {
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
            imageView.setOnClickListener(this);

            FlexboxLayoutManager.LayoutParams layoutParams =
                    (FlexboxLayoutManager.LayoutParams) imageView.getLayoutParams();

            layoutParams.setWidth(MainApplication.imageViewPosterWidth);
            layoutParams.setHeight(MainApplication.imageViewPosterHeight);
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
