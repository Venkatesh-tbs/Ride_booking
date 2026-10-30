
package com.ridebooking.module3;

import java.util.List;
import java.util.Scanner;

public class TripCLI {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        TripService service = new TripService();

        int choice = -1;
        do {
            System.out.println("\n===== TRIP LIFECYCLE =====");
            System.out.println("1. Create Trip");
            System.out.println("2. Accept Trip");
            System.out.println("3. Driver Arrived");
            System.out.println("4. Start Trip");
            System.out.println("5. Complete Trip");
            System.out.println("6. Cancel by Rider");
            System.out.println("7. Cancel by Driver");
            System.out.println("8. Display Trips");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");

            if (!sc.hasNextInt()) {
                System.out.println("Please enter a valid number.");
                sc.next();
                continue;
            }

            choice = sc.nextInt();

            switch (choice) {

                case 1:
                    createTrip(sc, service);
                    break;

                case 2:
                    performAction(
                            sc,
                            service,
                            Trip.Status.REQUESTED,
                            "Enter Trip ID to accept: ",
                            2);
                    break;

                case 3:
                    performAction(
                            sc,
                            service,
                            Trip.Status.ACCEPTED,
                            "Enter Trip ID: ",
                            3);
                    break;

                case 4:
                    startTrip(sc, service);
                    break;

                case 5:
                    performAction(
                            sc,
                            service,
                            Trip.Status.IN_PROGRESS,
                            "Enter Trip ID to complete: ",
                            5);
                    break;

                case 6:
                    cancelTrip(
                            sc,
                            service,
                            Trip.Status.REQUESTED,
                            "Cancel by rider");
                    break;

                case 7:
                    cancelDriverTrip(sc, service);
                    break;

                case 8:
                    service.displayTrips();
                    break;

                case 0:
                    System.out.println("Exiting Trip Management.");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }

        } while (choice != 0);

        sc.close();
    }

    // Create a trip
    private static void createTrip(
            Scanner sc, TripService service) {

        System.out.print("Enter Trip ID: ");
        int tripId = sc.nextInt();

        System.out.print("Enter Rider ID: ");
        int riderId = sc.nextInt();

        System.out.print("Enter Driver ID: ");
        int driverId = sc.nextInt();

        System.out.print("Set 4-digit OTP: ");
        String otp = sc.next();

        if (!otp.matches("\\d{4}")) {
            System.out.println(
                    "OTP must contain exactly four digits.");
            return;
        }

        Trip trip = new Trip(tripId, riderId, driverId, otp);
        service.addTrip(trip);
    }

    // Display trips matching the requested status
    private static boolean showTripsByStatus(
            TripService service, Trip.Status status) {

        List<Trip> trips = service.getTripsByStatus(status);

        if (trips.isEmpty()) {
            System.out.println(
                    "No trips available for this action.");
            return false;
        }

        System.out.println("\n===== AVAILABLE TRIPS =====");

        for (Trip trip : trips) {
            System.out.println(
                    "Trip ID: " + trip.getTripId()
                            + " | Rider ID: " + trip.getRiderId()
                            + " | Driver ID: " + trip.getDriverId()
                            + " | Status: " + trip.getStatus());
        }

        return true;
    }

    // Perform accept, driver-arrived, or complete action
    private static void performAction(
            Scanner sc,
            TripService service,
            Trip.Status status,
            String prompt,
            int action) {

        if (!showTripsByStatus(service, status)) {
            return;
        }

        System.out.print(prompt);
        int tripId = sc.nextInt();

        switch (action) {

            case 2:
                service.acceptTrip(tripId);
                break;

            case 3:
                service.driverArrived(tripId);
                break;

            case 5:
                service.completeTrip(tripId);
                break;

            default:
                System.out.println("Invalid action.");
        }
    }

    // Start a trip after OTP validation
    private static void startTrip(
            Scanner sc, TripService service) {

        if (!showTripsByStatus(
                service, Trip.Status.DRIVER_ARRIVED)) {
            return;
        }

        System.out.print("Enter Trip ID: ");
        int tripId = sc.nextInt();

        System.out.print("Enter OTP: ");
        String otp = sc.next();

        service.startTrip(tripId, otp);
    }

    // Cancel a trip by the rider
    private static void cancelTrip(
            Scanner sc,
            TripService service,
            Trip.Status status,
            String message) {

        System.out.println("\n" + message);

        List<Trip> trips = service.getAllTrips();
        boolean found = false;

        for (Trip trip : trips) {
            if (trip.getStatus() == Trip.Status.REQUESTED ||
                    trip.getStatus() == Trip.Status.ACCEPTED ||
                    trip.getStatus() == Trip.Status.DRIVER_ARRIVED) {

                System.out.println(
                        "Trip ID: " + trip.getTripId()
                                + " | Status: " + trip.getStatus());

                found = true;
            }
        }

        if (!found) {
            System.out.println("No trips available for cancellation.");
            return;
        }

        System.out.print("Enter Trip ID to cancel: ");
        service.cancelByRider(sc.nextInt());
    }

    // Cancel a trip by the driver
    private static void cancelDriverTrip(
            Scanner sc, TripService service) {

        List<Trip> trips = service.getAllTrips();
        boolean found = false;

        System.out.println("\n===== DRIVER CANCELLATION =====");

        for (Trip trip : trips) {
            if (trip.getStatus() == Trip.Status.REQUESTED ||
                    trip.getStatus() == Trip.Status.ACCEPTED) {

                System.out.println(
                        "Trip ID: " + trip.getTripId()
                                + " | Status: " + trip.getStatus());

                found = true;
            }
        }

        if (!found) {
            System.out.println("No trips available for cancellation.");
            return;
        }

        System.out.print("Enter Trip ID to cancel: ");
        service.cancelByDriver(sc.nextInt());
    }
}
