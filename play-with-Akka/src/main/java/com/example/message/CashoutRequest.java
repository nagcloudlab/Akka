package com.example.message;

public class CashoutRequest {

    private int customerId;
    private double amount;

    public CashoutRequest(int userId, double amount) {
        this.customerId = userId;
        this.amount = amount;
    }

    public int getCustomerId() {
        return customerId;
    }

    public double getAmount() {
        return amount;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
