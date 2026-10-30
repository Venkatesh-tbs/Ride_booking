package com.ridebooking.module4;

/**
 * Represents a rider's in-app wallet with a balance.
 * Supports deposit and deduction operations with balance validation.
 */
public class Wallet {

    private final String riderId;
    private double balance;

    public Wallet(String riderId, double initialBalance) {
        if (riderId == null || riderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Rider ID must not be empty.");
        }
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial wallet balance cannot be negative.");
        }
        this.riderId = riderId;
        this.balance = initialBalance;
    }

    // ---------- Getters ----------

    public String getRiderId() { return riderId; }
    public double getBalance() { return balance; }

    // ---------- Operations ----------

    /**
     * Deposits money into the wallet.
     *
     * @param amount amount to deposit (must be > 0)
     */
    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        balance = Math.round((balance + amount) * 100.0) / 100.0;
        System.out.printf("  Wallet credited Rs. %.2f | New balance: Rs. %.2f%n", amount, balance);
    }

    /**
     * Attempts to deduct the specified amount from the wallet.
     *
     * @param amount amount to deduct
     * @return true if the deduction succeeded (sufficient balance), false otherwise
     */
    public boolean deduct(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deduction amount must be positive.");
        }
        if (balance >= amount) {
            balance = Math.round((balance - amount) * 100.0) / 100.0;
            return true;
        }
        return false;
    }

    /**
     * Returns true if the wallet has enough balance for the given amount.
     */
    public boolean hasSufficientBalance(double amount) {
        return balance >= amount;
    }

    @Override
    public String toString() {
        return String.format("Wallet[riderId=%s, balance=Rs. %.2f]", riderId, balance);
    }
}
