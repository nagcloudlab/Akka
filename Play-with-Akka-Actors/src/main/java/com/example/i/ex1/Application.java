package com.example.i.ex1;

import akka.actor.typed.ActorSystem;

public class Application {
    public static void main(String[] args) {


        ActorSystem<Integer> actorSystem = ActorSystem.create(WalletActor.create(), "walletActor");
        actorSystem.tell(10);



        //actorSystem.terminate();


    }
}
