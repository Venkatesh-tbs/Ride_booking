package com.ridebooking.module5;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * CLI menu for Module 5 – Ratings.
 *
 * All interaction happens through the terminal using Scanner (input)
 * and System.out.println (output). No GUI, Swing or web components used.
 *
 * Integration:
 *   Call RatingsMenu.show(scanner) from the main application menu
 *   to launch the ratings sub-menu. The Scanner instance should be
 *   the same one used throughout the application to avoid resource leaks.
 */
public class RatingsMenu {

    // ---------------------------------------------------------------
    // Dependencies
    // ---------------------------------------------------------------
    private final RatingsService service;
    private final Scanner        scanner;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------
    public RatingsMenu(RatingsService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    // ---------------------------------------------------------------
    // Main Menu Loop
    // ---------------------------------------------------------------

    /**
     * Display and run the ratings sub-menu until the user chooses to exit.
     */
    public void show() {
        boolean running = true;
        while (running) {
            printMenuHeader();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> handleRiderRatesDriver();
                case 2 -> handleDriverRatesRider();
                case 3 -> handleViewUserRatings();
                case 4 -> handleViewAverageRating();
                case 5 -> handleViewAllRatings();
                case 6 -> handleViewFlaggedRiders();
                case 0 -> { running = false; printLine("\nReturning to main menu..."); }
                default -> printLine("  [!] Invalid choice. Please select 0–6.");
            }
        }
    }

    // ---------------------------------------------------------------
    // Menu Options
    // ---------------------------------------------------------------

    /** Option 1 – Rider rates Driver */
    private void handleRiderRatesDriver() {
        printSectionHeader("RIDER RATES DRIVER");

        String tripId           = readString("  Enter Trip ID              : ");
        String riderId          = readString("  Enter Rider ID             : ");
        String driverId         = readString("  Enter Driver ID            : ");
        int    score            = readRatingScore();
        LocalDateTime completed = readTripCompletedAt();

        RatingResult result = service.riderRatesDriver(
            tripId, riderId, driverId, score, completed
        );
        printResult(result);
    }

    /** Option 2 – Driver rates Rider */
    private void handleDriverRatesRider() {
        printSectionHeader("DRIVER RATES RIDER");

        String tripId           = readString("  Enter Trip ID              : ");
        String driverId         = readString("  Enter Driver ID            : ");
        String riderId          = readString("  Enter Rider ID             : ");
        int    score            = readRatingScore();
        LocalDateTime completed = readTripCompletedAt();

        RatingResult result = service.driverRatesRider(
            tripId, driverId, riderId, score, completed
        );
        printResult(result);
    }

    /** Option 3 – View all ratings received by a user */
    private void handleViewUserRatings() {
        printSectionHeader("VIEW RATINGS FOR USER");

        String userId = readString("  Enter User ID: ");
        List<Rating> ratings = service.getRatingsForUser(userId);

        if (ratings.isEmpty()) {
            printLine("  No ratings found for user: " + userId);
        } else {
            printLine("  Ratings received by [" + userId + "]:");
            printLine("  " + "-".repeat(70));
            for (Rating r : ratings) {
                printLine("  " + r);
            }
            printLine("  " + "-".repeat(70));
            printLine(String.format(
                "  Total: %d rating(s) | Average: %.2f/5",
                service.getRatingCount(userId),
                service.getAverageRating(userId)
            ));
        }
    }

    /** Option 4 – View average rating for a user */
    private void handleViewAverageRating() {
        printSectionHeader("VIEW AVERAGE RATING");

        String userId    = readString("  Enter User ID: ");
        double avg       = service.getAverageRating(userId);
        int    count     = service.getRatingCount(userId);
        boolean flagged  = service.isRiderFlagged(userId);

        if (count == 0) {
            printLine("  User [" + userId + "] has not received any ratings yet.");
        } else {
            printLine(String.format(
                "  User [%s]: Average Rating = %.2f/5 (based on %d rating%s)%s",
                userId, avg, count,
                count == 1 ? "" : "s",
                flagged ? "  *** FLAGGED FOR ADMIN REVIEW ***" : ""
            ));
        }
    }

