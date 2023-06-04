package com.example.ii.ex2;

import akka.actor.typed.ActorSystem;

import java.util.List;

public class Application {
    public static void main(String[] args) {

        ActorSystem guardianActorRef = ActorSystem.create(GuardianActor.create(), "guardianActor");
        guardianActorRef.tell(new GuardianActor.Start(List.of("a-b", "b-c", "c-d")));

    }
}
