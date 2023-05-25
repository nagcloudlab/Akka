package com.example.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PaymentResponseMsg {
    boolean result;
    String text;

}
