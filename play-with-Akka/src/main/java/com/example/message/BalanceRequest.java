package com.example.message;

public class BalanceRequest {

    private int customerId;
    private String userName;

    public BalanceRequest(String userName, int customerId) {
        this.userName = userName;
        this.customerId = customerId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getUserName() {
        return userName;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
