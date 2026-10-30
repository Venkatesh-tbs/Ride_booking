package com.ridebooking.module4;

/**
 * Calculates fares for rides based on vehicle type, distance, duration,
 * and an optional surge multiplier.
 *
 * Fare formula:
 *   raw = BASE_FARE + (ratePerKm * distanceKm) + (RATE_PER_MIN * durationMinutes)
 *   surged = raw * surgeMultiplier
 *   final  = max(surged, MINIMUM_FARE)
 */
public class FareCalculator {

    // Fare constants
    public static final double BASE_FARE    = 30.0;
    public static final double RATE_PER_MIN = 1.0;
    public static final double MINIMUM_FARE = 50.0;

    /**
     * Calculates the fare given trip details and a surge multiplier.
     *
     * @param vehicleType     the type of vehicle (BIKE / AUTO / MINI / SEDAN / SUV)
     * @param distanceKm      trip distance in kilometres (must be > 0)
     * @param durationMinutes trip duration in minutes (must be >= 0)
     * @param surgeMultiplier surge factor (1.0 = no surge, 1.5 = surge)
     * @return a FareDetails object with the full breakdown
     */
    public FareDetails calculate(VehicleType vehicleType,
                                 double distanceKm,
                                 int durationMinutes,
                                 double surgeMultiplier) {

        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type must not be null.");
        }
        if (distanceKm <= 0) {
            throw new IllegalArgumentException("Distance must be greater than 0 km.");
        }
        if (durationMinutes < 0) {
            throw new IllegalArgumentException("Duration cannot be negative.");
        }
        if (surgeMultiplier < 1.0) {
            throw new IllegalArgumentException("Surge multiplier cannot be less than 1.0.");
        }

        double distanceFare = vehicleType.getRatePerKm() * distanceKm;
        double durationFare = RATE_PER_MIN * durationMinutes;
        double rawFare      = BASE_FARE + distanceFare + durationFare;
        double surgedFare   = rawFare * surgeMultiplier;
        double finalFare    = Math.max(surgedFare, MINIMUM_FARE);

        // Round to 2 decimal places
        finalFare = Math.round(finalFare * 100.0) / 100.0;

        boolean surgeApplied = (surgeMultiplier > 1.0);

        return new FareDetails(
                vehicleType.name(),
                distanceKm,
                durationMinutes,
                BASE_FARE,
                distanceFare,
                durationFare,
                surgeMultiplier,
                finalFare,
                surgeApplied
        );
    }
}
