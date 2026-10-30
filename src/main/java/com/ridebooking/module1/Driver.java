
package com.ridebooking.module1;

import java.util.ArrayList;
import java.util.List;

public class Driver {

    private String name;
    private String phoneNumber;
    private String licenceNumber;

    private DocumentStatus documentStatus;

    private List<Vehicle> vehicles;
    private Vehicle activeVehicle;

    private boolean available;
    private boolean suspended;

    private int completedRides;
    private int ratingCount;
    private double totalRating;
    private double averageRating;

    public Driver(String name, String phoneNumber,
                  String licenceNumber, List<Vehicle> vehicles) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver name is required");
        }

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        if (licenceNumber == null || licenceNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Licence number is required");
        }

        if (vehicles == null || vehicles.isEmpty()) {
            throw new IllegalArgumentException(
                "Driver must have at least one vehicle"
            );
        }

        this.name = name.trim();
        this.phoneNumber = phoneNumber.trim();
        this.licenceNumber = licenceNumber.trim();

        this.documentStatus = DocumentStatus.PENDING;

        this.vehicles = new ArrayList<>(vehicles);
        this.activeVehicle = this.vehicles.get(0);

        this.available = false;
        this.suspended = false;

        this.completedRides = 0;
        this.ratingCount = 0;
        this.totalRating = 0;
        this.averageRating = 0;
    }

    // Getters

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLicenceNumber() {
        return licenceNumber;
    }

    public DocumentStatus getDocumentStatus() {
        return documentStatus;
    }

    public List<Vehicle> getVehicles() {
        return new ArrayList<>(vehicles);
    }

    public Vehicle getActiveVehicle() {
        return activeVehicle;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public int getCompletedRides() {
        return completedRides;
    }

    public double getAverageRating() {
        return averageRating;
    }

    // Document verification

    void setDocumentStatus(DocumentStatus status) {
        this.documentStatus = status;

        if (status != DocumentStatus.APPROVED) {
            this.available = false;
        }
    }

    // Vehicle management

    void setActiveVehicle(Vehicle vehicle) {

        if (!vehicles.contains(vehicle)) {
            throw new IllegalArgumentException(
                "Vehicle does not belong to this driver"
            );
        }

        this.activeVehicle = vehicle;
    }

    // Driver availability

    void makeAvailable() {

        if (documentStatus != DocumentStatus.APPROVED) {
            throw new IllegalStateException(
                "Driver documents are not approved"
            );
        }

        if (suspended) {
            throw new IllegalStateException(
                "Suspended driver cannot become available"
            );
        }

        if (activeVehicle == null) {
            throw new IllegalStateException(
                "Driver must have an active vehicle"
            );
        }

        available = true;
    }

    void makeUnavailable() {
        available = false;
    }

    // Record completed ride

    void recordCompletedRide() {

        completedRides++;

        checkSuspension();
    }

    // Add driver rating

    void addRating(int rating) {

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                "Rating must be between 1 and 5"
            );
        }

        totalRating += rating;
        ratingCount++;

        averageRating = totalRating / ratingCount;

        checkSuspension();
    }

    // Automatic suspension

    private void checkSuspension() {

        if (completedRides >= 20 && ratingCount > 0
                && averageRating < 3.0) {

            suspended = true;
            available = false;

            System.out.println("Driver automatically suspended!");
        }
    }

    // Display driver information

    public void displayDriver() {

        System.out.println("Driver Name: " + name);
        System.out.println("Phone Number: " + phoneNumber);
        System.out.println("Licence Number: " + licenceNumber);
        System.out.println("Document Status: " + documentStatus);
        System.out.println("Active Vehicle: "
                + activeVehicle.getRegistrationNumber());
        System.out.println("Available: " + available);
        System.out.println("Suspended: " + suspended);
        System.out.println("Completed Rides: " + completedRides);
        System.out.println("Average Rating: " + averageRating);
    }
}
