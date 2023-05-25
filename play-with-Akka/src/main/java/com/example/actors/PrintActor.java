package com.example.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class PrintActor extends AbstractActor {

    public static Props props() {
        return Props.create(PrintActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, System.out::println)
                .matchAny(o -> System.out.println("received unknown message"))
                .build();
    }
}
