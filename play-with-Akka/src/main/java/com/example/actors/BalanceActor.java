package com.example.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.example.db.FakeDB;
import com.example.messages.BalanceRequestMsg;

public class BalanceActor extends AbstractActor {

    public static Props props() {
        return Props.create(BalanceActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BalanceRequestMsg.class, this::balanceEnquiry)
                .matchAny(o -> System.out.println("Unknown message received"))
                .build();
    }

    private void balanceEnquiry(BalanceRequestMsg msg) throws InterruptedException {
        int customerId = msg.getCustomerId();
        Thread.sleep(6*1000);
        double balance = FakeDB.balanceSheet.get(customerId);
        System.out.println(msg.getUsername());
        System.out.println(balance);
    }

}
