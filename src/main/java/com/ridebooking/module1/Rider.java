
package com.ridebooking.module1;

public class Rider {

    private String name;
    private String phoneNumber;
    private String email;

    public Rider(String name, String phoneNumber, String email) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }

        this.name = name.trim();
        this.phoneNumber = phoneNumber.trim();
        this.email = email.trim();
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void displayRider() {
        System.out.println("Rider Name: " + name);
        System.out.println("Phone Number: " + phoneNumber);
        System.out.println("Email: " + email);
    }
}
