package main.java.com.ridebooking.module2;

//package com.ridebooking.module2;

import java.util.Arrays;
import java.util.List;

public class RideMatchingTest {

    static class TestDriver implements DriverInfo {

        private final String driverId;
        private final double x;
        private final double y;
        private final String vehicleType;
        private boolean available;

        public TestDriver(String driverId, double x, double y,
                          String vehicleType, boolean available) {
            this.driverId = driverId;
            this.x = x;
            this.y = y;
            this.vehicleType = vehicleType;
            this.available = available;
        }

        public String getDriverId() {
            return driverId;
        }

        public double getCurrentX() {
            return x;
        }

        public double getCurrentY() {
            return y;
        }

        public String getVehicleType() {
            return vehicleType;
        }

        public boolean isAvailable() {
            return available;
        }
        @Override
        public void markBusy() {
            this.available = false;
        }
    }

    public static void main(String[] args) {

        RideRequest request = new RideRequest(
                "R001",
                "U001",
                0, 0,
                10, 10,
                "Bike"
        );

        List<DriverInfo> drivers = Arrays.asList(
                new TestDriver("D101", 3, 0, "Bike", true),
                new TestDriver("D102", 1, 1, "Bike", true),
                new TestDriver("D103", 4.9, 0, "Bike", true),
                new TestDriver("D104", 5.1, 0, "Bike", true),
                new TestDriver("D105", 0.5, 0, "Auto", true),
                new TestDriver("D106", 0.2, 0, "Bike", false)
        );

        RideMatchingService service = new RideMatchingService();

        List<DriverInfo> matchedDrivers =
                service.findNearestDrivers(request, drivers);

        System.out.println("Eligible drivers in matching order:");

        for (DriverInfo driver : matchedDrivers) {
            double distance = Math.hypot(
                    request.getPickupX() - driver.getCurrentX(),
                    request.getPickupY() - driver.getCurrentY()
            );

            System.out.println(
                    driver.getDriverId()
                    + " - Distance: "
                    + distance
                    + " km"
            );
        }
    }
}
