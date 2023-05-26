package com.example.msg;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class OnlinePaymentRequest implements PaymentCommand{
    private int userId;
    private double amount;
}
