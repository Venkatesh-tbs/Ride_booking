package com.ridebooking;

import com.ridebooking.module1.DocumentStatus;
import com.ridebooking.module1.Driver;
import com.ridebooking.module1.DriverService;
import com.ridebooking.module1.Rider;
import com.ridebooking.module1.Vehicle;
import com.ridebooking.module1.VehicleType;
import com.ridebooking.module2.DriverInfo;
import com.ridebooking.module2.RideMatchingService;
import com.ridebooking.module2.RideOfferService;
import com.ridebooking.module2.RideRequest;
import com.ridebooking.module3.Trip;
import com.ridebooking.module3.TripService;
import com.ridebooking.module4.PaymentModule;
import com.ridebooking.module5.RatingsModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main entry point for the Ride Booking System CLI.
 *
 * Integrates all five modules:
 *   Module 1 – Rider and Driver Registration
 *   Module 2 – Ride Request and Matching
 *   Module 3 – Trip Lifecycle
 *   Module 4 – Fare and Payments
 *   Module 5 – Ratings
 *
 * All modules share the same in-memory data and the same Scanner instance.
 */
public class Main {

    // -----------------------------------------------------------------------
    //  Shared data stores (passed between modules)
    // -----------------------------------------------------------------------
    private final DriverService       driverService;
    private final RideMatchingService matchingService;
    private final RideOfferService    offerService;
    private final TripService         tripService;
    private final Scanner             scanner;

    // -----------------------------------------------------------------------
    //  Constructor
    // -----------------------------------------------------------------------
    public Main() {
        this.driverService   = new DriverService();
        this.matchingService = new RideMatchingService();
        this.offerService    = new RideOfferService(matchingService);
        this.tripService     = new TripService();
        this.scanner         = new Scanner(System.in);
    }

