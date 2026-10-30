
package com.ridebooking.module1;

public class Vehicle {

    private String registrationNumber;
    private VehicleType type;
    private int seatingCapacity;

    public Vehicle(String registrationNumber,
                   VehicleType type,
                   int seatingCapacity) {

        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number is required");
        }

        if (type == null) {
            throw new IllegalArgumentException("Vehicle type is required");
        }

        if (seatingCapacity <= 0) {
            throw new IllegalArgumentException("Seating capacity must be positive");
        }

        this.registrationNumber = registrationNumber.trim();
        this.type = type;
        this.seatingCapacity = seatingCapacity;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public VehicleType getType() {
        return type;
    }

    public int getSeatingCapacity() {
        return seatingCapacity;
    }

    public void displayVehicle() {
        System.out.println("Registration Number: " + registrationNumber);
        System.out.println("Vehicle Type: " + type);
        System.out.println("Seating Capacity: " + seatingCapacity);
    }
}
