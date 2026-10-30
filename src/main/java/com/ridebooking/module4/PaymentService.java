package com.ridebooking.module4;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Handles payment processing for all supported payment methods:
 *   CASH   — remains PENDING until trip completion and cash confirmation.
 *   UPI    — randomly simulates PAID or FAILED.
 *   WALLET — checks balance and deducts when sufficient; FAILED otherwise.
 *
 * Also enforces the FAILED-payment restriction: a rider with an unresolved
 * FAILED payment cannot initiate another ride until the payment is retried.
 */
public class PaymentService {

    private static final double UPI_SUCCESS_PROBABILITY = 0.80; // 80 % success rate for UPI simulation

    private final PaymentRepository repository;
    private final Map<String, Wallet> wallets;   // riderId → Wallet
    private final Random random;
    private int paymentCounter = 1;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
        this.wallets    = new HashMap<>();
        this.random     = new Random();
    }

    // =====================================================================
    //  Wallet management
    // =====================================================================

    /**
     * Creates a wallet for a rider with the given initial balance.
     * If one already exists, it is returned unchanged.
     */
    public Wallet getOrCreateWallet(String riderId, double initialBalance) {
        wallets.putIfAbsent(riderId, new Wallet(riderId, initialBalance));
        return wallets.get(riderId);
    }

    /**
     * Returns the wallet for a rider, or empty if none exists.
     */
    public Optional<Wallet> getWallet(String riderId) {
        return Optional.ofNullable(wallets.get(riderId));
    }

    /**
     * Tops up the rider's wallet. Creates one if it does not exist.
     */
    public void topUpWallet(String riderId, double amount) {
        Wallet wallet = wallets.computeIfAbsent(riderId, id -> new Wallet(id, 0.0));
        wallet.deposit(amount);
    }

    // =====================================================================
    //  Payment processing
    // =====================================================================

    /**
     * Processes a payment for a trip using the chosen method.
     *
     * @param tripId  unique trip identifier
     * @param riderId rider placing the payment
     * @param amount  fare amount to be paid
     * @param method  CASH / UPI / WALLET
     * @return the saved Payment record
     */
    public Payment processPayment(String tripId, String riderId, double amount, PaymentMethod method) {
        // Guard: check for unresolved FAILED payments
        if (repository.hasUnresolvedFailedPayment(riderId)) {
            System.out.println("\n[BLOCKED] Rider " + riderId +
                    " has an unresolved FAILED payment. Please retry the failed payment first.");
            return null;
        }

        String paymentId = generatePaymentId();
        Payment payment;

        switch (method) {
            case CASH:
                payment = processCash(paymentId, tripId, riderId, amount);
                break;
            case UPI:
                payment = processUpi(paymentId, tripId, riderId, amount);
                break;
            case WALLET:
                payment = processWallet(paymentId, tripId, riderId, amount);
                break;
            default:
                throw new IllegalArgumentException("Unsupported payment method: " + method);
        }

        repository.save(payment);
        return payment;
    }

    // =====================================================================
    //  Cash payment
    // =====================================================================

    private Payment processCash(String paymentId, String tripId, String riderId, double amount) {
        System.out.println("\n[CASH] Payment initiated. Status: PENDING.");
        System.out.println("       Cash will be collected at trip completion.");
        return new Payment(paymentId, tripId, riderId, amount, PaymentMethod.CASH, PaymentStatus.PENDING);
    }

    /**
     * Confirms a PENDING cash payment after physical cash is handed over.
     *
     * @param paymentId the ID of the cash payment to confirm
     * @return true if confirmed successfully
     */
    public boolean confirmCashPayment(String paymentId) {
        Optional<Payment> opt = repository.findById(paymentId);
        if (opt.isEmpty()) {
            System.out.println("[ERROR] Payment not found: " + paymentId);
            return false;
        }
        Payment p = opt.get();
        if (p.getMethod() != PaymentMethod.CASH) {
            System.out.println("[ERROR] Payment " + paymentId + " is not a CASH payment.");
            return false;
        }
        if (p.getStatus() != PaymentStatus.PENDING) {
            System.out.println("[INFO] Payment " + paymentId + " is already " + p.getStatus());
            return false;
        }
        p.setStatus(PaymentStatus.PAID);
        System.out.println("[CASH] Cash confirmed. Payment " + paymentId + " marked as PAID.");
        return true;
    }

    // =====================================================================
    //  UPI payment
    // =====================================================================

    private Payment processUpi(String paymentId, String tripId, String riderId, double amount) {
        // Simulate network response
        boolean success = random.nextDouble() < UPI_SUCCESS_PROBABILITY;
        PaymentStatus status = success ? PaymentStatus.PAID : PaymentStatus.FAILED;
        System.out.printf("%n[UPI] Transaction result: %s%n", status);
        if (!success) {
            System.out.println("[UPI] Payment FAILED. Use retry option to attempt again.");
        }
        return new Payment(paymentId, tripId, riderId, amount, PaymentMethod.UPI, status);
    }

    // =====================================================================
    //  Wallet payment
    // =====================================================================

    private Payment processWallet(String paymentId, String tripId, String riderId, double amount) {
        Wallet wallet = wallets.get(riderId);
        if (wallet == null) {
            System.out.println("[WALLET] No wallet found for rider " + riderId + ". Payment FAILED.");
            return new Payment(paymentId, tripId, riderId, amount, PaymentMethod.WALLET, PaymentStatus.FAILED);
        }
        System.out.printf("[WALLET] Current balance: Rs. %.2f | Required: Rs. %.2f%n",
                wallet.getBalance(), amount);
        boolean deducted = wallet.deduct(amount);
        if (deducted) {
            System.out.printf("[WALLET] Rs. %.2f deducted. Remaining balance: Rs. %.2f%n",
                    amount, wallet.getBalance());
            return new Payment(paymentId, tripId, riderId, amount, PaymentMethod.WALLET, PaymentStatus.PAID);
        } else {
            System.out.println("[WALLET] Insufficient balance. Payment FAILED.");
            System.out.println("[WALLET] Please top up your wallet and retry.");
            return new Payment(paymentId, tripId, riderId, amount, PaymentMethod.WALLET, PaymentStatus.FAILED);
        }
    }

    // =====================================================================
    //  Retry failed payment
    // =====================================================================

    /**
     * Retries the most recent FAILED payment for a rider using a new method.
     *
     * @param paymentId the failed payment ID to retry
     * @param newMethod the payment method to use for retry
     * @return the new Payment record, or null if retry is not applicable
     */
    public Payment retryPayment(String paymentId, PaymentMethod newMethod) {
        Optional<Payment> opt = repository.findById(paymentId);
        if (opt.isEmpty()) {
            System.out.println("[ERROR] Payment not found: " + paymentId);
            return null;
        }
        Payment original = opt.get();
        if (original.getStatus() != PaymentStatus.FAILED) {
            System.out.println("[INFO] Payment " + paymentId + " is not in FAILED state. Cannot retry.");
            return null;
        }

        System.out.println("\n[RETRY] Retrying payment " + paymentId + " via " + newMethod + "...");

        // Mark original as PAID (superseded) to remove the block, then process new payment
        // We nullify the old FAILED record by marking it PAID (resolved)
        original.setStatus(PaymentStatus.PAID);   // resolved — block lifted
        System.out.println("[RETRY] Original FAILED record resolved. Processing new payment...");

        // Process fresh payment (block already lifted by resolving original)
        String newPaymentId = generatePaymentId();
        Payment retryPayment;

        switch (newMethod) {
            case CASH:
                retryPayment = processCash(newPaymentId, original.getTripId(),
                        original.getRiderId(), original.getAmount());
                break;
            case UPI:
                retryPayment = processUpi(newPaymentId, original.getTripId(),
                        original.getRiderId(), original.getAmount());
                break;
            case WALLET:
                retryPayment = processWallet(newPaymentId, original.getTripId(),
                        original.getRiderId(), original.getAmount());
                break;
            default:
                throw new IllegalArgumentException("Unsupported payment method: " + newMethod);
        }

        repository.save(retryPayment);
        return retryPayment;
    }

    // =====================================================================
    //  Cancellation fee
    // =====================================================================

    /**
     * Charges a ₹30 cancellation fee to the rider.
     * The fee is stored as a PAID record immediately (deducted from wallet if available,
     * else recorded as cash liability).
     *
     * @param tripId  the cancelled trip ID
     * @param riderId the rider who cancelled
     * @return the Payment record for the cancellation fee
     */
    public Payment chargeCancellationFee(String tripId, String riderId) {
        double fee = RevenueService.CANCELLATION_FEE;
        System.out.printf("%n[CANCEL] Cancellation fee of Rs. %.2f charged to rider %s.%n", fee, riderId);

        String paymentId = generatePaymentId();
        PaymentStatus status;

        Wallet wallet = wallets.get(riderId);
        if (wallet != null && wallet.deduct(fee)) {
            System.out.printf("[CANCEL] Rs. %.2f deducted from wallet. Balance: Rs. %.2f%n",
                    fee, wallet.getBalance());
            status = PaymentStatus.PAID;
        } else {
            System.out.println("[CANCEL] Wallet unavailable or insufficient. Fee recorded as PENDING (cash).");
            status = PaymentStatus.PENDING;
        }

        Payment cancelPayment = new Payment(paymentId, tripId, riderId, fee,
                PaymentMethod.CASH, status);
        repository.save(cancelPayment);
        return cancelPayment;
    }

    // =====================================================================
    //  Helpers
    // =====================================================================

    private String generatePaymentId() {
        return String.format("PAY%04d", paymentCounter++);
    }

    public PaymentRepository getRepository() {
        return repository;
    }
}
