package com.amazonaws.samples.appconfig.movies;

public class Movie {

    private long id;
    private final String movieName;
    private double averageRating;
    private int reviewCount;

    public Movie(Long id, String movieName) {
        this.id = id;
        this.movieName = movieName;
        this.averageRating = 0.0;
        this.reviewCount = 0;
    }

    public Movie(Long id, String movieName, double averageRating, int reviewCount) {
        this.id = id;
        this.movieName = movieName;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public long getId() {
        return this.id;
    }

    public long setId(int movieId){
        return this.id = movieId;
    }
    
    public String getMovieName() {
        return this.movieName;
    }
    
    public double getAverageRating() {
        return this.averageRating;
    }
    
    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
    
    public int getReviewCount() {
        return this.reviewCount;
    }
    
    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }
}