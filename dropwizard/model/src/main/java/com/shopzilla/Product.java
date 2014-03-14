package com.shopzilla;

/**
 * Created by divakarbala on 2/28/14.
 */

import org.apache.solr.client.solrj.beans.Field;

import java.util.Collection;
import java.util.List;

public class Product {
    @Field
    private String pid;

    @Field
    private String productTitle;

    @Field
    private String category;

    @Field
    private String[] commentTitles;

    @Field
    private String[] commentTexts;

    @Field
    private Double[] commentRatings;

    @Field
    private List<String> averageRating;

    @Field
    private List<String> averageRatingHalf;

    @Field
    private Integer reviewCount;

    @Field("PID")
    public void setPID(String pid) {
        this.pid = pid;
    }

    @Field("ProductTitle")
    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    @Field("Category")
    public void setCategory(String category) {
        this.category = category;
    }

    @Field("CommentTitle")
    public void setCommentTitles(String[] commentTitles) { this.commentTitles = commentTitles; }

    @Field("CommentText")
    public void setCommentTexts(String[] commentTexts) { this.commentTexts = commentTexts; }

    @Field("CommentRating")
    public void setCommentRatings(Double[] commentRatings) { this.commentRatings = commentRatings; }

    @Field("ReviewCount")
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

    @Field("AverageRating")
    public void setAverageRating(List<String> averageRating) {
        this.averageRating = averageRating;
    }

    @Field("AverageRatingHalf")
    public void setAverageRatingHalf(List<String> averageRatingHalf) {
        this.averageRatingHalf = averageRatingHalf;
    }

    public String getPID() {
        return this.pid;
    }

    public String getProductTitle() {
        return this.productTitle;
    }

    public String getCategory() {
        return this.category;
    }

    public String[] getCommentTitles() {
        return this.commentTitles;
    }

    public String[] getCommentTexts() { return this.commentTexts; }

    public Double[] getCommentRatings() { return this.commentRatings; }

    public Integer getReviewCount() {
        return this.reviewCount;
    }
    public List<String> getAverageRating() {
        return this.averageRating;
    }
    public List<String> getAverageRatingHalf() {
        return this.averageRatingHalf;
    }
}
