package main.java.com.ridebooking.module2;

public interface DriverInfo {

    String getDriverId();

    double getCurrentX();

    double getCurrentY();

    String getVehicleType();

    boolean isAvailable();

    void markBusy();
}
