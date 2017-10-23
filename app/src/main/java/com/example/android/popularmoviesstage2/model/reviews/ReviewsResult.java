package com.example.android.popularmoviesstage2.model.reviews;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReviewsResult implements Parcelable {

    public final static Creator<ReviewsResult> CREATOR = new Creator<ReviewsResult>() {


        @SuppressWarnings({
                "unchecked"
        })
        public ReviewsResult createFromParcel(Parcel in) {
            return new ReviewsResult(in);
        }

        public ReviewsResult[] newArray(int size) {
            return (new ReviewsResult[size]);
        }

    };
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("author")
    @Expose
    private String author;
    @SerializedName("content")
    @Expose
    private String content;
    @SerializedName("url")
    @Expose
    private String url;

    protected ReviewsResult(Parcel in) {
        this.id = ((String) in.readValue((String.class.getClassLoader())));
        this.author = ((String) in.readValue((String.class.getClassLoader())));
        this.content = ((String) in.readValue((String.class.getClassLoader())));
        this.url = ((String) in.readValue((String.class.getClassLoader())));
    }

    public ReviewsResult() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(author);
        dest.writeValue(content);
        dest.writeValue(url);
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "ReviewsResult{" +
                "id='" + id + '\'' +
                ", author='" + author + '\'' +
                ", content='" + content + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
