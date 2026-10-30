package main.java.com.ridebooking.module5;

import java.time.LocalDateTime;

/**
 * Represents a single rating given by one user (rater) to another (ratee)
 * after a completed trip.
 *
 * Module 5 – Ratings
 */
public class Rating {

    // ---------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;

    // ---------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------
    private final String ratingId;       // Unique ID for this rating
    private final String tripId;         // Trip this rating belongs to
    private final String raterId;        // ID of the person giving the rating
    private final String raterType;      // "RIDER" or "DRIVER"
    private final String rateeId;        // ID of the person receiving the rating
    private final String rateeType;      // "RIDER" or "DRIVER"
    private final int score;             // 1 – 5
    private final LocalDateTime submittedAt; // When the rating was submitted

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------
    public Rating(String ratingId,
                  String tripId,
                  String raterId,
                  String raterType,
                  String rateeId,
                  String rateeType,
                  int score,
                  LocalDateTime submittedAt) {
        this.ratingId      = ratingId;
        this.tripId        = tripId;
        this.raterId       = raterId;
        this.raterType     = raterType;
        this.rateeId       = rateeId;
        this.rateeType     = rateeType;
        this.score         = score;
        this.submittedAt   = submittedAt;
    }

    // ---------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------
    public String getRatingId()        { return ratingId; }
    public String getTripId()          { return tripId; }
    public String getRaterId()         { return raterId; }
    public String getRaterType()       { return raterType; }
    public String getRateeId()         { return rateeId; }
    public String getRateeType()       { return rateeType; }
    public int getScore()              { return score; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }

    // ---------------------------------------------------------------
    // Display
    // ---------------------------------------------------------------
    @Override
    public String toString() {
        return String.format(
            "[Rating #%s] Trip: %s | %s (%s) → %s (%s) | Score: %d/5 | At: %s",
            ratingId, tripId,
            raterId, raterType,
            rateeId, rateeType,
            score,
            submittedAt
        );
    }
}
