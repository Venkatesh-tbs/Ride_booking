
package com.ridebooking.module1;

import java.util.ArrayList;
import java.util.List;

public class Module1Demo {

    public static void main(String[] args) {

        DriverService service = new DriverService();

        // 1. Register a rider

        System.out.println("===== RIDER REGISTRATION =====");

        Rider rider = service.registerRider(
            "Arun",
            "9876543210",
            "arun@gmail.com"
        );

        rider.displayRider();

        // 2. Create vehicles

        System.out.println("\n===== VEHICLE CREATION =====");

        Vehicle v1 = new Vehicle(
            "TN69AB1234",
            VehicleType.SEDAN,
            4
        );

        Vehicle v2 = new Vehicle(
            "TN69CD5678",
            VehicleType.BIKE,
            1
        );

        v1.displayVehicle();

        // 3. Register driver with two vehicles

        System.out.println("\n===== DRIVER REGISTRATION =====");

        List<Vehicle> vehicles = new ArrayList<>();

        vehicles.add(v1);
        vehicles.add(v2);

        Driver driver = service.registerDriver(
            "Kumar",
            "9876501234",
            "TN2026123456",
            vehicles
        );

        driver.displayDriver();

        // 4. Try making driver available before approval

        System.out.println("\n===== AVAILABILITY CHECK =====");

        try {
            service.makeDriverAvailable(driver);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }

        // 5. Approve documents

        System.out.println("\n===== DOCUMENT APPROVAL =====");

        service.updateDocumentStatus(
            driver,
            DocumentStatus.APPROVED
        );

        // 6. Make driver available

        service.makeDriverAvailable(driver);

        // 7. Change active vehicle

        System.out.println("\n===== CHANGE ACTIVE VEHICLE =====");

        service.changeActiveVehicle(driver, v2);

        System.out.println(
            "Current vehicle: "
            + driver.getActiveVehicle().getRegistrationNumber()
        );

        // 8. Record 20 completed rides

        System.out.println("\n===== RATING AND SUSPENSION =====");

        for (int i = 1; i <= 20; i++) {
            service.recordCompletedRide(driver);
        }

        // 9. Add ratings

        service.addDriverRating(driver, 2);
        service.addDriverRating(driver, 2);

        System.out.println(
            "Average Rating: " + driver.getAverageRating()
        );

        System.out.println(
            "Suspended: " + driver.isSuspended()
        );

        System.out.println(
            "Available: " + driver.isAvailable()
        );

        // 10. Final driver details

        System.out.println("\n===== FINAL DRIVER DETAILS =====");

        driver.displayDriver();
    }
}
