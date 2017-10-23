package com.example.android.popularmoviesstage2.model.video;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class VideosResponse implements Parcelable {

    public final static Creator<VideosResponse> CREATOR = new Creator<VideosResponse>() {


        @SuppressWarnings({
                "unchecked"
        })
        public VideosResponse createFromParcel(Parcel in) {
            return new VideosResponse(in);
        }

        public VideosResponse[] newArray(int size) {
            return (new VideosResponse[size]);
        }

    };
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("videosResults")
    @Expose
    private List<VideosResult> videosResults = new ArrayList<>();

    protected VideosResponse(Parcel in) {
        this.id = ((Integer) in.readValue((Integer.class.getClassLoader())));
        in.readList(this.videosResults, (VideosResult.class.getClassLoader()));
    }

    public VideosResponse() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<VideosResult> getVideosResults() {
        return videosResults;
    }

    public void setVideosResults(List<VideosResult> videosResults) {
        this.videosResults = videosResults;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeList(videosResults);
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "VideosResponse{" +
                "id=" + id +
                ", videosResults=" + videosResults +
                '}';
    }
}
