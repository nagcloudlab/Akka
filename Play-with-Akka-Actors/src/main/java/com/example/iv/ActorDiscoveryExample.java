package com.example.iv;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;

public class ActorDiscoveryExample {

    public static void main(String[] args) {

        //ActorSystem.create(Guardian.create(), "guardian");//
        ActorSystem<PingManager.Command> pingManager=ActorSystem.create(PingManager.create(), "pingManager");
        pingManager.tell(PingManager.PingAll.INSTANCE);

    }
}
