package com.ridebooking.module2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RideMatchingService {

    private static final double MAX_DISTANCE_KM = 5.0;
    private static final int MAX_DRIVERS = 3;

    public List<DriverInfo> findNearestDrivers(
            RideRequest request,
            List<? extends DriverInfo> drivers) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Ride request cannot be null.");
        }

        if (drivers == null) {
            throw new IllegalArgumentException(
                    "Driver list cannot be null.");
        }

        List<DriverInfo> eligibleDrivers = new ArrayList<>();

        for (DriverInfo driver : drivers) {

            if (driver == null) {
                continue;
            }

            if (!driver.isAvailable()) {
                continue;
            }

            if (driver.getVehicleType() == null
                    || !driver.getVehicleType().equalsIgnoreCase(
                            request.getVehicleType())) {
                continue;
            }

            double driverX = driver.getCurrentX();
            double driverY = driver.getCurrentY();

            if (!Double.isFinite(driverX)
                    || !Double.isFinite(driverY)) {
                continue;
            }

            double distance = Math.hypot(
                    request.getPickupX() - driverX,
                    request.getPickupY() - driverY);

            if (distance <= MAX_DISTANCE_KM) {
                eligibleDrivers.add(driver);
            }
        }

        eligibleDrivers.sort(
                Comparator.comparingDouble(
                        driver -> Math.hypot(
                                request.getPickupX()
                                        - driver.getCurrentX(),
                                request.getPickupY()
                                        - driver.getCurrentY())));

        if (eligibleDrivers.size() > MAX_DRIVERS) {
            return new ArrayList<>(
                    eligibleDrivers.subList(0, MAX_DRIVERS));
        }

        return eligibleDrivers;
    }
}
