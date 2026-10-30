package com.ridebooking.module4;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single payment record for a ride.
 * Stores payment ID, trip ID, rider ID, amount, date, method, and status.
 */
public class Payment {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String        paymentId;
    private final String        tripId;
    private final String        riderId;
    private final double        amount;
    private final LocalDateTime paymentDate;
    private final PaymentMethod method;
    private       PaymentStatus status;

    public Payment(String paymentId,
                   String tripId,
                   String riderId,
                   double amount,
                   PaymentMethod method,
                   PaymentStatus status) {
        this.paymentId   = paymentId;
        this.tripId      = tripId;
        this.riderId     = riderId;
        this.amount      = Math.round(amount * 100.0) / 100.0;
        this.method      = method;
        this.status      = status;
        this.paymentDate = LocalDateTime.now();
    }

    // ---------- Getters ----------

    public String        getPaymentId()   { return paymentId; }
    public String        getTripId()      { return tripId; }
    public String        getRiderId()     { return riderId; }
    public double        getAmount()      { return amount; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public PaymentMethod getMethod()      { return method; }
    public PaymentStatus getStatus()      { return status; }

    // ---------- Mutators ----------

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    // ---------- Display ----------

    public void printSummary() {
        System.out.println("  +---------------------------------+");
        System.out.printf ("  | Payment ID : %-20s|%n", paymentId);
        System.out.printf ("  | Trip ID    : %-20s|%n", tripId);
        System.out.printf ("  | Rider ID   : %-20s|%n", riderId);
        System.out.printf ("  | Amount     : Rs. %-17.2f|%n", amount);
        System.out.printf ("  | Date       : %-20s|%n", paymentDate.format(FORMATTER));
        System.out.printf ("  | Method     : %-20s|%n", method);
        System.out.printf ("  | Status     : %-20s|%n", status);
        System.out.println("  +---------------------------------+");
    }

    @Override
    public String toString() {
        return String.format("Payment{id=%s, tripId=%s, riderId=%s, amount=%.2f, method=%s, status=%s}",
                paymentId, tripId, riderId, amount, method, status);
    }
}
