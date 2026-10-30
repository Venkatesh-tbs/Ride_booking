package com.ridebooking.module2;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RideOfferService {

    private static final Duration OFFER_TIMEOUT =
            Duration.ofSeconds(30);

    private static final Duration REQUEST_TIMEOUT =
            Duration.ofMinutes(2);

    private final RideMatchingService matchingService;

    private final Map<String, OfferSession> sessions =
            new HashMap<>();

    public RideOfferService(RideMatchingService matchingService) {
        if (matchingService == null) {
            throw new IllegalArgumentException(
                    "Matching service is required.");
        }

        this.matchingService = matchingService;
    }

    public DriverInfo startRequest(
            RideRequest request,
            List<? extends DriverInfo> drivers) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request cannot be null.");
        }

        if (sessions.containsKey(request.getRequestId())) {
            throw new IllegalArgumentException(
                    "This request has already been started.");
        }

        if (request.getStatus() != RideRequestStatus.REQUESTED) {
            throw new IllegalStateException(
                    "Request is not in REQUESTED status.");
        }

        List<DriverInfo> candidates =
                matchingService.findNearestDrivers(request, drivers);

        OfferSession session = new OfferSession(request, candidates);

        sessions.put(request.getRequestId(), session);

        return offerNextDriver(session, Instant.now());
    }

    public DriverInfo getCurrentOffer(String requestId) {
        OfferSession session = sessions.get(requestId);

        if (session == null) {
            return null;
        }

        processSession(session, Instant.now());

        return session.currentDriver;
    }

    public boolean acceptOffer(String requestId, String driverId) {
        OfferSession session = sessions.get(requestId);

        if (session == null) {
            return false;
        }

        processSession(session, Instant.now());

        if (session.currentDriver == null) {
            return false;
        }

        if (!session.currentDriver.getDriverId().equals(driverId)) {
            return false;
        }

        if (!session.currentDriver.isAvailable()) {
            offerNextDriver(session, Instant.now());
            return false;
        }

        session.currentDriver.markBusy();

        session.request.setStatus(RideRequestStatus.ACCEPTED);
        session.currentDriver = null;

        return true;
    }

    public DriverInfo rejectOffer(String requestId, String driverId) {
        OfferSession session = sessions.get(requestId);

        if (session == null) {
            return null;
        }

        processSession(session, Instant.now());

        if (session.currentDriver == null) {
            return null;
        }

        if (!session.currentDriver.getDriverId().equals(driverId)) {
            return null;
        }

        session.currentDriver = null;

        return offerNextDriver(session, Instant.now());
    }

    public void processTimeouts() {
        Instant now = Instant.now();

        for (OfferSession session : sessions.values()) {
            processSession(session, now);
        }
    }

    private void processSession(
            OfferSession session, Instant now) {

        RideRequest request = session.request;

        if (request.getStatus() == RideRequestStatus.ACCEPTED
                || request.getStatus() == RideRequestStatus.EXPIRED
                || request.getStatus() == RideRequestStatus.CANCELLED) {
            return;
        }

        Duration requestAge = Duration.between(
                request.getRequestedAt(), now);

        if (requestAge.compareTo(REQUEST_TIMEOUT) >= 0) {
            request.setStatus(RideRequestStatus.EXPIRED);
            session.currentDriver = null;
            return;
        }

        if (session.currentDriver != null
                && Duration.between(session.offerStartedAt, now)
                        .compareTo(OFFER_TIMEOUT) >= 0) {

            session.currentDriver = null;
            offerNextDriver(session, now);
        }
    }

    private DriverInfo offerNextDriver(
            OfferSession session, Instant now) {

        if (Duration.between(
                session.request.getRequestedAt(), now)
                .compareTo(REQUEST_TIMEOUT) >= 0) {

            session.request.setStatus(RideRequestStatus.EXPIRED);
            session.currentDriver = null;
            return null;
        }

        if (session.nextIndex >= session.candidates.size()) {
            session.currentDriver = null;
            return null;
        }

        session.currentDriver =
                session.candidates.get(session.nextIndex);

        session.nextIndex++;
        session.offerStartedAt = now;

        session.request.setStatus(RideRequestStatus.OFFERED);

        return session.currentDriver;
    }

    private static class OfferSession {

        private final RideRequest request;
        private final List<DriverInfo> candidates;

        private int nextIndex;
        private Instant offerStartedAt;
        private DriverInfo currentDriver;

        private OfferSession(
                RideRequest request,
                List<DriverInfo> candidates) {

            this.request = request;
            this.candidates = candidates;
            this.nextIndex = 0;
        }
    }
}
