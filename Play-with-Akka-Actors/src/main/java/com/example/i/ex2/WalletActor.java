package com.example.i.ex2;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


public class WalletActor extends AbstractBehavior<Integer> {

    private int balance = 0; // managing state on variables

    public WalletActor(ActorContext<Integer> context,int initialBalance) {
        super(context);
        this.balance=initialBalance;
    }

    public static Behavior<Integer> create(int initialBalance) {
        return Behaviors.setup(context -> new WalletActor(context,initialBalance));
    }

    public Receive<Integer> createReceive() {
        return newReceiveBuilder()
                .onMessage(Integer.class, this::onMessage).build();
    }

    private Behavior<Integer> onMessage(Integer m) {
        if(m>0) {
            balance += m;
            getContext().getLog().info("Balance: {}", balance);
        }
        return Behaviors.same();
    }


}
