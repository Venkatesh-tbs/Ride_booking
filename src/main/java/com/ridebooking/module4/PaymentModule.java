package com.ridebooking.module4;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Module 4 – Fare & Payments CLI Entry Point.
 *
 * Provides a console-based menu to exercise all Module 4 functionality:
 *   1.  Calculate fare
 *   2.  Check / apply surge pricing
 *   3.  Process payment (Cash / UPI / Wallet)
 *   4.  Confirm cash payment after trip
 *   5.  Retry failed payment
 *   6.  View payment records
 *   7.  View wallet balance
 *   8.  Top up wallet
 *   9.  View revenue distribution
 *   10. Charge cancellation fee
 *   11. View all payments (admin)
 *   0.  Exit
 *
 * All data is in-memory (ArrayList-backed).
 */
public class PaymentModule {

    // -----------------------------------------------------------------------
    //  Services / repositories
    // -----------------------------------------------------------------------
    private final FareCalculator      fareCalculator;
    private final SurgePricingService surgeService;
    private final PaymentRepository   repository;
    private final PaymentService      paymentService;
    private final RevenueService      revenueService;
    private final Scanner             scanner;

    // -----------------------------------------------------------------------
    //  Constructor
    // -----------------------------------------------------------------------
    public PaymentModule() {
        this.fareCalculator  = new FareCalculator();
        this.surgeService    = new SurgePricingService();
        this.repository      = new PaymentRepository();
        this.paymentService  = new PaymentService(repository);
        this.revenueService  = new RevenueService();
        this.scanner         = new Scanner(System.in);
    }