    // -----------------------------------------------------------------------
    //  Application entry point
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        new Main().run();
    }

    // -----------------------------------------------------------------------
    //  Main menu loop
    // -----------------------------------------------------------------------
    private void run() {
        printBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter your choice: ");
            System.out.println();
            switch (choice) {
                case 1:
                    runModule1();
                    break;
                case 2:
                    runModule2();
                    break;
                case 3:
                    runModule3();
                    break;
                case 4:
                    runModule4();
                    break;
                case 5:
                    runModule5();
                    break;
                case 0:
                    running = false;
                    System.out.println("Thank you for using Ride Booking System. Goodbye!");
                    break;
                default:
                    System.out.println("[ERROR] Invalid choice. Please enter 0–5.");
            }
        }
        scanner.close();
    }

    // -----------------------------------------------------------------------
    //  Module 1 – Rider and Driver Registration CLI
    // -----------------------------------------------------------------------
    private void runModule1() {
        boolean back = false;
        while (!back) {
            System.out.println("\n===== MODULE 1 – REGISTRATION =====");
            System.out.println("  1. Register Rider");
            System.out.println("  2. Register Driver");
            System.out.println("  3. Approve / Reject Driver Documents");
            System.out.println("  4. Make Driver Available");
            System.out.println("  5. Make Driver Unavailable");
            System.out.println("  6. Display All Riders");
            System.out.println("  7. Display All Drivers");
            System.out.println("  0. Back to Main Menu");
            int choice = readInt("Enter choice: ");
            System.out.println();

            switch (choice) {
                case 1:
                    registerRider();
                    break;
                case 2:
                    registerDriver();
                    break;
                case 3:
                    updateDocumentStatus();
                    break;
                case 4:
                    setDriverAvailability(true);
                    break;
                case 5:
                    setDriverAvailability(false);
                    break;
                case 6:
                    driverService.displayAllRiders();
                    break;
                case 7:
                    driverService.displayAllDrivers();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("[ERROR] Invalid choice.");
            }
        }
    }

    private void registerRider() {
        System.out.println("--- Register Rider ---");
        String name  = readNonEmptyString("  Name        : ");
        String phone = readNonEmptyString("  Phone Number: ");
        String email = readNonEmptyString("  Email       : ");
        try {
            driverService.registerRider(name, phone, email);
        } catch (IllegalArgumentException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void registerDriver() {
        System.out.println("--- Register Driver ---");
        String name    = readNonEmptyString("  Name          : ");
        String phone   = readNonEmptyString("  Phone Number  : ");
        String licence = readNonEmptyString("  Licence Number: ");

        int numVehicles = readPositiveInt("  Number of vehicles to register: ");
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < numVehicles; i++) {
            System.out.println("  -- Vehicle " + (i + 1) + " --");
            String regNum = readNonEmptyString("    Registration Number: ");
            VehicleType type = selectVehicleTypeModule1();
            int seats = readPositiveInt("    Seating Capacity   : ");
            try {
                vehicles.add(new Vehicle(regNum, type, seats));
            } catch (IllegalArgumentException e) {
                System.out.println("[ERROR] " + e.getMessage());
                i--;
            }
        }

        try {
            driverService.registerDriver(name, phone, licence, vehicles);
        } catch (IllegalArgumentException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void updateDocumentStatus() {
        System.out.println("--- Update Driver Document Status ---");
        List<Driver> drivers = driverService.getAllDrivers();
        if (drivers.isEmpty()) {
            System.out.println("No drivers registered.");
            return;
        }
        System.out.println("Registered drivers:");
        for (int i = 0; i < drivers.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + drivers.get(i).getName()
                    + " [" + drivers.get(i).getDocumentStatus() + "]");
        }
        int idx = readInt("Select driver number: ") - 1;
        if (idx < 0 || idx >= drivers.size()) {
            System.out.println("[ERROR] Invalid selection.");
            return;
        }
        Driver driver = drivers.get(idx);
        System.out.println("Status: 1=APPROVED  2=REJECTED");
        int s = readInt("Select status: ");
        DocumentStatus status = (s == 1) ? DocumentStatus.APPROVED : DocumentStatus.REJECTED;
        try {
            driverService.updateDocumentStatus(driver, status);
        } catch (IllegalArgumentException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void setDriverAvailability(boolean makeAvailable) {
        List<Driver> drivers = driverService.getAllDrivers();
        if (drivers.isEmpty()) {
            System.out.println("No drivers registered.");
            return;
        }
        System.out.println("Registered drivers:");
        for (int i = 0; i < drivers.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + drivers.get(i).getName()
                    + " | Available: " + drivers.get(i).isAvailable());
        }
        int idx = readInt("Select driver number: ") - 1;
        if (idx < 0 || idx >= drivers.size()) {
            System.out.println("[ERROR] Invalid selection.");
            return;
        }
        Driver driver = drivers.get(idx);
        try {
            if (makeAvailable) {
                driverService.makeDriverAvailable(driver);
            } else {
                driverService.makeDriverUnavailable(driver);
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private VehicleType selectVehicleTypeModule1() {
        System.out.println("    Vehicle types: 1=BIKE  2=AUTO  3=MINI  4=SEDAN  5=SUV");
        while (true) {
            int c = readInt("    Select type: ");
            switch (c) {
                case 1: return VehicleType.BIKE;
                case 2: return VehicleType.AUTO;
                case 3: return VehicleType.MINI;
                case 4: return VehicleType.SEDAN;
                case 5: return VehicleType.SUV;
                default: System.out.println("    [ERROR] Invalid. Try again.");
            }
        }
    }

    // -----------------------------------------------------------------------
    //  Module 2 – Ride Request and Matching CLI
    // -----------------------------------------------------------------------
    private void runModule2() {
        boolean back = false;
        while (!back) {
            System.out.println("\n===== MODULE 2 – RIDE MATCHING =====");
            System.out.println("  1. Create Ride Request");
            System.out.println("  2. View Current Offer for Request");
            System.out.println("  3. Accept Current Offer");
            System.out.println("  4. Reject Current Offer");
            System.out.println("  0. Back to Main Menu");
            int choice = readInt("Enter choice: ");
            System.out.println();

            switch (choice) {
                case 1:
                    createRideRequest();
                    break;
                case 2:
                    viewCurrentOffer();
                    break;
                case 3:
                    respondToOffer(true);
                    break;
                case 4:
                    respondToOffer(false);
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("[ERROR] Invalid choice.");
            }
        }
    }

    private void createRideRequest() {
        System.out.println("--- Create Ride Request ---");
        String reqId   = readNonEmptyString("  Request ID     : ");
        String riderId = readNonEmptyString("  Rider ID       : ");
        System.out.print("  Pickup X       : ");
        double px = readDouble();
        System.out.print("  Pickup Y       : ");
        double py = readDouble();
        System.out.print("  Drop X         : ");
        double dx = readDouble();
        System.out.print("  Drop Y         : ");
        double dy = readDouble();
        String vType = readNonEmptyString("  Vehicle Type (e.g. BIKE, AUTO, SEDAN): ");

        try {
            RideRequest request = new RideRequest(reqId, riderId, px, py, dx, dy, vType);

            // Gather available drivers from Module 1
            List<Driver> allDrivers = driverService.getAllDrivers();
            List<DriverInfo> driverInfos = new ArrayList<>();
            for (Driver d : allDrivers) {
                driverInfos.add(new DriverAdapter(d, driverService));
            }

            DriverInfo offered = offerService.startRequest(request, driverInfos);
            if (offered != null) {
                System.out.println("[MATCH] Request started. First offer sent to driver: "
                        + offered.getDriverId());
            } else {
                System.out.println("[INFO] No suitable drivers found within 5 km for the requested vehicle type.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void viewCurrentOffer() {
        String reqId = readNonEmptyString("Enter Request ID: ");
        DriverInfo driver = offerService.getCurrentOffer(reqId);
        if (driver == null) {
            System.out.println("[INFO] No active offer for request " + reqId
                    + " (expired, accepted, or no drivers available).");
        } else {
            System.out.println("[OFFER] Current offer for request " + reqId
                    + ": Driver ID = " + driver.getDriverId());
        }
    }

    private void respondToOffer(boolean accept) {
        String reqId    = readNonEmptyString("Enter Request ID : ");
        String driverId = readNonEmptyString("Enter Driver ID  : ");
        if (accept) {
            boolean ok = offerService.acceptOffer(reqId, driverId);
            System.out.println(ok ? "[SUCCESS] Offer accepted. Driver marked busy."
                                  : "[ERROR] Could not accept offer (expired/invalid).");
        } else {
            DriverInfo next = offerService.rejectOffer(reqId, driverId);
            if (next != null) {
                System.out.println("[INFO] Offer rejected. Next driver offered: " + next.getDriverId());
            } else {
                System.out.println("[INFO] Offer rejected. No more drivers available.");
            }
        }
    }

    // -----------------------------------------------------------------------
    //  Module 3 – Trip Lifecycle CLI (delegates to TripCLI-equivalent logic)
    // -----------------------------------------------------------------------
    private void runModule3() {
        boolean back = false;
        while (!back) {
            System.out.println("\n===== MODULE 3 – TRIP LIFECYCLE =====");
            System.out.println("  1. Create Trip");
            System.out.println("  2. Accept Trip");
            System.out.println("  3. Driver Arrived");
            System.out.println("  4. Start Trip (OTP)");
            System.out.println("  5. Complete Trip");
            System.out.println("  6. Cancel by Rider");
            System.out.println("  7. Cancel by Driver");
            System.out.println("  8. Display All Trips");
            System.out.println("  0. Back to Main Menu");
            int choice = readInt("Enter choice: ");
            System.out.println();

            switch (choice) {
                case 1:
                    createTrip();
                    break;
                case 2:
                    performTripAction(Trip.Status.REQUESTED, "Accept", 2);
                    break;
                case 3:
                    performTripAction(Trip.Status.ACCEPTED, "Driver Arrived", 3);
                    break;
                case 4:
                    startTrip();
                    break;
                case 5:
                    performTripAction(Trip.Status.IN_PROGRESS, "Complete", 5);
                    break;
                case 6:
                    cancelByRider();
                    break;
                case 7:
                    cancelByDriver();
                    break;
                case 8:
                    tripService.displayTrips();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("[ERROR] Invalid choice.");
            }
        }
    }

    private void createTrip() {
        System.out.print("  Trip ID   : ");
        int tripId   = readInt("");
        System.out.print("  Rider ID  : ");
        int riderId  = readInt("");
        System.out.print("  Driver ID : ");
        int driverId = readInt("");
        String otp   = readNonEmptyString("  4-digit OTP: ");
        if (!otp.matches("\\d{4}")) {
            System.out.println("[ERROR] OTP must be exactly 4 digits.");
            return;
        }
        Trip trip = new Trip(tripId, riderId, driverId, otp);
        tripService.addTrip(trip);
    }

    private void performTripAction(Trip.Status requiredStatus, String label, int action) {
        List<Trip> trips = tripService.getTripsByStatus(requiredStatus);
        if (trips.isEmpty()) {
            System.out.println("[INFO] No trips with status " + requiredStatus + " found.");
            return;
        }
        System.out.println("Trips available for [" + label + "]:");
        for (Trip t : trips) {
            System.out.println("  Trip ID: " + t.getTripId() + " | Status: " + t.getStatus());
        }
        System.out.print("  Enter Trip ID: ");
        int tripId = readInt("");
        switch (action) {
            case 2: tripService.acceptTrip(tripId); break;
            case 3: tripService.driverArrived(tripId); break;
            case 5: tripService.completeTrip(tripId); break;
        }
    }

    private void startTrip() {
        List<Trip> trips = tripService.getTripsByStatus(Trip.Status.DRIVER_ARRIVED);
        if (trips.isEmpty()) {
            System.out.println("[INFO] No trips with status DRIVER_ARRIVED found.");
            return;
        }
        for (Trip t : trips) {
            System.out.println("  Trip ID: " + t.getTripId() + " | Status: " + t.getStatus());
        }
        System.out.print("  Enter Trip ID: ");
        int tripId = readInt("");
        String otp = readNonEmptyString("  Enter OTP    : ");
        tripService.startTrip(tripId, otp);
    }

    private void cancelByRider() {
        List<Trip> trips = tripService.getAllTrips();
        boolean found = false;
        for (Trip t : trips) {
            if (t.getStatus() == Trip.Status.REQUESTED
                    || t.getStatus() == Trip.Status.ACCEPTED
                    || t.getStatus() == Trip.Status.DRIVER_ARRIVED) {
                System.out.println("  Trip ID: " + t.getTripId() + " | Status: " + t.getStatus());
                found = true;
            }
        }
        if (!found) {
            System.out.println("[INFO] No cancellable trips found.");
            return;
        }
        System.out.print("  Enter Trip ID to cancel: ");
        tripService.cancelByRider(readInt(""));
    }

    private void cancelByDriver() {
        List<Trip> trips = tripService.getAllTrips();
        boolean found = false;
        for (Trip t : trips) {
            if (t.getStatus() == Trip.Status.REQUESTED
                    || t.getStatus() == Trip.Status.ACCEPTED) {
                System.out.println("  Trip ID: " + t.getTripId() + " | Status: " + t.getStatus());
                found = true;
            }
        }
        if (!found) {
            System.out.println("[INFO] No cancellable trips found for driver.");
            return;
        }
        System.out.print("  Enter Trip ID to cancel: ");
        tripService.cancelByDriver(readInt(""));
    }

    // -----------------------------------------------------------------------
    //  Module 4 – Fare and Payments (delegates to PaymentModule)
    // -----------------------------------------------------------------------
    private void runModule4() {
        // PaymentModule manages its own Scanner internally
        // We pass control to it then return
        PaymentModule module4 = new PaymentModule(scanner);
        module4.run();
    }

    // -----------------------------------------------------------------------
    //  Module 5 – Ratings (delegates to RatingsModule)
    // -----------------------------------------------------------------------
    private void runModule5() {
        RatingsModule module5 = new RatingsModule(scanner);
        module5.launch();
    }

    // -----------------------------------------------------------------------
    //  DriverAdapter – bridges Module 1 Driver to Module 2 DriverInfo interface
    // -----------------------------------------------------------------------
    private static class DriverAdapter implements DriverInfo {

        private final Driver driver;
        private final DriverService driverService;

        DriverAdapter(Driver driver, DriverService driverService) {
            this.driver = driver;
            this.driverService = driverService;
        }

        @Override
        public String getDriverId() {
            return driver.getPhoneNumber(); // phone number used as unique driver ID
        }

        @Override
        public double getCurrentX() {
            return 0.0; // Module 1 does not track GPS; default to origin
        }

        @Override
        public double getCurrentY() {
            return 0.0;
        }

        @Override
        public String getVehicleType() {
            if (driver.getActiveVehicle() == null) return "";
            return driver.getActiveVehicle().getType().name();
        }

        @Override
        public boolean isAvailable() {
            return driver.isAvailable();
        }

        @Override
        public void markBusy() {
            driverService.makeDriverUnavailable(driver);
        }
    }

    // -----------------------------------------------------------------------
    //  UI helpers
    // -----------------------------------------------------------------------

    private void printBanner() {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║       RIDE BOOKING SYSTEM  –  CLI Application     ║");
        System.out.println("║           All Five Modules Integrated              ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
    }

    private void printMainMenu() {
        System.out.println("\n============================================");
        System.out.println("           MAIN MENU");
        System.out.println("============================================");
        System.out.println("  1. Module 1 – Rider & Driver Registration");
        System.out.println("  2. Module 2 – Ride Request & Matching");
        System.out.println("  3. Module 3 – Trip Lifecycle");
        System.out.println("  4. Module 4 – Fare & Payments");
        System.out.println("  5. Module 5 – Ratings");
        System.out.println("  0. Exit");
        System.out.println("============================================");
    }

    private int readInt(String prompt) {
        while (true) {
            if (!prompt.isEmpty()) System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("[ERROR] Please enter a valid integer: ");
            }
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            int v = readInt(prompt);
            if (v > 0) return v;
            System.out.println("[ERROR] Value must be greater than 0.");
        }
    }

    private double readDouble() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("[ERROR] Please enter a valid number: ");
            }
        }
    }

    private String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String v = scanner.nextLine().trim();
            if (!v.isEmpty()) return v;
            System.out.println("[ERROR] Input cannot be empty.");
        }
    }
}
