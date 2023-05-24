package com.example.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.example.message.CashoutRequest;
import com.example.repository.FakeDB;

import java.util.concurrent.TimeUnit;

public class CashActor extends AbstractActor {

    public static Props props() {
        return Props.create(CashActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CashoutRequest.class,this::cashOut)
                .build();
    }

    private void cashOut(CashoutRequest message) throws InterruptedException {
        int customerId=message.getCustomerId();
        double balance= FakeDB.balanceSheet.get(customerId);
        //TimeUnit.SECONDS.sleep(1);
        if(balance<message.getAmount()){
            System.out.println("Customer Id:"+String.valueOf(message.getCustomerId()));
            System.out.println("insufficient balance");
        }else{
            double newBalance=balance-message.getAmount();
            FakeDB.balanceSheet.replace(customerId,newBalance);
            System.out.println("Money out :"+String.valueOf(message.getAmount()));
        }
    }
}
