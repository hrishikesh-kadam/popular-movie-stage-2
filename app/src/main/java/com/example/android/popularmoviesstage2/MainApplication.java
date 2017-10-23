package com.example.android.popularmoviesstage1;

import android.app.Application;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Created by Hrishikesh Kadam on 12/10/2017
 */

public class MainApplication extends Application {

    public static final String LOG_TAG = MainApplication.class.getSimpleName();
    public static float density;
    public static int widthInPixels;
    public static int heightInPixels;
    public static float widthInDp;
    public static float heightInDp;
    public static int imageViewPosterWidth;
    public static int imageViewPosterHeight;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(LOG_TAG, "-> onCreate");

        initDisplayMetrics();
    }

    public void initDisplayMetrics() {
        Log.v(LOG_TAG, "-> initDisplayMetrics");

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        density = displayMetrics.density;
        widthInPixels = displayMetrics.widthPixels;
        heightInPixels = displayMetrics.heightPixels;
        widthInDp = widthInPixels / density;
        heightInDp = heightInPixels / density;

        Log.i(LOG_TAG, "-> initDisplayMetrics -> desity = " + density);
        Log.i(LOG_TAG, "-> initDisplayMetrics -> widthInPixels x heightInPixels = " + widthInPixels + " x " + heightInPixels);
        Log.i(LOG_TAG, "-> initDisplayMetrics -> widthInDp x heightInDp = " + widthInDp + " x " + heightInDp);

        if (widthInPixels > heightInPixels)
            computeImageViewPosterMetrics(heightInPixels);
        else
            computeImageViewPosterMetrics(widthInPixels);
    }

    public void computeImageViewPosterMetrics(int width) {
        imageViewPosterWidth = width / 2;
        imageViewPosterHeight = (int)(imageViewPosterWidth * 1.5);
    }

}
