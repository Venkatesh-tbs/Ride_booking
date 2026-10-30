
package com.ridebooking.module3;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TripService {

    private final List<Trip> trips = new ArrayList<>();

    // Add a new trip
    public boolean addTrip(Trip trip) {

        if (trip == null) {
            System.out.println("Invalid trip.");
            return false;
        }

        if (findTrip(trip.getTripId()) != null) {
            System.out.println("Trip ID already exists.");
            return false;
        }

        trips.add(trip);
        System.out.println("Trip created successfully.");

        return true;
    }

    // Find a trip using its ID
    private Trip findTrip(int tripId) {

        for (Trip trip : trips) {
            if (trip.getTripId() == tripId) {
                return trip;
            }
        }

        return null;
    }

    // Return all trips
    public List<Trip> getAllTrips() {
        return new ArrayList<>(trips);
    }

    // Return trips with a particular status
    public List<Trip> getTripsByStatus(Trip.Status status) {

        List<Trip> result = new ArrayList<>();

        for (Trip trip : trips) {
            if (trip.getStatus() == status) {
                result.add(trip);
            }
        }

        return result;
    }

    // Accept a requested trip
    public boolean acceptTrip(int tripId) {

        Trip trip = findTrip(tripId);

        if (trip == null) {
            System.out.println("Trip not found.");
            return false;
        }

        if (trip.getStatus() != Trip.Status.REQUESTED) {
            System.out.println("Only requested trips can be accepted.");
            return false;
        }

        trip.setStatus(Trip.Status.ACCEPTED);
        trip.setAcceptedAt(LocalDateTime.now());

        System.out.println("Trip accepted successfully.");
        return true;
    }

    // Record driver arrival
    public boolean driverArrived(int tripId) {

        Trip trip = findTrip(tripId);

        if (trip == null) {
            System.out.println("Trip not found.");
            return false;
        }

        if (trip.getStatus() != Trip.Status.ACCEPTED) {
            System.out.println(
                "Driver arrival is allowed only after acceptance."
            );
            return false;
        }

        trip.setStatus(Trip.Status.DRIVER_ARRIVED);

        System.out.println("Driver arrival recorded successfully.");
        return true;
    }

    // Start the trip after validating the OTP
    public boolean startTrip(int tripId, String enteredOTP) {

        Trip trip = findTrip(tripId);

        if (trip == null) {
            System.out.println("Trip not found.");
            return false;
        }

        if (trip.getStatus() != Trip.Status.DRIVER_ARRIVED) {
            System.out.println(
                "Trip can start only after driver arrival."
            );
            return false;
        }

        if (enteredOTP == null ||
            !enteredOTP.matches("\\d{4}") ||
            !trip.verifyOTP(enteredOTP)) {

            System.out.println("Invalid OTP. Trip cannot start.");
            return false;
        }

        trip.setStatus(Trip.Status.IN_PROGRESS);

        System.out.println("Trip started successfully.");
        return true;
    }

    // Complete an ongoing trip
    public boolean completeTrip(int tripId) {

        Trip trip = findTrip(tripId);

        if (trip == null) {
            System.out.println("Trip not found.");
            return false;
        }

        if (trip.getStatus() != Trip.Status.IN_PROGRESS) {
            System.out.println(
                "Only in-progress trips can be completed."
            );
            return false;
        }

        trip.setStatus(Trip.Status.COMPLETED);

        System.out.println("Trip completed successfully.");
        System.out.println(
            "The driver can now become available for another trip."
        );

        return true;
    }

    // Cancel a trip by the rider
    public boolean cancelByRider(int tripId) {

        Trip trip = findTrip(tripId);

        if (trip == null) {
            System.out.println("Trip not found.");
            return false;
        }

        if (trip.getStatus() != Trip.Status.REQUESTED &&
            trip.getStatus() != Trip.Status.ACCEPTED &&
            trip.getStatus() != Trip.Status.DRIVER_ARRIVED) {

            System.out.println(
                "Rider cannot cancel this trip at its current status."
            );
            return false;
        }

        if (trip.getStatus() == Trip.Status.REQUESTED) {

            trip.setStatus(Trip.Status.CANCELLED);
            System.out.println("Trip cancelled. No cancellation fee.");

            return true;
        }

        LocalDateTime acceptedAt = trip.getAcceptedAt();

        if (acceptedAt != null) {

            long seconds = Duration.between(
                acceptedAt, LocalDateTime.now()
            ).getSeconds();

            if (seconds <= 120) {
                System.out.println(
                    "Trip cancelled. No cancellation fee."
                );
            } else {
                System.out.println(
                    "Trip cancelled. Cancellation fee: Rs. 30"
                );
            }
        }

        trip.setStatus(Trip.Status.CANCELLED);

        return true;
    }

    // Cancel a trip by the driver
    public boolean cancelByDriver(int tripId) {

        Trip trip = findTrip(tripId);

        if (trip == null) {
            System.out.println("Trip not found.");
            return false;
        }

        if (trip.getStatus() != Trip.Status.ACCEPTED &&
            trip.getStatus() != Trip.Status.REQUESTED) {

            System.out.println(
                "Driver cannot cancel after arrival or trip start."
            );
            return false;
        }

        trip.setStatus(Trip.Status.CANCELLED);

        System.out.println("Trip cancelled by driver.");
        return true;
    }

    // Display all trips
    public void displayTrips() {

        if (trips.isEmpty()) {
            System.out.println("No trips available.");
            return;
        }

        System.out.println("\n========== ALL TRIPS ==========");

        for (Trip trip : trips) {
            System.out.println(
                "Trip ID: " + trip.getTripId()
                + " | Rider ID: " + trip.getRiderId()
                + " | Driver ID: " + trip.getDriverId()
                + " | Status: " + trip.getStatus()
            );
        }
    }
}
