package com.ridebooking.module4;

/**
 * Holds the detailed breakdown of a calculated fare.
 * Immutable value object produced by FareCalculator.
 */
public class FareDetails {

    private final String vehicleType;
    private final double distanceKm;
    private final int durationMinutes;
    private final double baseFare;
    private final double distanceFare;
    private final double durationFare;
    private final double surgeMultiplier;
    private final double finalFare;
    private final boolean surgeApplied;

    public FareDetails(String vehicleType,
                       double distanceKm,
                       int durationMinutes,
                       double baseFare,
                       double distanceFare,
                       double durationFare,
                       double surgeMultiplier,
                       double finalFare,
                       boolean surgeApplied) {
        this.vehicleType = vehicleType;
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        this.baseFare = baseFare;
        this.distanceFare = distanceFare;
        this.durationFare = durationFare;
        this.surgeMultiplier = surgeMultiplier;
        this.finalFare = finalFare;
        this.surgeApplied = surgeApplied;
    }

    // ---------- Getters ----------

    public String getVehicleType()     { return vehicleType; }
    public double getDistanceKm()      { return distanceKm; }
    public int    getDurationMinutes() { return durationMinutes; }
    public double getBaseFare()        { return baseFare; }
    public double getDistanceFare()    { return distanceFare; }
    public double getDurationFare()    { return durationFare; }
    public double getSurgeMultiplier() { return surgeMultiplier; }
    public double getFinalFare()       { return finalFare; }
    public boolean isSurgeApplied()   { return surgeApplied; }

    /**
     * Prints a formatted fare breakdown to the console.
     */
    public void printBreakdown() {
        System.out.println("\n========== FARE BREAKDOWN ==========");
        System.out.printf("  Vehicle Type    : %s%n", vehicleType);
        System.out.printf("  Distance        : %.2f km%n", distanceKm);
        System.out.printf("  Duration        : %d min%n", durationMinutes);
        System.out.printf("  Base Fare       : Rs. %.2f%n", baseFare);
        System.out.printf("  Distance Fare   : Rs. %.2f%n", distanceFare);
        System.out.printf("  Duration Fare   : Rs. %.2f%n", durationFare);
        if (surgeApplied) {
            System.out.printf("  Surge Multiplier: %.1fx (SURGE ACTIVE)%n", surgeMultiplier);
        } else {
            System.out.printf("  Surge Multiplier: %.1fx (no surge)%n", surgeMultiplier);
        }
        System.out.printf("  FINAL FARE      : Rs. %.2f%n", finalFare);
        System.out.println("====================================");
    }
}
