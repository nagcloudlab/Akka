package com.example.v;


import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Routers;


public class Manager {

    public static Behavior<String> greetBehavior() {
        return Behaviors.setup(context -> {
            return Behaviors.receive(String.class)
                    .onMessage(String.class, message -> {
                        context.getLog().info(message);
                        return Behaviors.same();
                    })
                    .build();
        });
    }

    public static void main(String[] args) {

        Behavior<String> routingBehavior =
                Routers.pool(4, greetBehavior());
        // .withRandomRouting();
        // .withBroadcastPredicate(msg -> msg.equals("0"));
        ActorRef<String> router = ActorSystem.create(routingBehavior, "pool-router");
        for (int i = 0; i < 4; i++) {
            router.tell("" + i);
        }

    }
}
