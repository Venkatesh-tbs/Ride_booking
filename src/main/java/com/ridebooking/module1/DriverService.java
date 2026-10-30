
package com.ridebooking.module1;

import java.util.ArrayList;
import java.util.List;

public class DriverService {

    private List<Rider> riders;
    private List<Driver> drivers;

    public DriverService() {
        riders = new ArrayList<>();
        drivers = new ArrayList<>();
    }

    // Check duplicate rider phone number

    private boolean riderPhoneExists(String phone) {

        for (Rider rider : riders) {

            if (rider.getPhoneNumber().equalsIgnoreCase(phone)) {
                return true;
            }
        }

        return false;
    }

    // Check duplicate driver phone number

    private boolean driverPhoneExists(String phone) {

        for (Driver driver : drivers) {

            if (driver.getPhoneNumber().equalsIgnoreCase(phone)) {
                return true;
            }
        }

        return false;
    }

    // Check duplicate vehicle registration number

    private boolean vehicleRegistrationExists(String number) {

        for (Driver driver : drivers) {

            for (Vehicle vehicle : driver.getVehicles()) {

                if (vehicle.getRegistrationNumber()
                        .equalsIgnoreCase(number)) {
                    return true;
                }
            }
        }

        return false;
    }

    // Register rider

    public Rider registerRider(String name,
                               String phone,
                               String email) {

        if (riderPhoneExists(phone)) {
            throw new IllegalArgumentException(
                "Rider phone number already exists"
            );
        }

        Rider rider = new Rider(name, phone, email);

        riders.add(rider);

        System.out.println("Rider registered successfully!");

        return rider;
    }

    // Register driver

    public Driver registerDriver(String name,
                                 String phone,
                                 String licence,
                                 List<Vehicle> vehicles) {

        if (driverPhoneExists(phone)) {
            throw new IllegalArgumentException(
                "Driver phone number already exists"
            );
        }

        if (vehicles == null || vehicles.isEmpty()) {
            throw new IllegalArgumentException(
                "At least one vehicle is required"
            );
        }

        // Check duplicate vehicle numbers within the new registration
        for (int i = 0; i < vehicles.size(); i++) {

            if (vehicles.get(i) == null) {
                throw new IllegalArgumentException(
                    "Vehicle cannot be null"
                );
            }

            for (int j = i + 1; j < vehicles.size(); j++) {

                if (vehicles.get(i).getRegistrationNumber()
                        .equalsIgnoreCase(
                            vehicles.get(j).getRegistrationNumber()
                        )) {

                    throw new IllegalArgumentException(
                        "Duplicate vehicle registration number"
                    );
                }
            }
        }

        // Check against previously registered vehicles

        for (Vehicle vehicle : vehicles) {

            if (vehicleRegistrationExists(
                    vehicle.getRegistrationNumber())) {

                throw new IllegalArgumentException(
                    "Vehicle registration number already exists"
                );
            }
        }

        Driver driver = new Driver(
            name, phone, licence, vehicles
        );

        drivers.add(driver);

        System.out.println("Driver registered successfully!");
        System.out.println("Document status: PENDING");

        return driver;
    }

    // Approve or reject driver documents

    public void updateDocumentStatus(
            Driver driver, DocumentStatus status) {

        if (!drivers.contains(driver)) {
            throw new IllegalArgumentException(
                "Driver is not registered in the system"
            );
        }

        if (status == null) {
            throw new IllegalArgumentException(
                "Document status is required"
            );
        }

        driver.setDocumentStatus(status);

        System.out.println(
            "Driver document status updated to: " + status
        );
    }

    // Change active vehicle

    public void changeActiveVehicle(
            Driver driver, Vehicle vehicle) {

        if (!drivers.contains(driver)) {
            throw new IllegalArgumentException(
                "Driver is not registered"
            );
        }

        driver.setActiveVehicle(vehicle);

        System.out.println("Active vehicle changed successfully!");
    }

    // Make driver available

    public void makeDriverAvailable(Driver driver) {

        if (!drivers.contains(driver)) {
            throw new IllegalArgumentException(
                "Driver is not registered"
            );
        }

        driver.makeAvailable();

        System.out.println("Driver is now AVAILABLE");
    }

    // Make driver unavailable

    public void makeDriverUnavailable(Driver driver) {

        if (!drivers.contains(driver)) {
            throw new IllegalArgumentException(
                "Driver is not registered"
            );
        }

        driver.makeUnavailable();

        System.out.println("Driver is now UNAVAILABLE");
    }

    // Record completed ride

    public void recordCompletedRide(Driver driver) {

        if (!drivers.contains(driver)) {
            throw new IllegalArgumentException(
                "Driver is not registered"
            );
        }

        driver.recordCompletedRide();
    }

    // Add driver rating

    public void addDriverRating(Driver driver, int rating) {

        if (!drivers.contains(driver)) {
            throw new IllegalArgumentException(
                "Driver is not registered"
            );
        }

        driver.addRating(rating);
    }

    // Display all registered riders

    public void displayAllRiders() {

        for (Rider rider : riders) {
            rider.displayRider();
            System.out.println("------------------");
        }
    }

    // Display all registered drivers

    public void displayAllDrivers() {

        for (Driver driver : drivers) {
            driver.displayDriver();
            System.out.println("------------------");
        }
    }
}
