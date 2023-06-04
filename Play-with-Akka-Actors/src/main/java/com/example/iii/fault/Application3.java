package com.example.iii.fault;


import akka.actor.TypedActor;
import akka.actor.typed.*;
import akka.actor.typed.javadsl.Behaviors;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Application3 {


    interface Protocol {
        interface Command {
        }

        @AllArgsConstructor
        @Getter
        class Hello implements Command {
            private String text;
        }

        @AllArgsConstructor
        @Getter
        class Fail implements Command {
            private String text;
        }
    }


    static class Worker {
        public static Behavior<Protocol.Command> create() {

            Behavior<Protocol.Command> setupBehavior = Behaviors.setup(context -> {
                context.getLog().info("Worker started");
                return Behaviors.receive(Protocol.Command.class)
                        .onMessage(Protocol.Hello.class, (msg) -> {
                            context.getLog().info(msg.getText());
                            return Behaviors.same();
                        })
                        .onMessage(Protocol.Fail.class, (msg) -> {
                            throw new IllegalArgumentException("Fail");
                        })
                        .build();
            });
            return setupBehavior;

//            Behavior<Protocol.Command> superviseBehavior= Behaviors.supervise(setupBehavior)
//                    .onFailure(SupervisorStrategy.resume());

//            Behavior<Protocol.Command> superviseBehavior = Behaviors.supervise(setupBehavior)
//                    .onFailure(IllegalArgumentException.class, SupervisorStrategy.resume());
//
//            Behavior<Protocol.Command> superviseBehavior = Behaviors.supervise(
//                            Behaviors.supervise(setupBehavior)
//                                    .onFailure(IllegalArgumentException.class, SupervisorStrategy.resume()))
//                    .onFailure(RuntimeException.class, SupervisorStrategy.restart());


//            Behavior<Protocol.Command> superviseBehavior = Behaviors.supervise(
//                            Behaviors.supervise(setupBehavior)
//                                    .onFailure(IllegalArgumentException.class, SupervisorStrategy.resume()))
//                    .onFailure(RuntimeException.class, SupervisorStrategy.restart().withLimit(1, java.time.Duration.ofSeconds(5)));


//            Behavior<Protocol.Command> superviseBehavior = Behaviors.supervise(
//                            Behaviors.supervise(setupBehavior)
//                                    .onFailure(IllegalArgumentException.class, SupervisorStrategy.resume()))
//                    .onFailure(IllegalAccessException.class, SupervisorStrategy.stop());

//            return superviseBehavior;

        }
    }

    static class Manager {
        public static Behavior<Protocol.Command> create() {

            Behavior<Protocol.Command> setupBehavior=Behaviors.setup(context -> {
                context.getLog().info("Manager started");
                var worker = context.spawn(Worker.create(), "worker");

                context.watch(worker);

                return Behaviors.receive(Protocol.Command.class)
                        .onMessage(Protocol.Hello.class, (msg) -> {
                            worker.tell(msg);
                            return Behaviors.same();
                        })
                        .onMessage(Protocol.Fail.class, (msg) -> {
//                            if(msg.getText().equals("Fail-1"))
//                                throw new IllegalArgumentException("Fail-1");
                            worker.tell(msg);
                            return Behaviors.same();
                        })
//                        .onSignal(ChildFailed.class, (msg) -> {
//                            context.getLog().info("" + msg.getRef().path() + " Child Failed");
//                            return Behaviors.same();
//                        })
                        .onSignal(Terminated.class, (msg) -> {
                            context.getLog().info("" + msg.getRef().path() + " Terminated");
                            return Behaviors.same();
                        })
                        .onSignal(PreRestart.class, (msg) -> {
                            context.getLog().info(" PreRestart");
                            // clean access to external world
                            return Behaviors.same();
                        })
                        .onSignal(PostStop.class, (msg) -> {
                            context.getLog().info(" PostStop");
                            // clean access to external world
                            return Behaviors.same();
                        })
                        .build();
            });

//            return setupBehavior;
//
           return Behaviors.supervise(setupBehavior)
                    .onFailure(IllegalArgumentException.class, SupervisorStrategy.restart().withStopChildren(true));

        }
    }

    static class Boss {
        public static Behavior<Protocol.Command> create() {
            return Behaviors.setup(context -> {
                context.getLog().info("Boss started");
                var manager = context.spawn(Manager.create(), "manager");
                return Behaviors.receive(Protocol.Command.class)
                        .onMessage(Protocol.Hello.class, (msg) -> {
                            manager.tell(msg);
                            return Behaviors.same();
                        })
                        .onMessage(Protocol.Fail.class, (msg) -> {
                            manager.tell(msg);
                            return Behaviors.same();
                        })
                        .build();
            });
        }
    }


    public static void main(String[] args) {

        ActorRef<Protocol.Command> actorRef = ActorSystem.create(Boss.create(), "boss");
        actorRef.tell(new Protocol.Hello("Hello-1"));
        actorRef.tell(new Protocol.Fail("Fail-1"));


    }
}
