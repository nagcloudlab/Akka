package com.example.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.example.db.FakeDB;
import com.example.msg.VerificationQuery;
import com.example.msg.VerificationResponse;

public class VerificationActor extends AbstractBehavior<VerificationQuery> {


    // Constructor
    public VerificationActor(ActorContext<VerificationQuery> context) {
        super(context);
    }
    // Behavior
    public static Behavior<VerificationQuery> create(){
        return Behaviors.setup(VerificationActor::new);
    }
    @Override
    public Receive<VerificationQuery> createReceive() {
        return newReceiveBuilder()
                .onMessage(VerificationQuery.class,this::verify)
                .build();
    }

    private Behavior<VerificationQuery> verify(VerificationQuery verificationQuery){
        double balance= FakeDB.balanceSheet.get(verificationQuery.getUserId());
        if(balance>=verificationQuery.getAmount()){
            double newBalance=balance-verificationQuery.getAmount();
            FakeDB.balanceSheet.replace(verificationQuery.getUserId(),newBalance);
            verificationQuery.getSenderActor().tell(new VerificationResponse(true,"verified",verificationQuery.getUserId(),verificationQuery.getAmount()));
        }else{
            verificationQuery.getSenderActor().tell(new VerificationResponse(false,"insufficient balance",verificationQuery.getUserId(),verificationQuery.getAmount()));
        }
        return this;
    }

}
