package com.example.android.popularmoviesstage2.model.reviews;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ReviewsResponse implements Parcelable {

    public final static Creator<ReviewsResponse> CREATOR = new Creator<ReviewsResponse>() {


        @SuppressWarnings({
                "unchecked"
        })
        public ReviewsResponse createFromParcel(Parcel in) {
            return new ReviewsResponse(in);
        }

        public ReviewsResponse[] newArray(int size) {
            return (new ReviewsResponse[size]);
        }

    };
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("page")
    @Expose
    private Integer page;
    @SerializedName("reviewsResults")
    @Expose
    private List<ReviewsResult> reviewsResults = new ArrayList<ReviewsResult>();
    @SerializedName("total_pages")
    @Expose
    private Integer totalPages;
    @SerializedName("total_results")
    @Expose
    private Integer totalResults;

    protected ReviewsResponse(Parcel in) {
        this.id = ((Integer) in.readValue((Integer.class.getClassLoader())));
        this.page = ((Integer) in.readValue((Integer.class.getClassLoader())));
        in.readList(this.reviewsResults, (ReviewsResult.class.getClassLoader()));
        this.totalPages = ((Integer) in.readValue((Integer.class.getClassLoader())));
        this.totalResults = ((Integer) in.readValue((Integer.class.getClassLoader())));
    }

    public ReviewsResponse() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public List<ReviewsResult> getReviewsResults() {
        return reviewsResults;
    }

    public void setReviewsResults(List<ReviewsResult> reviewsResults) {
        this.reviewsResults = reviewsResults;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(page);
        dest.writeList(reviewsResults);
        dest.writeValue(totalPages);
        dest.writeValue(totalResults);
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "ReviewsResponse{" +
                "id=" + id +
                ", page=" + page +
                ", reviewsResults=" + reviewsResults +
                ", totalPages=" + totalPages +
                ", totalResults=" + totalResults +
                '}';
    }
}
