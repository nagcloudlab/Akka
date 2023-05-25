package com.example.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.example.db.FakeDB;
import com.example.messages.PaymentRequestMsg;
import com.example.messages.PaymentResponseMsg;

public class PaymentActor extends AbstractActor {

    public static Props  props(){
        return Props.create(PaymentActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
              .match(PaymentRequestMsg.class, this::handlePayment)
              .build();
    }

    private void handlePayment(PaymentRequestMsg msg) {
        int customerId=msg.getCustomerId();
        double amount=msg.getAmount();
        double balance= FakeDB.balanceSheet.get(customerId);
        if(balance>=amount){
            double  newBalance=balance-amount;
            FakeDB.balanceSheet.replace(customerId,newBalance);
            getSender().tell(new PaymentResponseMsg(true,"Payment confirmed"),getSelf());
        }
        else{
            getSender().tell(new PaymentResponseMsg(false,"Insufficient balance"),getSelf());
        }
    }
}
