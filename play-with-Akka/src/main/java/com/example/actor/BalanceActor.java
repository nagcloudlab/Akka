package com.example.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.example.message.BalanceRequest;
import com.example.repository.FakeDB;

import java.util.concurrent.TimeUnit;

public class BalanceActor extends AbstractActor {

    public static Props props() {
        return Props.create(BalanceActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BalanceRequest.class,this::handleBalanceRequest)
                .build();
    }

    private void handleBalanceRequest(BalanceRequest message) throws InterruptedException {
        int customerId= message.getCustomerId();
        double balance= FakeDB.balanceSheet.get(customerId);
        TimeUnit.SECONDS.sleep(5);
        System.out.println(message.getUserName());
        System.out.println(balance);
    }

}
