package com.example.msg;


import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerificationQuery {
    private ActorRef<PaymentCommand> senderActor;
    private int userId;
    private double amount;


    public String consistentHashKey() {
        if (userId <= 1000) return "1";
        if (userId <= 10000) return "2";
        if (userId <= 100000) return "3";
        if (userId <= 1000000) return "4";
        if (userId <= 10000000) return "5";
        if (userId <= 100000000) return "6";
        return "";
    }

}
