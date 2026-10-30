package com.ridebooking.module4;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory repository for Payment records.
 * Provides CRUD-like access and query methods.
 * No database — backed by an ArrayList.
 */
public class PaymentRepository {

    private final List<Payment> payments = new ArrayList<>();

    // ---------- Write ----------

    /**
     * Saves a new payment record.
     *
     * @param payment payment to store (must not be null)
     */
    public void save(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment must not be null.");
        }
        payments.add(payment);
    }

    // ---------- Read ----------

    /**
     * Finds a payment by its unique payment ID.
     */
    public Optional<Payment> findById(String paymentId) {
        return payments.stream()
                .filter(p -> p.getPaymentId().equals(paymentId))
                .findFirst();
    }

    /**
     * Returns all payments for a given rider.
     */
    public List<Payment> findByRiderId(String riderId) {
        return payments.stream()
                .filter(p -> p.getRiderId().equals(riderId))
                .collect(Collectors.toList());
    }

    /**
     * Returns all payments for a given trip.
     */
    public List<Payment> findByTripId(String tripId) {
        return payments.stream()
                .filter(p -> p.getTripId().equals(tripId))
                .collect(Collectors.toList());
    }

    /**
     * Returns all payments with a specific status.
     */
    public List<Payment> findByStatus(PaymentStatus status) {
        return payments.stream()
                .filter(p -> p.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Returns all stored payment records.
     */
    public List<Payment> findAll() {
        return new ArrayList<>(payments);
    }

    /**
     * Checks whether a rider has any unresolved FAILED payments.
     */
    public boolean hasUnresolvedFailedPayment(String riderId) {
        return payments.stream()
                .anyMatch(p -> p.getRiderId().equals(riderId)
                        && p.getStatus() == PaymentStatus.FAILED);
    }

    // ---------- Update ----------

    /**
     * Updates the status of a payment identified by paymentId.
     *
     * @return true if the record was found and updated, false otherwise
     */
    public boolean updateStatus(String paymentId, PaymentStatus newStatus) {
        Optional<Payment> opt = findById(paymentId);
        opt.ifPresent(p -> p.setStatus(newStatus));
        return opt.isPresent();
    }

    // ---------- Stats ----------

    public int count() {
        return payments.size();
    }
}
