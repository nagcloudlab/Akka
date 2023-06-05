package com.example.v;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;

public class ActorRouterExample {

    static class Worker {
        interface Command {
        }
        static class DoLog implements Command {
            public final String text;

            public DoLog(String text) {
                this.text = text;
            }
        }
        static final Behavior<Command> create() {
            return Behaviors.setup(
                    context -> {
                        context.getLog().info("Starting worker");

                        return Behaviors.receive(Command.class)
                                .onMessage(DoLog.class, doLog -> onDoLog(context, doLog))
                                .build();
                    });
        }
        private static Behavior<Command> onDoLog(ActorContext<Command> context, DoLog doLog) {
            context.getLog().info("Got message {}", doLog.text);
            return Behaviors.same();
        }
    }
    static class PoolRouter_Test {
        static Behavior<Void> create() {
            return Behaviors.setup(context -> {
                PoolRouter<Worker.Command> poolRouter=
                        Routers.pool(4,
                                Behaviors.supervise(Worker.create()).onFailure(SupervisorStrategy.restart()));
                ActorRef<Worker.Command> actorRef=context.spawn(poolRouter,"worker-pool");
                for (int i = 0; i < 8; i++) {
                    actorRef.tell(new Worker.DoLog("message " + i));
                }
                return Behaviors.empty();
            });

        }
    }

    static class GroupRouter_Test {

        static ServiceKey<Worker.Command> serviceKey = ServiceKey.create(Worker.Command.class, "log-worker");

        static Behavior<Void> create() {
            return Behaviors.setup(context -> {

                // this would likely happen elsewhere - if we create it locally we
                // can just as well use a pool

                ActorRef<Worker.Command> worker1 = context.spawn(Worker.create(), "worker-1");
                ActorRef<Worker.Command> worker2 = context.spawn(Worker.create(), "worker-2");

                context.getSystem().receptionist().tell(Receptionist.register(serviceKey, worker1));
                context.getSystem().receptionist().tell(Receptionist.register(serviceKey, worker2));

                GroupRouter<Worker.Command> group = Routers.group(serviceKey);
                ActorRef<Worker.Command> router = context.spawn(group, "worker-group");

                // the group router will stash messages until it sees the first listing of
                // registered
                // services from the receptionist, so it is safe to send messages right away
                for (int i = 0; i < 4; i++) {
                    router.tell(new Worker.DoLog("msg " + i));
                }

                return Behaviors.empty();

            });
        }

    }

    public static void main(String[] args) {
        //ActorRef<Void> actorRef=ActorSystem.create(PoolRouter_Test.create(), "pool");
        ActorRef<Void> actorRef=ActorSystem.create(GroupRouter_Test.create(), "group");
    }
}
