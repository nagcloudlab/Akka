package com.example.i.ex3;

import akka.actor.typed.ActorSystem;

public class Application {
    public static void main(String[] args) {


        ActorSystem<WalletActor.Command> actorSystem = ActorSystem.create(WalletActor.create(10), "wallet");
        actorSystem.tell(new WalletActor.Deposit(10));
        actorSystem.tell(new WalletActor.Deposit(10));
        actorSystem.tell(new WalletActor.Deactivate());
        actorSystem.tell(new WalletActor.Deposit(10));


        //actorSystem.terminate();


    }
}
