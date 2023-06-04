package com.example.i.ex4;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


public class WalletActor extends AbstractBehavior<WalletActor.Command> {


    public WalletActor(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create(int initialBalance) {
        return Behaviors.setup(context -> new WalletActor(context).activate(initialBalance));
    }


    @Override
    public Receive<Command> createReceive() {
        return (Receive<Command>) this.activate(0);
    }

    public Behavior<Command> activate(int balance) {
        return Behaviors.receive(Command.class)
                .onMessage(Activate.class, m->{
                    getContext().getLog().info("Wallet is activated");
                    return Behaviors.same();
                })
                .onMessage(Deactivate.class, m-> Behaviors.withTimers(timers -> {
                    timers.startSingleTimer(new Activate(), java.time.Duration.ofSeconds(3));
                    return deactivate(balance);
               }))
                .onMessage(Deposit.class, m->onDepositActivateState(m,balance))
                .build();
    }

    public Behavior<Command> deactivate(int balance) {
        return Behaviors.receive(Command.class)
                .onMessage(Activate.class, m->{
                    getContext().getLog().info("Wallet is activated");
                    return activate(balance);
                })
                .onMessage(Deactivate.class, m->Behaviors.same())
                .onMessage(Deposit.class, this::onDepositDeactivateState)
                .build();
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



}
