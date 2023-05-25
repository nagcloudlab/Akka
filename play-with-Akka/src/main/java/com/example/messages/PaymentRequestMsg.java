package com.example.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PaymentRequestMsg {
    private int customerId;
    private double amount;
}
