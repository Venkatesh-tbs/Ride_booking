package com.ridebooking.module5;

import java.util.Scanner;

/**
 * Module 5 – Ratings entry point / wiring class.
 *
 * This class bootstraps the Ratings module by creating the repository,
 * service and menu, then launching the CLI menu.
 *
 * Integration with Main.java:
 *   From the main application menu, construct and call:
 *
 *       RatingsModule module5 = new RatingsModule(sharedScanner);
 *       module5.launch();
 *
 *   Pass the same Scanner instance that the rest of the application uses.
 *
 * Note: Java does not allow a class named "5" (starts with a digit).
 *       This file retains the name 5.java as required by the project
 *       structure rule, but the public class is named RatingsModule.
 *       Compile this file and reference RatingsModule from the main app.
 */
public class RatingsModule {

    // ---------------------------------------------------------------
    // Wired components
    // ---------------------------------------------------------------
    private final RatingsMenu menu;

    // ---------------------------------------------------------------
    // Constructor — wires up the full module
    // ---------------------------------------------------------------
    public RatingsModule(Scanner scanner) {
        RatingsRepository repository = new RatingsRepository();
        RatingsService    service    = new RatingsService(repository);
        this.menu = new RatingsMenu(service, scanner);
    }

    // ---------------------------------------------------------------
    // Launch
    // ---------------------------------------------------------------

    /**
     * Start the Ratings module CLI menu.
     * Call this from Main.java or any parent menu.
     */
    public void launch() {
        menu.show();
    }

    // ---------------------------------------------------------------
    // Stand-alone demo entry (for testing module5 in isolation)
    // ---------------------------------------------------------------

    /**
     * Stand-alone runner — only used when testing Module 5 independently.
     * Do NOT call this from Main.java; use launch() instead.
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Ride Booking System - Module 5 Demo  ");
        System.out.println("         Ratings Sub-System             ");
        System.out.println("========================================");

        Scanner scanner = new Scanner(System.in);
        RatingsModule module5 = new RatingsModule(scanner);
        module5.launch();

        scanner.close();
        System.out.println("Module 5 demo ended.");
    }
}
