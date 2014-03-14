package com.shopzilla.service.manual.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by divakarbala on 2/20/14.
 */
public class Doc {

    private String pid;
    private String productTitle;
    private String category;
    private Collection<String> commentTitles;
    private Collection<String> commentTexts;
    private Collection<Double> commentRatings;

    private Collection<String> averageRating;
    private Collection<String> averageRatingHalf;
    private Integer reviewCount;

    @JsonProperty("PID")
    public void setPID(String pid) {
        this.pid = pid;
    }

    @JsonProperty("ProductTitle")
    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    @JsonProperty("Category")
    public void setCategory(String category) {
        this.category = category;
    }

    @JsonProperty("CommentTitle")
    public void setCommentTitles(Collection<String> commentTitles) {
        this.commentTitles = commentTitles;
    }

    @JsonProperty("CommentText")
    public void setCommentTexts(Collection<String> commentTexts) {
        this.commentTexts = commentTexts;
    }

    @JsonProperty("CommentRating")
    public void setCommentRatings(Collection<Double> commentRatings) { this.commentRatings = commentRatings; }

    @JsonProperty("ReviewCount")
    public void setReviewCount() {}

    @JsonProperty("AverageRating")
    public void setAverageRating() {}

    @JsonProperty("AverageRatingHalf")
    public void setAverageRatingHalf() {}

    public String getPID() {
        return this.pid;
    }

    public String getProductTitle() {
        return this.productTitle;
    }

    public String getCategory() {
        return this.category;
    }

    public Collection<String> getCommentTitles() { return this.commentTitles; }

    public Collection<String> getCommentTexts() {
        return this.commentTexts;
    }

    public Collection<Double> getCommentRatings() { return this.commentRatings; }

    public Integer getReviewCount() {
        return this.reviewCount;
    }

    public Collection<String> getAverageRating() {
        return this.averageRating;
    }

    public Collection<String> getAverageRatingHalf() {
        return this.averageRatingHalf;
    }
}
