package com.amazonaws.samples.appconfig.model;

import java.time.LocalDateTime;

public class Rating {
    private long movieId;
    private int rating; // 1-5 stars
    private LocalDateTime timestamp;
    private String userId; // Optional user identifier

    public Rating() {
        this.timestamp = LocalDateTime.now();
    }

    public Rating(long movieId, int rating) {
        this.movieId = movieId;
        this.rating = rating;
        this.timestamp = LocalDateTime.now();
    }

    public Rating(long movieId, int rating, String userId) {
        this.movieId = movieId;
        this.rating = rating;
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