    /** Option 5 – View all ratings in the system */
    private void handleViewAllRatings() {
        printSectionHeader("ALL RATINGS IN SYSTEM");

        List<Rating> all = service.getAllRatings();
        if (all.isEmpty()) {
            printLine("  No ratings have been submitted yet.");
        } else {
            printLine("  Total ratings: " + all.size());
            printLine("  " + "-".repeat(70));
            for (Rating r : all) {
                printLine("  " + r);
            }
            printLine("  " + "-".repeat(70));
        }
    }

    /** Option 6 – View all flagged riders */
    private void handleViewFlaggedRiders() {
        printSectionHeader("FLAGGED RIDERS (Admin View)");

        Map<String, String> flagged = service.getFlaggedRiders();
        if (flagged.isEmpty()) {
            printLine("  No riders are currently flagged for review.");
        } else {
            printLine("  Riders flagged for admin review (" + flagged.size() + "):");
            printLine("  " + "-".repeat(70));
            for (Map.Entry<String, String> entry : flagged.entrySet()) {
                printLine("  Rider ID : " + entry.getKey());
                printLine("  Reason   : " + entry.getValue());
                printLine("  " + "-".repeat(70));
            }
        }
    }

    // ---------------------------------------------------------------
    // Input Helpers
    // ---------------------------------------------------------------

    /**
     * Read a rating score (1–5) with re-prompt on invalid numeric input.
     */
    private int readRatingScore() {
        while (true) {
            System.out.print("  Enter Rating Score (1–5)    : ");
            String line = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val >= Rating.MIN_RATING && val <= Rating.MAX_RATING) {
                    return val;
                }
                printLine("  [!] Score must be between "
                    + Rating.MIN_RATING + " and " + Rating.MAX_RATING + ". Try again.");
            } catch (NumberFormatException e) {
                printLine("  [!] Please enter a valid integer (1–5).");
            }
        }
    }

    /**
     * Ask the user when the trip was completed.
     * Accepts "now" for current time, or an offset in hours (e.g. "-5" = 5 hours ago).
     */
    private LocalDateTime readTripCompletedAt() {
        printLine("  Trip completed at: type 'now' OR enter hours ago as a negative number (e.g. -2)");
        while (true) {
            System.out.print("  Trip completed at           : ");
            String line = scanner.nextLine().trim().toLowerCase();
            if (line.equals("now")) {
                return LocalDateTime.now();
            }
            try {
                long hoursAgo = Long.parseLong(line);
                if (hoursAgo > 0) {
                    printLine("  [!] Enter 'now' or a negative number for hours ago (e.g. -2).");
                    continue;
                }
                return LocalDateTime.now().plusHours(hoursAgo);
            } catch (NumberFormatException e) {
                printLine("  [!] Invalid input. Type 'now' or a negative number like -3.");
            }
        }
    }

    /** Read a non-blank string. */
    private String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            printLine("  [!] Input cannot be empty.");
        }
    }

    /** Read an integer, re-prompt on parse failure. */
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                printLine("  [!] Please enter a valid number.");
            }
        }
    }

    // ---------------------------------------------------------------
    // Display Helpers
    // ---------------------------------------------------------------

    private void printMenuHeader() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════╗");
        System.out.println("  ║       MODULE 5 — RATINGS MENU        ║");
        System.out.println("  ╠══════════════════════════════════════╣");
        System.out.println("  ║  1. Rider rates Driver               ║");
        System.out.println("  ║  2. Driver rates Rider               ║");
        System.out.println("  ║  3. View ratings for a user          ║");
        System.out.println("  ║  4. View average rating for a user   ║");
        System.out.println("  ║  5. View all ratings (system-wide)   ║");
        System.out.println("  ║  6. View flagged riders (Admin)      ║");
        System.out.println("  ║  0. Back to main menu                ║");
        System.out.println("  ╚══════════════════════════════════════╝");
    }

    private void printSectionHeader(String title) {
        System.out.println();
        System.out.println("  --- " + title + " ---");
    }

    private void printResult(RatingResult result) {
        System.out.println();
        System.out.println("  " + result);
        System.out.println();
    }

    private void printLine(String line) {
        System.out.println(line);
    }
}
