package com.carthagegg.models;

import java.time.LocalDate;

public class Coupon {
    private String code;
    private double discountPercentage;
    private LocalDate expiryDate;

    public Coupon() {}

    public Coupon(String code, double discountPercentage, LocalDate expiryDate) {
        this.code = code;
        this.discountPercentage = discountPercentage;
        this.expiryDate = expiryDate;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public boolean isValid() {
        return expiryDate == null || !expiryDate.isBefore(LocalDate.now());
    }
}
