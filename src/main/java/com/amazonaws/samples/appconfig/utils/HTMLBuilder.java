package com.amazonaws.samples.appconfig.utils;
import com.amazonaws.samples.appconfig.movies.Movie;
public class HTMLBuilder {

//    public String getMoviesHtml(Movie[] movies) {
//        StringBuilder htmlBuilder = new StringBuilder();
//        htmlBuilder.append("<div id='movies-container'>");
//        htmlBuilder.append("<h1> FREE Movie List for this Month</h1>");
//        for (Movie movie : movies) {
//            htmlBuilder.append("<div class='movie-item'>");
//            htmlBuilder.append("<p>ID: ").append(movie.getId()).append("</p>");
//            htmlBuilder.append("<h3>").append(movie.getMovieName()).append("</h3>");
//            htmlBuilder.append("<hr width=\"100%\" size=\"2\" color=\"blue\" noshade>");
//            // Add other movie details as needed
//            htmlBuilder.append("</div>");
//        }
//        htmlBuilder.append("<hr>");
//        htmlBuilder.append("<hr width=\"100%\" size=\"2\" color=\"blue\" noshade>");
//
//        return htmlBuilder.toString();
//    }

    public String getMoviesHtml(Movie[] movies) {
        String htmlBuilder = "<style>"
                + ".movie-item { margin: 15px 0; padding: 10px; border: 1px solid #ddd; border-radius: 5px; }"
                + ".rating-info { margin: 10px 0; }"
                + ".stars { font-size: 18px; color: #ffa500; }"
                + ".rating-text { font-weight: bold; color: #333; }"
                + ".review-count { color: #666; font-style: italic; }"
                + ".no-rating { color: #999; font-style: italic; }"
                + "h1 { color: #2c3e50; }"
                + "h3 { color: #34495e; margin: 5px 0; }"
                + "</style>"
                + "<div id='movies-container'>"
                + "<h1> FREE Movie List for this Month</h1>"
                + getMovieItemsHtml(movies)
                + "<hr>"
                + "<hr width=\"100%\" size=\"2\" color=\"blue\" noshade>"
                + "</div>";
        return htmlBuilder;
    }

    private static String getMovieItemsHtml(Movie[] movies) {
        StringBuilder movieItemsHtml = new StringBuilder();
        for (Movie movie : movies) {
            movieItemsHtml.append("<div class='movie-item'>"
                    + "<p>ID: ").append(movie.getId()).append("</p>"
                    + "<h3>").append(movie.getMovieName()).append("</h3>");
            
            // Add rating information
            if (movie.getAverageRating() > 0) {
                movieItemsHtml.append("<div class='rating-info'>"
                        + "<span class='stars'>").append(getStarsHtml(movie.getAverageRating())).append("</span>"
                        + "<span class='rating-text'> ").append(String.format("%.1f", movie.getAverageRating())).append("/5</span>"
                        + "<span class='review-count'> (").append(movie.getReviewCount()).append(" reviews)</span>"
                        + "</div>");
            } else {
                movieItemsHtml.append("<div class='rating-info'>"
                        + "<span class='no-rating'>No ratings yet</span>"
                        + "</div>");
            }
            
            movieItemsHtml.append("<hr width=\"100%\" size=\"2\" color=\"blue\" noshade>"
                    + "</div>");
        }
        return movieItemsHtml.toString();
    }

    /**
     * Generate HTML for star rating display
     */
    private static String getStarsHtml(double rating) {
        StringBuilder stars = new StringBuilder();
        int fullStars = (int) rating;
        boolean hasHalfStar = (rating - fullStars) >= 0.5;
        
        // Add full stars
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }
        
        // Add half star if needed
        if (hasHalfStar && fullStars < 5) {
            stars.append("☆");
            fullStars++;
        }
        
        // Add empty stars to make 5 total
        for (int i = fullStars; i < 5; i++) {
            stars.append("☆");
        }
        
        return stars.toString();
    }

}