    // -----------------------------------------------------------------------
    //  main
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        new PaymentModule().run();
    }

    // -----------------------------------------------------------------------
    //  Menu loop
    // -----------------------------------------------------------------------
    public void run() {
        printBanner();
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter choice: ");
            System.out.println();
            switch (choice) {
                case 1  -> calculateFare();
                case 2  -> checkSurgePricing();
                case 3  -> processPayment();
                case 4  -> confirmCashPayment();
                case 5  -> retryFailedPayment();
                case 6  -> viewRiderPayments();
                case 7  -> viewWalletBalance();
                case 8  -> topUpWallet();
                case 9  -> viewRevenueDistribution();
                case 10 -> chargeCancellationFee();
                case 11 -> viewAllPayments();
                case 0  -> { running = false; System.out.println("Exiting Module 4. Goodbye!"); }
                default -> System.out.println("[ERROR] Invalid option. Please try again.");
            }
        }
    }

    // -----------------------------------------------------------------------
    //  Option 1 – Calculate Fare
    // -----------------------------------------------------------------------
    private void calculateFare() {
        System.out.println("=== Calculate Fare ===");
        VehicleType vehicle   = selectVehicleType();
        if (vehicle == null) return;
        double distance       = readPositiveDouble("Enter distance (km): ");
        int    duration       = readNonNegativeInt ("Enter trip duration (minutes): ");

        System.out.println("\nDo you want to check surge pricing first?");
        System.out.print("Enter pending requests (press 0 to skip surge check): ");
        int pending = readInt("");
        double surge = 1.0;
        if (pending > 0) {
            int available = readNonNegativeInt("Enter available drivers: ");
            surge = surgeService.getSurgeMultiplier(pending, available);
            surgeService.printSurgeStatus(pending, available);
        }

        FareDetails details = fareCalculator.calculate(vehicle, distance, duration, surge);
        details.printBreakdown();
    }

    // -----------------------------------------------------------------------
    //  Option 2 – Check Surge Pricing
    // -----------------------------------------------------------------------
    private void checkSurgePricing() {
        System.out.println("=== Surge Pricing Check ===");
        int pending   = readNonNegativeInt("Enter number of pending ride requests: ");
        int available = readNonNegativeInt("Enter number of available drivers  : ");
        surgeService.printSurgeStatus(pending, available);
    }

    // -----------------------------------------------------------------------
    //  Option 3 – Process Payment
    // -----------------------------------------------------------------------
    private void processPayment() {
        System.out.println("=== Process Payment ===");

        String riderId = readNonEmptyString("Enter Rider ID          : ");
        String tripId  = readNonEmptyString("Enter Trip ID           : ");

        // Check for FAILED payment block
        if (repository.hasUnresolvedFailedPayment(riderId)) {
            System.out.println("\n[BLOCKED] Rider " + riderId +
                    " has an unresolved FAILED payment.");
            System.out.println("  Use option 5 (Retry Failed Payment) to resolve it first.");
            return;
        }

        // Fare calculation
        VehicleType vehicle  = selectVehicleType();
        if (vehicle == null) return;
        double distance      = readPositiveDouble("Enter distance (km)     : ");
        int    duration      = readNonNegativeInt ("Enter duration (minutes): ");

        System.out.println("\nApply surge pricing?");
        int pending = readNonNegativeInt("Pending requests (0 = no surge): ");
        double surge = 1.0;
        if (pending > 0) {
            int available = readNonNegativeInt("Available drivers: ");
            surge = surgeService.getSurgeMultiplier(pending, available);
            surgeService.printSurgeStatus(pending, available);
        }

        FareDetails details = fareCalculator.calculate(vehicle, distance, duration, surge);
        details.printBreakdown();

        System.out.print("\nProceed to payment? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("y")) {
            System.out.println("Payment cancelled by user.");
            return;
        }

        PaymentMethod method = selectPaymentMethod();
        if (method == null) return;

        // For WALLET — ensure wallet exists
        if (method == PaymentMethod.WALLET) {
            Optional<Wallet> wallet = paymentService.getWallet(riderId);
            if (wallet.isEmpty()) {
                System.out.print("No wallet found. Create one with initial balance (Rs.): ");
                double initBal = readPositiveDouble("");
                paymentService.getOrCreateWallet(riderId, initBal);
            }
        }

        Payment payment = paymentService.processPayment(tripId, riderId, details.getFinalFare(), method);
        if (payment != null) {
            System.out.println("\n[SUCCESS] Payment recorded:");
            payment.printSummary();

            // Auto-distribute revenue for PAID payments
            if (payment.getStatus() == PaymentStatus.PAID) {
                revenueService.distributeRevenue(payment);
            }
        }
    }

    // -----------------------------------------------------------------------
    //  Option 4 – Confirm Cash Payment
    // -----------------------------------------------------------------------
    private void confirmCashPayment() {
        System.out.println("=== Confirm Cash Payment ===");
        String paymentId = readNonEmptyString("Enter Payment ID to confirm: ");
        paymentService.confirmCashPayment(paymentId);

        // Distribute revenue if now PAID
        repository.findById(paymentId).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.PAID) {
                revenueService.distributeRevenue(p);
            }
        });
    }

    // -----------------------------------------------------------------------
    //  Option 5 – Retry Failed Payment
    // -----------------------------------------------------------------------
    private void retryFailedPayment() {
        System.out.println("=== Retry Failed Payment ===");
        String riderId = readNonEmptyString("Enter Rider ID     : ");

        // Show FAILED payments for this rider
        List<Payment> failed = repository.findByRiderId(riderId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.FAILED)
                .toList();

        if (failed.isEmpty()) {
            System.out.println("[INFO] No FAILED payments found for rider " + riderId + ".");
            return;
        }

        System.out.println("\nFailed payments for rider " + riderId + ":");
        failed.forEach(Payment::printSummary);

        String paymentId  = readNonEmptyString("Enter Payment ID to retry: ");
        PaymentMethod method = selectPaymentMethod();
        if (method == null) return;

        // For WALLET retry — ensure wallet exists and has funds
        if (method == PaymentMethod.WALLET) {
            Optional<Wallet> wallet = paymentService.getWallet(riderId);
            if (wallet.isEmpty()) {
                System.out.print("No wallet found. Create one with initial balance (Rs.): ");
                double initBal = readPositiveDouble("");
                paymentService.getOrCreateWallet(riderId, initBal);
            } else {
                System.out.printf("Current wallet balance: Rs. %.2f%n", wallet.get().getBalance());
            }
        }

        Payment retryResult = paymentService.retryPayment(paymentId, method);
        if (retryResult != null) {
            System.out.println("\n[RETRY RESULT]");
            retryResult.printSummary();
            if (retryResult.getStatus() == PaymentStatus.PAID) {
                revenueService.distributeRevenue(retryResult);
            }
        }
    }

    // -----------------------------------------------------------------------
    //  Option 6 – View Rider Payment History
    // -----------------------------------------------------------------------
    private void viewRiderPayments() {
        System.out.println("=== Rider Payment History ===");
        String riderId     = readNonEmptyString("Enter Rider ID: ");
        List<Payment> list = repository.findByRiderId(riderId);
        if (list.isEmpty()) {
            System.out.println("[INFO] No payment records found for rider " + riderId + ".");
        } else {
            System.out.println("\nPayment records for rider " + riderId + " (" + list.size() + " found):");
            list.forEach(Payment::printSummary);
        }
    }

    // -----------------------------------------------------------------------
    //  Option 7 – View Wallet Balance
    // -----------------------------------------------------------------------
    private void viewWalletBalance() {
        System.out.println("=== Wallet Balance ===");
        String riderId = readNonEmptyString("Enter Rider ID: ");
        Optional<Wallet> walletOpt = paymentService.getWallet(riderId);
        if (walletOpt.isPresent()) {
            Wallet w = walletOpt.get();
            System.out.printf("  Wallet for %s: Rs. %.2f%n", riderId, w.getBalance());
        } else {
            System.out.println("[INFO] No wallet found for rider " + riderId + ".");
            System.out.print("Create wallet with initial balance? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                double bal = readPositiveDouble("Initial balance (Rs.): ");
                Wallet w = paymentService.getOrCreateWallet(riderId, bal);
                System.out.println("Wallet created: " + w);
            }
        }
    }

    // -----------------------------------------------------------------------
    //  Option 8 – Top Up Wallet
    // -----------------------------------------------------------------------
    private void topUpWallet() {
        System.out.println("=== Top Up Wallet ===");
        String riderId = readNonEmptyString("Enter Rider ID       : ");
        double amount  = readPositiveDouble("Enter top-up amount  : ");
        paymentService.topUpWallet(riderId, amount);
        paymentService.getWallet(riderId).ifPresent(w ->
                System.out.printf("  New wallet balance: Rs. %.2f%n", w.getBalance()));
    }

    // -----------------------------------------------------------------------
    //  Option 9 – View Revenue Distribution
    // -----------------------------------------------------------------------
    private void viewRevenueDistribution() {
        System.out.println("=== Revenue Distribution ===");
        System.out.println("  1. Distribution for a specific payment");
        System.out.println("  2. Revenue summary (all PAID payments)");
        int choice = readInt("Choose: ");
        if (choice == 1) {
            String paymentId = readNonEmptyString("Enter Payment ID: ");
            repository.findById(paymentId).ifPresentOrElse(
                    p -> revenueService.distributeRevenue(p),
                    () -> System.out.println("[ERROR] Payment not found: " + paymentId));
        } else if (choice == 2) {
            revenueService.printRevenueSummary(repository.findAll());
        } else {
            System.out.println("[ERROR] Invalid choice.");
        }
    }

    // -----------------------------------------------------------------------
    //  Option 10 – Charge Cancellation Fee
    // -----------------------------------------------------------------------
    private void chargeCancellationFee() {
        System.out.println("=== Charge Cancellation Fee ===");
        System.out.printf("  Cancellation fee: Rs. %.2f (goes entirely to the driver)%n",
                RevenueService.CANCELLATION_FEE);
        String riderId = readNonEmptyString("Enter Rider ID: ");
        String tripId  = readNonEmptyString("Enter Trip ID : ");

        Payment cancelPayment = paymentService.chargeCancellationFee(tripId, riderId);
        System.out.println("\nCancellation payment record:");
        cancelPayment.printSummary();
        revenueService.distributeCancellationRevenue(cancelPayment);
    }

    // -----------------------------------------------------------------------
    //  Option 11 – View All Payments (Admin)
    // -----------------------------------------------------------------------
    private void viewAllPayments() {
        System.out.println("=== All Payment Records ===");
        List<Payment> all = repository.findAll();
        if (all.isEmpty()) {
            System.out.println("[INFO] No payment records yet.");
        } else {
            System.out.println("Total records: " + all.size());
            all.forEach(Payment::printSummary);
        }
    }

    // -----------------------------------------------------------------------
    //  UI helpers
    // -----------------------------------------------------------------------

    private void printBanner() {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   MODULE 4 — FARE & PAYMENTS MANAGEMENT      ║");
        System.out.println("║   Ride Booking System | In-Memory CLI         ║");
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    private void printMenu() {
        System.out.println("\n============================================");
        System.out.println("  M O D U L E   4   M A I N   M E N U");
        System.out.println("============================================");
        System.out.println("  1.  Calculate Fare");
        System.out.println("  2.  Check / Apply Surge Pricing");
        System.out.println("  3.  Process Payment (Cash / UPI / Wallet)");
        System.out.println("  4.  Confirm Cash Payment (after trip)");
        System.out.println("  5.  Retry Failed Payment");
        System.out.println("  6.  View Rider Payment History");
        System.out.println("  7.  View Wallet Balance");
        System.out.println("  8.  Top Up Wallet");
        System.out.println("  9.  View Revenue Distribution");
        System.out.println("  10. Charge Cancellation Fee");
        System.out.println("  11. View All Payments (Admin)");
        System.out.println("  0.  Exit");
        System.out.println("============================================");
    }

    private VehicleType selectVehicleType() {
        System.out.println("Vehicle types:");
        VehicleType[] types = VehicleType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("  %d. %-6s (Rs. %.0f/km)%n", i + 1, types[i].name(), types[i].getRatePerKm());
        }
        int choice = readInt("Select vehicle type: ");
        if (choice < 1 || choice > types.length) {
            System.out.println("[ERROR] Invalid vehicle type selection.");
            return null;
        }
        return types[choice - 1];
    }

    private PaymentMethod selectPaymentMethod() {
        System.out.println("Payment methods:");
        System.out.println("  1. CASH");
        System.out.println("  2. UPI");
        System.out.println("  3. WALLET");
        int choice = readInt("Select payment method: ");
        switch (choice) {
            case 1 -> { return PaymentMethod.CASH; }
            case 2 -> { return PaymentMethod.UPI; }
            case 3 -> { return PaymentMethod.WALLET; }
            default -> {
                System.out.println("[ERROR] Invalid payment method selection.");
                return null;
            }
        }
    }

    // ---------- Input readers ----------

    private int readInt(String prompt) {
        while (true) {
            if (!prompt.isEmpty()) System.out.print(prompt);
            try {
                String line = scanner.nextLine().trim();
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("[ERROR] Please enter a valid integer: ");
            }
        }
    }

    private int readNonNegativeInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value >= 0) return value;
            System.out.print("[ERROR] Value cannot be negative. Try again: ");
        }
    }

    private double readPositiveDouble(String prompt) {
        while (true) {
            if (!prompt.isEmpty()) System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value > 0) return value;
                System.out.print("[ERROR] Value must be greater than 0. Try again: ");
            } catch (NumberFormatException e) {
                System.out.print("[ERROR] Please enter a valid number: ");
            }
        }
    }

    private String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("[ERROR] Input cannot be empty.");
        }
    }
}
