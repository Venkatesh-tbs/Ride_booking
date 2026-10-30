
package com.ridebooking.module3;

import java.time.LocalDateTime;

public class Trip {

    public enum Status {
        REQUESTED,
        ACCEPTED,
        DRIVER_ARRIVED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    private int tripId;
    private int riderId;
    private int driverId;
    private String otp;
    private Status status;
    private LocalDateTime acceptedAt;

    public Trip(int tripId, int riderId, int driverId, String otp) {
        this.tripId = tripId;
        this.riderId = riderId;
        this.driverId = driverId;
        this.otp = otp;
        this.status = Status.REQUESTED;
        this.acceptedAt = null;
    }

    public int getTripId() {
        return tripId;
    }

    public int getRiderId() {
        return riderId;
    }

    public int getDriverId() {
        return driverId;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public boolean verifyOTP(String enteredOTP) {
        return otp != null && otp.equals(enteredOTP);
    }
}
