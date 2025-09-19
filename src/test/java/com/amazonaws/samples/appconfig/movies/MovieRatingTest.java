package com.amazonaws.samples.appconfig.movies;

import com.amazonaws.samples.appconfig.model.Rating;
import com.amazonaws.samples.appconfig.model.Review;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class MovieRatingTest {

    private Movie movie;
    private Rating rating;
    private Review review;

    @Before
    public void setUp() {
        movie = new Movie(1L, "Test Movie");
        rating = new Rating(1L, 4);
        review = new Review(1L, 5, "Great movie!");
    }

    @Test
    public void testMovieConstructorWithRating() {
        Movie movieWithRating = new Movie(1L, "Test Movie", 4.5, 10);
        assertEquals(1L, movieWithRating.getId());
        assertEquals("Test Movie", movieWithRating.getMovieName());
        assertEquals(4.5, movieWithRating.getAverageRating(), 0.01);
        assertEquals(10, movieWithRating.getReviewCount());
    }

    @Test
    public void testMovieDefaultRating() {
        assertEquals(0.0, movie.getAverageRating(), 0.01);
        assertEquals(0, movie.getReviewCount());
    }

    @Test
    public void testMovieSetRating() {
        movie.setAverageRating(3.8);
        movie.setReviewCount(5);
        assertEquals(3.8, movie.getAverageRating(), 0.01);
        assertEquals(5, movie.getReviewCount());
    }

    @Test
    public void testRatingValidation() {
        assertTrue(rating.isValidRating());
        
        Rating invalidRating = new Rating();
        invalidRating.setMovieId(1L);
        
        try {
            invalidRating.setRating(0); // Should throw exception
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Rating must be between 1 and 5 stars", e.getMessage());
        }
        
        try {
            invalidRating.setRating(6); // Should throw exception
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Rating must be between 1 and 5 stars", e.getMessage());
        }
    }

    @Test
    public void testRatingProperties() {
        assertEquals(1L, rating.getMovieId());
        assertEquals(4, rating.getRating());
        assertNotNull(rating.getTimestamp());
        assertNull(rating.getUserId());
    }

    @Test
    public void testRatingWithUserId() {
        Rating userRating = new Rating(1L, 5, "user123");
        assertEquals(1L, userRating.getMovieId());
        assertEquals(5, userRating.getRating());
        assertEquals("user123", userRating.getUserId());
        assertNotNull(userRating.getTimestamp());
    }

    @Test
    public void testReviewValidation() {
        assertTrue(review.isValidRating());
        
        try {
            review.setRating(0); // Should throw exception
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Rating must be between 1 and 5 stars", e.getMessage());
        }
    }

    @Test
    public void testReviewProperties() {
        assertEquals(1L, review.getMovieId());
        assertEquals(5, review.getRating());
        assertEquals("Great movie!", review.getReviewText());
        assertNotNull(review.getTimestamp());
        assertNull(review.getUserId());
    }

    @Test
    public void testReviewWithUserId() {
        Review userReview = new Review(1L, 4, "Good movie", "user456");
        assertEquals(1L, userReview.getMovieId());
        assertEquals(4, userReview.getRating());
        assertEquals("Good movie", userReview.getReviewText());
        assertEquals("user456", userReview.getUserId());
        assertNotNull(userReview.getTimestamp());
    }
}