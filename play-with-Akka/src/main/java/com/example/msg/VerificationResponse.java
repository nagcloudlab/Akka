package com.example.msg;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerificationResponse implements PaymentCommand{

    boolean verified;
    String text;
    int userId;
    double amount;

}
