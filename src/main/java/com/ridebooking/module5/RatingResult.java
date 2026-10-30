package main.java.com.ridebooking.module5;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Immutable result object returned by RatingsService operations.
 * Carries success/failure status, a human-readable message,
 * and optional data (the saved Rating and new average).
 *
 * Module 5 – Ratings
 */
public class RatingResult {

    // ---------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------
    private final boolean   success;
    private final String    message;
    private final Rating    rating;       // null on failure
    private final double    newAverage;   // 0.0 on failure

    // ---------------------------------------------------------------
    // Private Constructor
    // ---------------------------------------------------------------
    private RatingResult(boolean success, String message, Rating rating, double newAverage) {
        this.success    = success;
        this.message    = message;
        this.rating     = rating;
        this.newAverage = newAverage;
    }

    // ---------------------------------------------------------------
    // Factory Methods
    // ---------------------------------------------------------------

    /** Build a success result. */
    public static RatingResult success(String message, Rating rating, double newAverage) {
        return new RatingResult(true, message, rating, newAverage);
    }

    /** Build a failure result. */
    public static RatingResult failure(String message) {
        return new RatingResult(false, message, null, 0.0);
    }

    // ---------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------

    public boolean isSuccess()      { return success; }
    public String  getMessage()     { return message; }
    public Rating  getRating()      { return rating; }
    public double  getNewAverage()  { return newAverage; }

    @Override
    public String toString() {
        return (success ? "[SUCCESS] " : "[ERROR]   ") + message;
    }
}
