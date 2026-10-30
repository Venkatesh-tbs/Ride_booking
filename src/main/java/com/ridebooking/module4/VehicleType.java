package com.ridebooking.module4;

/**
 * Enum representing supported vehicle types and their per-km rates.
 */
public enum VehicleType {
    BIKE(6.0),
    AUTO(10.0),
    MINI(12.0),
    SEDAN(15.0),
    SUV(20.0);

    private final double ratePerKm;

    VehicleType(double ratePerKm) {
        this.ratePerKm = ratePerKm;
    }

    public double getRatePerKm() {
        return ratePerKm;
    }
}
