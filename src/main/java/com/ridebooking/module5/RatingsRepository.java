package com.ridebooking.module5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory repository for all ratings and computed statistics.
 *
 * Stores:
 *   - All Rating objects (keyed by ratingId)
 *   - Per-user rating history (keyed by userId)
 *   - Running totals to compute average ratings efficiently
 *   - Set of submitted (tripId + raterId) pairs to enforce one-rating-per-trip
 *   - Flagged-rider list for admin review
 *
 * Module 5 – Ratings
 */
public class RatingsRepository {

    // ---------------------------------------------------------------
    // All ratings: ratingId → Rating
    // ---------------------------------------------------------------
    private final Map<String, Rating> allRatings = new HashMap<>();

    // ---------------------------------------------------------------
    // Ratings received by a user: userId → list of ratings
    // ---------------------------------------------------------------
    private final Map<String, List<Rating>> ratingsReceivedByUser = new HashMap<>();

    // ---------------------------------------------------------------
    // Duplicate-guard: "tripId::raterId" → true if already submitted
    // ---------------------------------------------------------------
    private final Map<String, Boolean> submissionGuard = new HashMap<>();

    // ---------------------------------------------------------------
    // Running totals for average calculation: userId → [totalScore, count]
    // ---------------------------------------------------------------
    private final Map<String, int[]> userRatingTotals = new HashMap<>();

    // ---------------------------------------------------------------
    // Flagged riders: riderId → reason
    // ---------------------------------------------------------------
    private final Map<String, String> flaggedRiders = new HashMap<>();

    // ---------------------------------------------------------------
    // Auto-increment counter for ratingId generation
    // ---------------------------------------------------------------
    private int counter = 1;

    // ---------------------------------------------------------------
    // Public Methods
    // ---------------------------------------------------------------

    /** Generate a unique rating ID. */
    public String generateRatingId() {
        return "R" + (counter++);
    }

    /**
     * Persist a new rating.
     *
     * @param rating the Rating to save
     */
    public void saveRating(Rating rating) {
        allRatings.put(rating.getRatingId(), rating);

        // Track per-user
        ratingsReceivedByUser
            .computeIfAbsent(rating.getRateeId(), k -> new ArrayList<>())
            .add(rating);

        // Update running totals
        int[] totals = userRatingTotals.computeIfAbsent(
            rating.getRateeId(), k -> new int[]{0, 0}
        );
        totals[0] += rating.getScore(); // sum
        totals[1] += 1;                 // count

        // Mark submission guard
        submissionGuard.put(buildGuardKey(rating.getTripId(), rating.getRaterId()), true);
    }

    /**
     * Check whether a rater has already rated for a given trip.
     *
     * @param tripId  the trip ID
     * @param raterId the rater's user ID
     * @return true if already rated
     */
    public boolean hasAlreadyRated(String tripId, String raterId) {
        return submissionGuard.containsKey(buildGuardKey(tripId, raterId));
    }

    /**
     * Get average rating for a user.
     *
     * @param userId the user ID
     * @return average, or 0.0 if no ratings yet
     */
    public double getAverageRating(String userId) {
        int[] totals = userRatingTotals.get(userId);
        if (totals == null || totals[1] == 0) return 0.0;
        return (double) totals[0] / totals[1];
    }

    /**
     * Get total number of ratings received by a user.
     *
     * @param userId the user ID
     * @return count
     */
    public int getRatingCount(String userId) {
        int[] totals = userRatingTotals.get(userId);
        return (totals == null) ? 0 : totals[1];
    }

    /**
     * Retrieve all ratings received by a user.
     *
     * @param userId the user ID
     * @return list of ratings (may be empty)
     */
    public List<Rating> getRatingsForUser(String userId) {
        return ratingsReceivedByUser.getOrDefault(userId, new ArrayList<>());
    }

    /**
     * Retrieve all ratings in the system.
     *
     * @return list of all Rating objects
     */
    public List<Rating> getAllRatings() {
        return new ArrayList<>(allRatings.values());
    }

    /**
     * Flag a rider for admin review.
     *
     * @param riderId the rider's ID
     * @param reason  the reason for flagging
     */
    public void flagRider(String riderId, String reason) {
        flaggedRiders.put(riderId, reason);
    }

    /**
     * Check if a rider is already flagged.
     *
     * @param riderId the rider's ID
     * @return true if flagged
     */
    public boolean isFlagged(String riderId) {
        return flaggedRiders.containsKey(riderId);
    }

    /**
     * Get all flagged riders.
     *
     * @return map of riderId → reason
     */
    public Map<String, String> getFlaggedRiders() {
        return new HashMap<>(flaggedRiders);
    }

    // ---------------------------------------------------------------
    // Private Helpers
    // ---------------------------------------------------------------

    private String buildGuardKey(String tripId, String raterId) {
        return tripId + "::" + raterId;
    }
}
