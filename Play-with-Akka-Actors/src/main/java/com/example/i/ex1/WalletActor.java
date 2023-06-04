package com.example.i.ex1;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


public class WalletActor extends AbstractBehavior<Integer> {


    public WalletActor(ActorContext<Integer> context) {
        super(context);
    }

    public static Behavior<Integer> create() {
        return Behaviors.setup(context -> new WalletActor(context));
    }

    public Receive<Integer> createReceive() {
        return newReceiveBuilder()
                .onMessage(Integer.class, this::onMessage).build();
    }

    private Behavior<Integer> onMessage(Integer m) {
        getContext().getLog().info("Received message: {}", m);
        return Behaviors.same();
    }


}
