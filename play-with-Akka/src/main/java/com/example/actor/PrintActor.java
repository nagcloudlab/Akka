package com.example.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class PrintActor extends AbstractActor {

    public static Props props(){
        return Props.create(PrintActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class,message->{
                    System.out.println("PrintActor - "+getSelf().path()+":"+message);
                })
                .matchAny(m->{
                    System.out.println("Unexpected Message Type");
                })
                .build();
    }
}
