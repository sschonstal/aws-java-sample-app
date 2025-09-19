package com.amazonaws.samples.appconfig.model;

import java.time.LocalDateTime;

public class Review {
    private long movieId;
    private int rating; // 1-5 stars
    private String reviewText;
    private LocalDateTime timestamp;
    private String userId; // Optional user identifier

    public Review() {
        this.timestamp = LocalDateTime.now();
    }

    public Review(long movieId, int rating, String reviewText) {
        this.movieId = movieId;
        this.rating = rating;
        this.reviewText = reviewText;
        this.timestamp = LocalDateTime.now();
    }

    public Review(long movieId, int rating, String reviewText, String userId) {
        this.movieId = movieId;
        this.rating = rating;
        this.reviewText = reviewText;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }

    public long getMovieId() {
        return movieId;
    }

    public void setMovieId(long movieId) {
        this.movieId = movieId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars");
        }
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isValidRating() {
        return rating >= 1 && rating <= 5;
    }
}