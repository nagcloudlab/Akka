package com.example.messages;

import lombok.*;

@AllArgsConstructor
@Data
public class BalanceRequestMsg {
    private int customerId;
    private String username;
}
