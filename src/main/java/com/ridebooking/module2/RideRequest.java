package com.ridebooking.module2;

import java.time.Instant;

public class RideRequest {

    private final String requestId;
    private final String riderId;

    private final double pickupX;
    private final double pickupY;

    private final double dropX;
    private final double dropY;

    private final String vehicleType;

    private final Instant requestedAt;

    private RideRequestStatus status;

    public RideRequest(
            String requestId,
            String riderId,
            double pickupX,
            double pickupY,
            double dropX,
            double dropY,
            String vehicleType) {

        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID is required.");
        }

        if (riderId == null || riderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Rider ID is required.");
        }

        if (!Double.isFinite(pickupX) || !Double.isFinite(pickupY)
                || !Double.isFinite(dropX) || !Double.isFinite(dropY)) {
            throw new IllegalArgumentException("Coordinates must be finite numbers.");
        }

        if (vehicleType == null || vehicleType.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle type is required.");
        }

        this.requestId = requestId.trim();
        this.riderId = riderId.trim();

        this.pickupX = pickupX;
        this.pickupY = pickupY;

        this.dropX = dropX;
        this.dropY = dropY;

        this.vehicleType = vehicleType.trim();

        this.requestedAt = Instant.now();
        this.status = RideRequestStatus.REQUESTED;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getRiderId() {
        return riderId;
    }

    public double getPickupX() {
        return pickupX;
    }

    public double getPickupY() {
        return pickupY;
    }

    public double getDropX() {
        return dropX;
    }

    public double getDropY() {
        return dropY;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public RideRequestStatus getStatus() {
        return status;
    }

    void setStatus(RideRequestStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }

        this.status = status;
    }
}
