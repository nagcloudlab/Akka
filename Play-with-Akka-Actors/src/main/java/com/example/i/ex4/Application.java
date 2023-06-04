package com.example.i.ex4;

import akka.actor.typed.ActorSystem;

import java.util.concurrent.TimeUnit;

public class Application {
    public static void main(String[] args)throws InterruptedException {


        ActorSystem<WalletActor.Command> actorSystem = ActorSystem.create(WalletActor.create(10), "wallet");
        actorSystem.tell(new WalletActor.Deposit(10));
        actorSystem.tell(new WalletActor.Deposit(10));
        actorSystem.tell(new WalletActor.Deactivate());
        TimeUnit.SECONDS.sleep(4);
        actorSystem.tell(new WalletActor.Deposit(10));


        //actorSystem.terminate();


    }
}
