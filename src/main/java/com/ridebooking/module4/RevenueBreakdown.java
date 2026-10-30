package com.ridebooking.module4;

/**
 * Immutable value object that holds a revenue split result.
 */
public class RevenueBreakdown {

    private final String  paymentId;
    private final String  tripId;
    private final double  totalAmount;
    private final double  driverShare;
    private final double  platformShare;
    private final boolean isCancellation;

    public RevenueBreakdown(String paymentId,
                            String tripId,
                            double totalAmount,
                            double driverShare,
                            double platformShare,
                            boolean isCancellation) {
        this.paymentId      = paymentId;
        this.tripId         = tripId;
        this.totalAmount    = totalAmount;
        this.driverShare    = driverShare;
        this.platformShare  = platformShare;
        this.isCancellation = isCancellation;
    }

    // ---------- Getters ----------

    public String  getPaymentId()      { return paymentId; }
    public String  getTripId()         { return tripId; }
    public double  getTotalAmount()    { return totalAmount; }
    public double  getDriverShare()    { return driverShare; }
    public double  getPlatformShare()  { return platformShare; }
    public boolean isCancellation()   { return isCancellation; }

    // ---------- Display ----------

    public void print() {
        System.out.println("\n-------- REVENUE DISTRIBUTION --------");
        System.out.printf("  Payment ID   : %s%n", paymentId);
        System.out.printf("  Trip ID      : %s%n", tripId);
        if (isCancellation) {
            System.out.printf("  Type         : CANCELLATION FEE%n");
            System.out.printf("  Total Fee    : Rs. %.2f%n", totalAmount);
            System.out.printf("  Driver gets  : Rs. %.2f (100%%)%n", driverShare);
            System.out.printf("  Platform gets: Rs. %.2f (0%%)%n", platformShare);
        } else {
            System.out.printf("  Type         : TRIP PAYMENT%n");
            System.out.printf("  Total Fare   : Rs. %.2f%n", totalAmount);
            System.out.printf("  Driver gets  : Rs. %.2f (80%%)%n", driverShare);
            System.out.printf("  Platform gets: Rs. %.2f (20%%)%n", platformShare);
        }
        System.out.println("--------------------------------------");
    }
}
