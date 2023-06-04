package com.example.i.ex2;

import akka.actor.typed.ActorSystem;

public class Application {
    public static void main(String[] args) {


        ActorSystem<Integer> actorSystem = ActorSystem.create(WalletActor.create(10), "wallet");
        actorSystem.tell(10);
        actorSystem.tell(10);


        //actorSystem.terminate();


    }
}
