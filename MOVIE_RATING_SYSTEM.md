# Movie Rating and Review System

This document describes the new movie rating and review functionality that has been added to the Movie Service.

## New Features

### 1. Enhanced Movie Model
- Added `averageRating` (double) field to store the calculated average rating
- Added `reviewCount` (int) field to store the number of reviews
- Backward compatible with existing movie data

### 2. New REST Endpoints

#### Submit a Rating
**POST** `/movies/{id}/rate`

Submit a rating (1-5 stars) for a movie.

**Request Body:**
```json
{
  "rating": 4,
  "userId": "user123" // optional
}
```

**Response:**
```json
{
  "success": true,
  "message": "Rating submitted successfully",
  "movieId": 1,
  "rating": 4,
  "averageRating": 4.2,
  "totalRatings": 5
}
```

#### Submit a Review
**POST** `/movies/{id}/review`

Submit a review with rating and text for a movie.

**Request Body:**
```json
{
  "rating": 5,
  "reviewText": "Great movie! Highly recommended.",
  "userId": "user123" // optional
}
```

**Response:**
```json
{
  "success": true,
  "message": "Review submitted successfully",
  "movieId": 1,
  "rating": 5,
  "reviewText": "Great movie! Highly recommended.",
  "averageRating": 4.3,
  "totalRatings": 6,
  "totalReviews": 3
}
```

#### Get Movie Reviews
**GET** `/movies/{id}/reviews`

Retrieve all reviews for a specific movie.

**Response:**
```json
{
  "movieId": 1,
  "reviews": [
    {
      "rating": 5,
      "reviewText": "Great movie! Highly recommended.",
      "timestamp": "2023-12-07T10:30:00",
      "userId": "user123"
    }
  ],
  "averageRating": 4.3,
  "totalRatings": 6,
  "totalReviews": 3
}
```

### 3. Enhanced HTML Display
The movie list now displays:
- Star ratings (★★★★☆)
- Average rating score (e.g., "4.3/5")
- Number of reviews (e.g., "(12 reviews)")
- "No ratings yet" for movies without ratings

## Testing the System

### 1. Start the Application
```bash
mvn spring-boot:run
```

### 2. View Movies
Navigate to: `http://localhost:8080/movies/getMovies`

### 3. Submit a Rating
```bash
curl -X POST http://localhost:8080/movies/1/rate \
  -H "Content-Type: application/json" \
  -d '{"rating": 4, "userId": "testuser"}'
```

### 4. Submit a Review
```bash
curl -X POST http://localhost:8080/movies/1/review \
  -H "Content-Type: application/json" \
  -d '{"rating": 5, "reviewText": "Excellent movie!", "userId": "testuser"}'
```

### 5. Get Reviews
```bash
curl http://localhost:8080/movies/1/reviews
```

## Data Storage

Currently, ratings and reviews are stored in memory using `ConcurrentHashMap` for thread safety. In a production environment, this data should be persisted to:
- AWS AppConfig (as additional JSON properties)
- Database (recommended for scalability)
- AWS DynamoDB or RDS

## Implementation Details

### Model Classes
- `Movie.java` - Enhanced with rating fields
- `Rating.java` - Individual rating model
- `Review.java` - Review with rating and text

### Controller Enhancements
- `MoviesController.java` - Added new endpoints and rating calculation logic
- Thread-safe in-memory storage using `ConcurrentHashMap`
- Input validation for ratings (1-5 range)
- Error handling for invalid movie IDs

### HTML Display
- `HTMLBuilder.java` - Enhanced with star display and CSS styling
- Responsive star rating display (★☆)
- Graceful handling of movies without ratings

## Validation Rules

1. **Rating Values**: Must be integers between 1 and 5 (inclusive)
2. **Review Text**: Required for review submissions, cannot be empty
3. **Movie ID**: Must exist in the system (currently IDs 1-10 are valid)
4. **User ID**: Optional field for tracking user submissions

## Error Handling

The system provides appropriate HTTP status codes and error messages:
- `400 Bad Request` - Invalid rating values or missing required fields
- `404 Not Found` - Movie ID does not exist
- `500 Internal Server Error` - Unexpected server errors

## Future Enhancements

1. **Persistent Storage**: Integrate with AWS AppConfig or database
2. **User Authentication**: Implement proper user management
3. **Rate Limiting**: Prevent spam ratings from same user
4. **Review Moderation**: Content filtering for inappropriate reviews
5. **Pagination**: For movies with many reviews
6. **Search and Filter**: Filter movies by rating range