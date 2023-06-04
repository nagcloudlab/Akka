package com.example.i.ex3;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

// Actor Message
// 2 types lof messages
// 1. command  ==> trigger some action in future
// 2. event    ==> something happened in the past

public class WalletActor extends AbstractBehavior<WalletActor.Command> {



    static  interface Command {}
    static final class Deposit implements Command {
        public final int amount;
        public Deposit(int amount) {
            this.amount = amount;
        }
        public int getAmount() {
            return amount;
        }
    }
    static class Activate implements Command {}
    static class Deactivate implements Command {}



    public WalletActor(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create(int initialBalance) {
        return Behaviors.setup(context -> new WalletActor(context).activate(initialBalance));
    }


    // Managing state on Behaviors
    public Behavior<Command> activate(int balance) {
        return newReceiveBuilder()
                .onMessage(Activate.class, m->Behaviors.same())
                .onMessage(Deactivate.class, m->deactivate(balance))
                .onMessage(Deposit.class, m->onDepositActivateState(m,balance))
                .build();
    }

    public Behavior<Command> deactivate(int balance) {
        return newReceiveBuilder()
                .onMessage(Activate.class, m->this.activate(balance))
                .onMessage(Deactivate.class, m->Behaviors.same())
                .onMessage(Deposit.class, m->onDepositDeactivateState(m))
                .build();
    }

    @Override
    public Receive<Command> createReceive() {
        return (Receive<Command>) this.activate(0);
    }


    private Behavior<Command> onDepositActivateState(Deposit deposit,int balance) {
        if(deposit.getAmount()>0) {
            balance = balance+deposit.getAmount();
            getContext().getLog().info("Balance: {}", balance);
        }
        return this.activate(balance);
    }

    private Behavior<Command> onDepositDeactivateState(Deposit deposit) {
        getContext().getLog().info("Wallet is deactivated");
        return Behaviors.same();
    }





}
