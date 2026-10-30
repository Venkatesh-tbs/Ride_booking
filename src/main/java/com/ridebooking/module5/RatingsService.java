package com.ridebooking.module5;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Core business logic for Module 5 – Ratings.
 *
 * Rules enforced:
 *   1. Rating score must be between 1 and 5 (inclusive).
 *   2. Only one rating per person per trip.
 *   3. Rating must be submitted within 24 hours of trip completion.
 *   4. Average ratings are recalculated after every new rating.
 *   5. If a RIDER's average rating falls below 3.0 after at least
 *      10 completed trips, they are flagged for admin review.
 *
 * Integration note:
 *   This service works with String-based IDs (tripId, riderId, driverId).
 *   When integrated with other modules, pass the actual IDs from those modules.
 *   The tripCompletedAt timestamp should be provided by Module 4 (Trip Management).
 */
public class RatingsService {

    // ---------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------
    private static final int    FLAG_THRESHOLD_TRIPS  = 10;   // min trips before flagging
    private static final double FLAG_AVG_THRESHOLD    = 3.0;  // flag if avg below this
    private static final long   WINDOW_HOURS          = 24;   // rating window in hours

    // ---------------------------------------------------------------
    // Dependency
    // ---------------------------------------------------------------
    private final RatingsRepository repository;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------
    public RatingsService(RatingsRepository repository) {
        this.repository = repository;
    }

    // ---------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------

    /**
     * Submit a rating from a rider to a driver after trip completion.
     *
     * @param tripId           the completed trip's ID
     * @param riderId          the rider (rater) ID
     * @param driverId         the driver (ratee) ID
     * @param score            rating score (1–5)
     * @param tripCompletedAt  when the trip ended (for 24-hour window check)
     * @return RatingResult describing success or failure with a message
     */
    public RatingResult riderRatesDriver(String tripId,
                                         String riderId,
                                         String driverId,
                                         int score,
                                         LocalDateTime tripCompletedAt) {
        return submitRating(tripId, riderId, "RIDER", driverId, "DRIVER",
                            score, tripCompletedAt);
    }

    /**
     * Submit a rating from a driver to a rider after trip completion.
     *
     * @param tripId           the completed trip's ID
     * @param driverId         the driver (rater) ID
     * @param riderId          the rider (ratee) ID
     * @param score            rating score (1–5)
     * @param tripCompletedAt  when the trip ended (for 24-hour window check)
     * @return RatingResult describing success or failure with a message
     */
    public RatingResult driverRatesRider(String tripId,
                                          String driverId,
                                          String riderId,
                                          int score,
                                          LocalDateTime tripCompletedAt) {
        return submitRating(tripId, driverId, "DRIVER", riderId, "RIDER",
                            score, tripCompletedAt);
    }

    /**
     * Get the current average rating for any user (rider or driver).
     *
     * @param userId the user's ID
     * @return average rating, or 0.0 if no ratings exist
     */
    public double getAverageRating(String userId) {
        return repository.getAverageRating(userId);
    }

    /**
     * Get total number of ratings received by a user.
     *
     * @param userId the user's ID
     * @return count
     */
    public int getRatingCount(String userId) {
        return repository.getRatingCount(userId);
    }

    /**
     * Get all ratings received by a specific user.
     *
     * @param userId the user's ID
     * @return list of Rating objects
     */
    public List<Rating> getRatingsForUser(String userId) {
        return repository.getRatingsForUser(userId);
    }

    /**
     * Get all ratings in the system.
     *
     * @return list of all Rating objects
     */
    public List<Rating> getAllRatings() {
        return repository.getAllRatings();
    }

    /**
     * Check if a rider is flagged for admin review.
     *
     * @param riderId the rider's ID
     * @return true if flagged
     */
    public boolean isRiderFlagged(String riderId) {
        return repository.isFlagged(riderId);
    }

    /**
     * Get all flagged riders.
     *
     * @return map of riderId → reason string
     */
    public Map<String, String> getFlaggedRiders() {
        return repository.getFlaggedRiders();
    }

    // ---------------------------------------------------------------
    // Private Core Logic
    // ---------------------------------------------------------------

    /**
     * Common internal method that validates and persists a rating.
     */
    private RatingResult submitRating(String tripId,
                                       String raterId,
                                       String raterType,
                                       String rateeId,
                                       String rateeType,
                                       int score,
                                       LocalDateTime tripCompletedAt) {

        // --- Validation 1: score range ---
        if (score < Rating.MIN_RATING || score > Rating.MAX_RATING) {
            return RatingResult.failure(
                "Invalid score: " + score + ". Rating must be between "
                + Rating.MIN_RATING + " and " + Rating.MAX_RATING + "."
            );
        }

        // --- Validation 2: one rating per person per trip ---
        if (repository.hasAlreadyRated(tripId, raterId)) {
            return RatingResult.failure(
                "You have already submitted a rating for trip [" + tripId + "]."
            );
        }

        // --- Validation 3: 24-hour submission window ---
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = tripCompletedAt.plusHours(WINDOW_HOURS);
        if (now.isAfter(deadline)) {
            return RatingResult.failure(
                "Rating window expired. Ratings must be submitted within "
                + WINDOW_HOURS + " hours of trip completion."
            );
        }

        // --- Persist the rating ---
        String ratingId = repository.generateRatingId();
        Rating rating = new Rating(
            ratingId, tripId,
            raterId, raterType,
            rateeId, rateeType,
            score, now
        );
        repository.saveRating(rating);

        // --- Recalculate and display updated average ---
        double newAverage = repository.getAverageRating(rateeId);
        int totalRatings  = repository.getRatingCount(rateeId);

        // --- Flagging check (only for RIDERS receiving ratings) ---
        String flagMessage = "";
        if ("RIDER".equals(rateeType)) {
            flagMessage = evaluateRiderForFlag(rateeId, newAverage, totalRatings);
        }

        String successMsg = String.format(
            "Rating submitted successfully! [ID: %s]\n"
            + "  %s (%s) rated %s (%s): %d/5\n"
            + "  %s's updated average: %.2f/5 (based on %d rating%s)%s",
            ratingId,
            raterId, raterType,
            rateeId, rateeType,
            score,
            rateeId, newAverage, totalRatings,
            totalRatings == 1 ? "" : "s",
            flagMessage
        );

        return RatingResult.success(successMsg, rating, newAverage);
    }

    /**
     * Evaluate if a rider should be flagged for admin review.
     * Flags when: avgRating < 3.0 AND totalRatings >= 10.
     *
     * @param riderId      the rider's ID
     * @param avgRating    the newly computed average
     * @param totalRatings total number of ratings received so far
     * @return a flag notice string, or empty string if no flag needed
     */
    private String evaluateRiderForFlag(String riderId,
                                         double avgRating,
                                         int totalRatings) {
        if (totalRatings >= FLAG_THRESHOLD_TRIPS
                && avgRating < FLAG_AVG_THRESHOLD
                && !repository.isFlagged(riderId)) {

            String reason = String.format(
                "Average rating %.2f/5 (below %.1f) after %d completed trips.",
                avgRating, FLAG_AVG_THRESHOLD, totalRatings
            );
            repository.flagRider(riderId, reason);

            return "\n  *** ADMIN ALERT: Rider [" + riderId
                   + "] has been flagged for review. Reason: " + reason + " ***";
        }
        return "";
    }
}
