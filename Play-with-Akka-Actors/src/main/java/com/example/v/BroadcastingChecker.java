package com.example.v;


import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Routers;

public class BroadcastingChecker {

    public static Behavior<HighWayPatrol.Command> speedBehavior() {
        return Behaviors.setup(context -> {
            return Behaviors.receive(HighWayPatrol.Command.class)
                    .onMessage(HighWayPatrol.Violation.class, message -> {
                        context.getLog().info("Violation: "+message.getLicensePlate());
                        return Behaviors.same();
                    })
                    .onMessage(HighWayPatrol.WithinLimit.class, message -> {
                        context.getLog().info("WithinLimit: "+message.getLicensePlate());
                        return Behaviors.same();
                    })
                    .build();
        });
    }

    public static void main(String[] args) {


        Behavior<HighWayPatrol.Command> routingBehavior =
                Routers.pool(4, speedBehavior())
         .withBroadcastPredicate(msg -> msg instanceof HighWayPatrol.Violation);

        ActorRef<HighWayPatrol.Command> router = ActorSystem.create(routingBehavior, "pool-router");

        for (int i = 0; i < 1; i++) {
            router.tell(new HighWayPatrol.Violation("1179",100, 120));
        }



    }
}
