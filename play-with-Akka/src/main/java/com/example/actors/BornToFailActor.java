package com.example.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class BornToFailActor extends AbstractActor {

    public static Props props() {
        return Props.create(BornToFailActor.class);
    }

    public BornToFailActor() {
        System.out.println("BornToFailActor created");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::doNotFail)
                .match(Integer.class, this::failNow)
                .build();
    }

    private void doNotFail(String msg) {
        System.out.println("I keep working,  your message : " + msg);
    }

    private void failNow(int x) throws Exception {
        System.out.println("Unexpected message, I will be failing in a second");
        throw new Exception("Unexpected message - integer value");
    }

}
