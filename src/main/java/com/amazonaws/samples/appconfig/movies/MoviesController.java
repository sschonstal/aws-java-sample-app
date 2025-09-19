package com.amazonaws.samples.appconfig.movies;

import com.amazonaws.samples.appconfig.utils.MovieUtils;
import com.amazonaws.samples.appconfig.model.Rating;
import com.amazonaws.samples.appconfig.model.Review;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.amazonaws.samples.appconfig.utils.AppConfigUtility;
import com.amazonaws.samples.appconfig.cache.ConfigurationCache;
import com.amazonaws.samples.appconfig.model.ConfigurationKey;
import com.amazonaws.samples.appconfig.utils.HTMLBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.services.appconfig.AppConfigClient;
import software.amazon.awssdk.services.appconfig.model.GetConfigurationResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class MoviesController {
    private static final Logger logger = LogManager.getLogger(MoviesController.class);

    // In-memory storage for ratings and reviews (in production, this would be in a database)
    private final Map<Long, List<Rating>> movieRatings = new ConcurrentHashMap<>();
    private final Map<Long, List<Review>> movieReviews = new ConcurrentHashMap<>();

    /**
     * Static Movie Array containing all the list of Movies.
     */
    static final Movie[] PAIDMOVIES = {
        new Movie(1L, "Static Movie 1"),
        new Movie(2L, "Static Movie 2"),
        new Movie(3L, "Static Movie 3"),
        new Movie(4L, "Static Movie 4"),
        new Movie(5L, "Static Movie 5"),
        new Movie(6L, "Static Movie 6"),
        new Movie(7L, "Static Movie 7"),
        new Movie(8L, "Static Movie 8"),
        new Movie(9L, "Static Movie 9"),
        new Movie(10L, "Static Movie 10")
    };
    public Duration cacheItemTtl = Duration.ofSeconds(30);
    private Boolean boolEnableFeature;
    private int intItemLimit;
    AppConfigClient client;
    String clientId;
    ConfigurationCache cache;

    @Autowired
    Environment env;

    /**
     * REST API method to get all the Movies based on AWS App Config parameter.
     *
     * @return List of Movies
     */
    @GetMapping("/movies/getMovies")
    public String movie() {
        logger.info("Fetching movies from AWS App Config");
        try {


        cacheItemTtl = Duration.ofSeconds(Long.parseLong(env.getProperty("appconfig.cacheTtlInSeconds")));

        final AppConfigUtility appConfigUtility = new AppConfigUtility(getOrDefault(this::getClient, this::getDefaultClient),
                getOrDefault(this::getConfigurationCache, ConfigurationCache::new),
                getOrDefault(this::getCacheItemTtl, () -> cacheItemTtl),
                getOrDefault(this::getClientId, this::getDefaultClientId));

            final String application = env.getProperty("appconfig.application");
            final String environment = env.getProperty("appconfig.environment");
            final String config = env.getProperty("appconfig.config");
        final GetConfigurationResponse response = appConfigUtility.getConfiguration(new ConfigurationKey(application, environment, config));
        final String appConfigResponse = response.content().asUtf8String();

        final JSONObject jsonResponseObject = new JSONObject(appConfigResponse);
        System.out.println("json is "+jsonResponseObject);

        JSONArray moviesArray = jsonResponseObject.getJSONArray("movies");
        System.out.println("movies array is "+moviesArray);
        List<Movie> movieList = new ArrayList<>();
        for (int i = 0; i < moviesArray.length(); i++) {
            JSONObject movieObj = moviesArray.getJSONObject(i);
            long id = movieObj.getLong("id");
            String movieName = movieObj.getString("movieName");
            
            // Calculate rating information from in-memory storage
            double averageRating = calculateAverageRating(id);
            int reviewCount = getReviewCount(id);
            
            Movie movie = new Movie(id, movieName, averageRating, reviewCount);
            movieList.add(movie);
        }
        Movie[] movies = movieList.toArray(new Movie[movieList.size()]);
        HTMLBuilder htmlBuilder = new HTMLBuilder();
        String moviesHtml = htmlBuilder.getMoviesHtml(movies);

        return moviesHtml;
        } catch (Exception e) {
            logger.error("Error fetching movies from AWS App Config", e);
            HTMLBuilder htmlBuilder = new HTMLBuilder();
            String moviesHtml = htmlBuilder.getMoviesHtml(PAIDMOVIES);
            return moviesHtml;
        }
    }

    @RequestMapping(value = "/movies/{movie}/edit", method = POST)
    public String processUpdateMovie(@Valid Movie movie, BindingResult result, @PathVariable("movieId") int movieId) {
        if (!MovieUtils.isValidMovieName(movie.getMovieName())) {
            result.rejectValue("name", "error.name", "Invalid movie name");
            return "editMovieForm";
        }
        final AppConfigUtility appConfigUtility = new AppConfigUtility(getOrDefault(this::getClient, this::getDefaultClient),
                getOrDefault(this::getConfigurationCache, ConfigurationCache::new),
                getOrDefault(this::getCacheItemTtl, () -> cacheItemTtl),
                getOrDefault(this::getClientId, this::getDefaultClientId));


        final String application = env.getProperty("appconfig.application");
        final String environment = env.getProperty("appconfig.environment");
        final String config = env.getProperty("appconfig.config");

        final GetConfigurationResponse response = appConfigUtility.updateConfiguration(new ConfigurationKey(application, environment, config),movie.toString());
        final String appConfigResponse = response.content().asUtf8String();

        final JSONObject jsonResponseObject = new JSONObject(appConfigResponse);
        System.out.println("json is "+jsonResponseObject);

        JSONArray moviesArray = jsonResponseObject.getJSONArray("movies");
        System.out.println("movies array is "+moviesArray);
        List<Movie> movieList = new ArrayList<>();
        for (int i = 0; i < moviesArray.length(); i++) {
            JSONObject movieObj = moviesArray.getJSONObject(i);
            long id = movieObj.getLong("id");
            String movieName = movieObj.getString("movieName");
            // Extract other fields as needed
            movieList.add(movie);
        }
        Movie[] movies = movieList.toArray(new Movie[movieList.size()]);
        HTMLBuilder htmlBuilder = new HTMLBuilder();
        String moviesHtml = htmlBuilder.getMoviesHtml(movies);

        return moviesHtml;

    }

    /**
     * REST API endpoint to submit a rating for a movie.
     *
     * @param movieId the ID of the movie to rate
     * @param ratingRequest the rating request containing the rating value
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/movies/{id}/rate")
    public ResponseEntity<Map<String, Object>> rateMovie(@PathVariable("id") Long movieId, @RequestBody Map<String, Object> ratingRequest) {
        logger.info("Submitting rating for movie ID: {}", movieId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate rating value
            Object ratingObj = ratingRequest.get("rating");
            if (ratingObj == null) {
                response.put("error", "Rating value is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            int ratingValue;
            if (ratingObj instanceof Integer) {
                ratingValue = (Integer) ratingObj;
            } else if (ratingObj instanceof Double) {
                ratingValue = ((Double) ratingObj).intValue();
            } else {
                response.put("error", "Invalid rating format");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (ratingValue < 1 || ratingValue > 5) {
                response.put("error", "Rating must be between 1 and 5 stars");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if movie exists (basic validation)
            if (!movieExists(movieId)) {
                response.put("error", "Movie not found");
                return ResponseEntity.notFound().build();
            }
            
            // Create and store rating
            String userId = (String) ratingRequest.get("userId"); // Optional
            Rating rating = new Rating(movieId, ratingValue, userId);
            
            movieRatings.computeIfAbsent(movieId, k -> new ArrayList<>()).add(rating);
            
            // Calculate new average
            double newAverage = calculateAverageRating(movieId);
            int totalRatings = movieRatings.get(movieId).size();
            
            response.put("success", true);
            response.put("message", "Rating submitted successfully");
            response.put("movieId", movieId);
            response.put("rating", ratingValue);
            response.put("averageRating", newAverage);
            response.put("totalRatings", totalRatings);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error submitting rating for movie ID: {}", movieId, e);
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * REST API endpoint to retrieve reviews for a movie.
     *
     * @param movieId the ID of the movie
     * @return ResponseEntity with reviews data
     */
    @GetMapping("/movies/{id}/reviews")
    public ResponseEntity<Map<String, Object>> getMovieReviews(@PathVariable("id") Long movieId) {
        logger.info("Fetching reviews for movie ID: {}", movieId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if movie exists
            if (!movieExists(movieId)) {
                response.put("error", "Movie not found");
                return ResponseEntity.notFound().build();
            }
            
            List<Review> reviews = movieReviews.getOrDefault(movieId, new ArrayList<>());
            List<Rating> ratings = movieRatings.getOrDefault(movieId, new ArrayList<>());
            
            // Convert reviews to response format
            List<Map<String, Object>> reviewsData = reviews.stream()
                .map(review -> {
                    Map<String, Object> reviewData = new HashMap<>();
                    reviewData.put("rating", review.getRating());
                    reviewData.put("reviewText", review.getReviewText());
                    reviewData.put("timestamp", review.getTimestamp().toString());
                    if (review.getUserId() != null) {
                        reviewData.put("userId", review.getUserId());
                    }
                    return reviewData;
                })
                .collect(Collectors.toList());
            
            response.put("movieId", movieId);
            response.put("reviews", reviewsData);
            response.put("averageRating", calculateAverageRating(movieId));
            response.put("totalRatings", ratings.size());
            response.put("totalReviews", reviews.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching reviews for movie ID: {}", movieId, e);
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * REST API endpoint to submit a review for a movie.
     *
     * @param movieId the ID of the movie to review
     * @param reviewRequest the review request containing rating and review text
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/movies/{id}/review")
    public ResponseEntity<Map<String, Object>> submitReview(@PathVariable("id") Long movieId, @RequestBody Map<String, Object> reviewRequest) {
        logger.info("Submitting review for movie ID: {}", movieId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate rating value
            Object ratingObj = reviewRequest.get("rating");
            if (ratingObj == null) {
                response.put("error", "Rating value is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            int ratingValue;
            if (ratingObj instanceof Integer) {
                ratingValue = (Integer) ratingObj;
            } else if (ratingObj instanceof Double) {
                ratingValue = ((Double) ratingObj).intValue();
            } else {
                response.put("error", "Invalid rating format");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (ratingValue < 1 || ratingValue > 5) {
                response.put("error", "Rating must be between 1 and 5 stars");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate review text
            String reviewText = (String) reviewRequest.get("reviewText");
            if (reviewText == null || reviewText.trim().isEmpty()) {
                response.put("error", "Review text is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if movie exists
            if (!movieExists(movieId)) {
                response.put("error", "Movie not found");
                return ResponseEntity.notFound().build();
            }
            
            // Create and store review
            String userId = (String) reviewRequest.get("userId"); // Optional
            Review review = new Review(movieId, ratingValue, reviewText.trim(), userId);
            
            movieReviews.computeIfAbsent(movieId, k -> new ArrayList<>()).add(review);
            
            // Also add the rating to the ratings collection
            Rating rating = new Rating(movieId, ratingValue, userId);
            movieRatings.computeIfAbsent(movieId, k -> new ArrayList<>()).add(rating);
            
            // Calculate new average
            double newAverage = calculateAverageRating(movieId);
            int totalRatings = movieRatings.get(movieId).size();
            int totalReviews = movieReviews.get(movieId).size();
            
            response.put("success", true);
            response.put("message", "Review submitted successfully");
            response.put("movieId", movieId);
            response.put("rating", ratingValue);
            response.put("reviewText", reviewText.trim());
            response.put("averageRating", newAverage);
            response.put("totalRatings", totalRatings);
            response.put("totalReviews", totalReviews);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error submitting review for movie ID: {}", movieId, e);
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Helper method to calculate average rating for a movie.
     */
    private double calculateAverageRating(Long movieId) {
        List<Rating> ratings = movieRatings.get(movieId);
        if (ratings == null || ratings.isEmpty()) {
            return 0.0;
        }
        
        double sum = ratings.stream().mapToInt(Rating::getRating).sum();
        return Math.round((sum / ratings.size()) * 10.0) / 10.0; // Round to 1 decimal place
    }

    /**
     * Helper method to get review count for a movie.
     */
    private int getReviewCount(Long movieId) {
        List<Review> reviews = movieReviews.get(movieId);
        return reviews != null ? reviews.size() : 0;
    }

    /**
     * Helper method to check if a movie exists.
     */
    private boolean movieExists(Long movieId) {
        // Check static movies first
        for (Movie movie : PAIDMOVIES) {
            if (movie.getId() == movieId) {
                return true;
            }
        }
        
        // In a real implementation, you would also check AppConfig data
        // For now, we'll assume movies with IDs 1-10 exist
        return movieId >= 1 && movieId <= 10;
    }

    private <T> T getOrDefault(final Supplier<T> optionalGetter, final Supplier<T> defaultGetter) {
        return Optional.ofNullable(optionalGetter.get()).orElseGet(defaultGetter);
    }

    String getDefaultClientId() {
        return UUID.randomUUID().toString();
    }

    protected AppConfigClient getDefaultClient() {
        return AppConfigClient.create();
    }

    public ConfigurationCache getConfigurationCache() {
        return cache;
    }


    public AppConfigClient getClient() {
        return client;
    }


    public Duration getCacheItemTtl() {
        return cacheItemTtl;
    }

    public String getClientId() {
        return clientId;
    }


}