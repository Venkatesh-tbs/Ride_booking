package com.ridebooking.module4;

/**
 * Determines whether surge pricing should be applied and returns the surge multiplier.
 *
 * Surge rule:
 *   If pendingRequests > 2 * availableDrivers  →  apply 1.5x surge.
 *   Otherwise                                  →  1.0x (no surge).
 */
public class SurgePricingService {

    public static final double SURGE_MULTIPLIER = 1.5;
    public static final double NORMAL_MULTIPLIER = 1.0;
    public static final int    SURGE_RATIO       = 2;   // pendingRequests > ratio * availableDrivers

    /**
     * Evaluates whether surge should be applied.
     *
     * @param pendingRequests  number of pending ride requests in the system
     * @param availableDrivers number of drivers currently available
     * @return 1.5 if surge conditions are met, 1.0 otherwise
     */
    public double getSurgeMultiplier(int pendingRequests, int availableDrivers) {
        if (pendingRequests < 0 || availableDrivers < 0) {
            throw new IllegalArgumentException("Counts cannot be negative.");
        }
        // Guard: if there are no drivers, treat as surge to discourage ride requests
        if (availableDrivers == 0) {
            return SURGE_MULTIPLIER;
        }
        if (pendingRequests > SURGE_RATIO * availableDrivers) {
            return SURGE_MULTIPLIER;
        }
        return NORMAL_MULTIPLIER;
    }

    /**
     * Returns true when the given parameters trigger surge pricing.
     */
    public boolean isSurge(int pendingRequests, int availableDrivers) {
        return getSurgeMultiplier(pendingRequests, availableDrivers) > NORMAL_MULTIPLIER;
    }

    /**
     * Prints a human-readable surge status to the console.
     */
    public void printSurgeStatus(int pendingRequests, int availableDrivers) {
        boolean surge = isSurge(pendingRequests, availableDrivers);
        System.out.println("\n--- Surge Pricing Status ---");
        System.out.printf("  Pending requests : %d%n", pendingRequests);
        System.out.printf("  Available drivers: %d%n", availableDrivers);
        if (surge) {
            System.out.printf("  SURGE ACTIVE     : %.1fx multiplier applied%n", SURGE_MULTIPLIER);
        } else {
            System.out.println("  No surge currently active (1.0x).");
        }
        System.out.println("----------------------------");
    }
}
