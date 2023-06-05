package com.example.iii;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Application1 {
    public static void main(String[] args) {
        ActorRef<Protocol.Command> actorRef = ActorSystem.create(Boss.create(), "boss");
        actorRef.tell(new Protocol.Hello("1"));
        actorRef.tell(new Protocol.Hello("2"));
        actorRef.tell(new Protocol.Fail("fail-1"));
    }


    public interface Protocol {
        public interface Command {
        }
        public static class Fail implements Command {
            public final String text;
            public Fail(String text) {
                this.text = text;
            }
        }
        public static class Hello implements Command {
            public final String text;
            public Hello(String text) {
                this.text = text;
            }
        }
    }


    public static class Worker extends AbstractBehavior<Protocol.Command> {

        public static Behavior<Protocol.Command> create() {

            return Behaviors.setup(Worker::new);
//            return Behaviors.supervise(Behaviors.setup(Worker::new))
//                    .onFailure(SupervisorStrategy.resume());
//            return Behaviors.supervise(Behaviors.setup(Worker::new))
//                    .onFailure(RuntimeException.class,SupervisorStrategy.restart());
//            return Behaviors.supervise(Behaviors.setup(Worker::new))
//                    .onFailure(RuntimeException.class,SupervisorStrategy.restart().withLimit(1,java.time.Duration.ofSeconds(5)));
//            return  Behaviors.supervise(Behaviors.setup(Worker::new))
//                    .onFailure(RuntimeException.class,SupervisorStrategy.stop());

//            return Behaviors.supervise(
//                    Behaviors.supervise(Behaviors.setup(Worker::new))
//                            .onFailure(IllegalStateException.class, SupervisorStrategy.restart())
//            ).onFailure(RuntimeException.class, SupervisorStrategy.stop());

        }

        private Worker(ActorContext<Protocol.Command> context) {
            super(context);
            context.getLog().info("Worker starting up");
        }

        @Override
        public Receive<Protocol.Command> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Protocol.Fail.class, this::onFail)
                    .onMessage(Protocol.Hello.class, this::onHello)
                    .build();
        }

        private Behavior<Protocol.Command> onFail(Protocol.Fail message) {
            throw new RuntimeException(message.text);
        }

        private Behavior<Protocol.Command> onHello(Protocol.Hello message) {
            getContext().getLog().info("Hello {}", message.text);
            return this;
        }
    }

    public static class MiddleManagement extends AbstractBehavior<Protocol.Command> {

        public static Behavior<Protocol.Command> create() {
           return Behaviors.setup(MiddleManagement::new);
//            return Behaviors.supervise(Behaviors.setup(MiddleManagement::new))
//                    .onFailure(SupervisorStrategy.restart().withStopChildren(true));
        }

        private final ActorRef<Protocol.Command> worker;

        private MiddleManagement(ActorContext<Protocol.Command> context) {
            super(context);

            context.getLog().info("Middle management starting up");
            // default supervision of child, meaning that it will stop on failure
            worker = context.spawn(Worker.create(), "worker");

            // we want to know when the child terminates, but since we do not handle
            // the Terminated signal, we will in turn fail on child termination
             context.watch(worker);
        }

        @Override
        public Receive<Protocol.Command> createReceive() {
            // here we don't handle Terminated at all which means that
            // when the child fails or stops gracefully this actor will
            // fail with a DeathPactException
            return newReceiveBuilder()
                    .onMessage(Protocol.Command.class, this::onCommand)
                    .onSignal(ChildFailed.class, signal -> {
                        getContext().getLog().info("ChildFailed: {} {}", signal.getRef(), signal.getCause());
                        return Behaviors.same();
                    })
                    .onSignal(Terminated.class, signal -> {
                        getContext().getLog().info("Child Terminated: {}", signal.getRef());
                        return Behaviors.same();
                    })
                    .build();
        }

        private Behavior<Protocol.Command> onCommand(Protocol.Command message) {
            // just pass messages on to the child
//            if(message instanceof Protocol.Fail){
//                throw new RuntimeException(((Protocol.Fail) message).text);
//            }
            worker.tell(message);
            return this;
        }

    }

    public static class Boss extends AbstractBehavior<Protocol.Command> {

        public static Behavior<Protocol.Command> create() {
            return Behaviors.setup(Boss::new);
//            return Behaviors.supervise(Behaviors.setup(Boss::new))
//                    .onFailure(DeathPactException.class, SupervisorStrategy.restart());
        }

        private final ActorRef<Protocol.Command> middleManagement;

        private Boss(ActorContext<Protocol.Command> context) {
            super(context);
            context.getLog().info("Boss starting up");
            // default supervision of child, meaning that it will stop on failure
            middleManagement = context.spawn(MiddleManagement.create(), "middle-management");
//            context.watch(middleManagement);
        }

        @Override
        public Receive<Protocol.Command> createReceive() {
            // here we don't handle Terminated at all which means that
            // when middle management fails with a DeathPactException
            // this actor will also fail
            return newReceiveBuilder().onMessage(Protocol.Command.class, this::onCommand).build();
        }

        private Behavior<Protocol.Command> onCommand(Protocol.Command message) {
            // just pass messages on to the child
            middleManagement.tell(message);
            return this;
        }
    }

}
