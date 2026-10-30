package com.ridebooking.module4;

import java.util.List;

/**
 * Calculates and displays revenue distribution for completed trips and cancellations.
 *
 * Distribution rules:
 *   Normal trip  : Driver 80% | Platform 20%
 *   Cancellation : Driver 100% of the Rs. 30 cancellation fee
 */
public class RevenueService {

    public static final double CANCELLATION_FEE       = 30.0;
    public static final double DRIVER_SHARE_PERCENT   = 0.80;
    public static final double PLATFORM_SHARE_PERCENT = 0.20;

    /**
     * Calculates and prints the revenue split for a completed trip payment.
     *
     * @param payment the payment record (must have status PAID)
     * @return a RevenueBreakdown holding the split amounts
     */
    public RevenueBreakdown distributeRevenue(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment must not be null.");
        }
        if (payment.getStatus() != PaymentStatus.PAID) {
            System.out.printf("[REVENUE] Payment %s is not PAID (status=%s). Skipping distribution.%n",
                    payment.getPaymentId(), payment.getStatus());
            return null;
        }

        double total          = payment.getAmount();
        double driverShare    = Math.round(total * DRIVER_SHARE_PERCENT   * 100.0) / 100.0;
        double platformShare  = Math.round(total * PLATFORM_SHARE_PERCENT * 100.0) / 100.0;

        RevenueBreakdown breakdown = new RevenueBreakdown(
                payment.getPaymentId(),
                payment.getTripId(),
                total,
                driverShare,
                platformShare,
                false
        );
        breakdown.print();
        return breakdown;
    }

    /**
     * Calculates and prints the revenue split for a cancellation fee.
     * The entire fee (Rs. 30) goes to the driver.
     *
     * @param payment the cancellation-fee payment record
     * @return a RevenueBreakdown
     */
    public RevenueBreakdown distributeCancellationRevenue(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment must not be null.");
        }

        double total = payment.getAmount();   // Should be Rs. 30
        RevenueBreakdown breakdown = new RevenueBreakdown(
                payment.getPaymentId(),
                payment.getTripId(),
                total,
                total,   // 100 % to driver
                0.0,     // 0 % to platform
                true
        );
        breakdown.print();
        return breakdown;
    }

    /**
     * Generates a summary of total revenue from a list of PAID payments.
     */
    public void printRevenueSummary(List<Payment> payments) {
        double totalRevenue   = 0;
        double totalDriver    = 0;
        double totalPlatform  = 0;
        int    paidCount      = 0;

        for (Payment p : payments) {
            if (p.getStatus() == PaymentStatus.PAID) {
                double amount       = p.getAmount();
                totalRevenue       += amount;
                totalDriver        += amount * DRIVER_SHARE_PERCENT;
                totalPlatform      += amount * PLATFORM_SHARE_PERCENT;
                paidCount++;
            }
        }

        System.out.println("\n========== REVENUE SUMMARY ==========");
        System.out.printf("  Paid Transactions : %d%n", paidCount);
        System.out.printf("  Total Revenue     : Rs. %.2f%n", totalRevenue);
        System.out.printf("  Driver Earnings   : Rs. %.2f (80%%)%n", totalDriver);
        System.out.printf("  Platform Earnings : Rs. %.2f (20%%)%n", totalPlatform);
        System.out.println("=====================================");
    }
}
