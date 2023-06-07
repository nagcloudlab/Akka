package com.example.viii;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DispatcherExample {
    public static void main(String[] args) {

        ActorSystem<Object> system1 = ActorSystem.create(Boss.create(), "boss");
//        ActorSystem<Object> system2 = ActorSystem.create(Boss.create(), "boss");

    }
}


class Boss {
    public static Behavior<Object> create() {
        return Behaviors.setup(context -> {

//            context.spawn(Worker.create(), "Worker");
//            context.spawn(Worker.create(), "Worker", DispatcherSelector.defaultDispatcher());
//            context.spawn(Worker.create(), "Worker", DispatcherSelector.blocking());
//            context.spawn(Worker.create(), "Worker", DispatcherSelector.sameAsParent());
//            context.spawn(Worker.create(), "Worker", DispatcherSelector.fromConfig("my-dispatcher"));

            for (int i = 0; i < 100; i++) {
//                context.spawn(BlockingActor.create(), "BlockingActor-" + i,DispatcherSelector.fromConfig("my-dispatcher")).tell(i);
                context.spawn(AsyncActor.create(), "AsyncActor-" + i).tell(i);
                context.spawn(PrintActor.create(), "PrintActor-" + i,DispatcherSelector.defaultDispatcher()).tell(i);
            }

            return Behaviors.empty();
        });
    }
}


class Worker {
    public static Behavior<Object> create() {
        return Behaviors.setup(context -> {
            return Behaviors.empty();
        });
    }
}


class BlockingActor extends AbstractBehavior<Integer> {

    public static Behavior<Integer> create() {
        return Behaviors.setup(BlockingActor::new);
    }

    private BlockingActor(ActorContext<Integer> context) {
        super(context);
    }

    @Override
    public Receive<Integer> createReceive() {
        return newReceiveBuilder()
                .onMessage(
                        Integer.class,
                        i -> {
                            // DO NOT DO THIS HERE: this is an example of incorrect code,
                            // better alternatives are described further on.
                            // read / write with file system, database, networking call..
                            // block for 5 seconds, representing blocking I/O, etc
                            Thread.sleep(5000);

                            // Note : while processing in actor, prefer async/reactive style cosing...

                            // - CompletableFuture
                            // - CompletionStage
                            // - reactive Programming style
                            // - Akka Streams


                            getContext().getLog().info("Blocking operation finished: " + i);
                            return Behaviors.same();
                        })
                .build();
    }

}


class AsyncActor extends AbstractBehavior<Integer> {
    private final Executor ec;
    public static Behavior<Integer> create() {
        return Behaviors.setup(AsyncActor::new);
    }
    private AsyncActor(ActorContext<Integer> context) {
        super(context);
        this.ec = context.getSystem().dispatchers().lookup(DispatcherSelector.fromConfig("my-dispatcher"));
    }
    @Override
    public Receive<Integer> createReceive() {
        return newReceiveBuilder()
               .onMessage(
                        Integer.class,
                        i -> {
                            triggerFutureBlockingOperation(i, ec)
                                    .thenAccept(n->{
                                        //..
                                    });
                            return Behaviors.same();
                        })
               .build();
    }
    private static CompletableFuture<Integer> triggerFutureBlockingOperation(Integer i, Executor ec) {
        System.out.println("Calling blocking Future on separate dispatcher: " + i);
        CompletableFuture<Integer> f =
        CompletableFuture.supplyAsync(() -> {
            try{
                Thread.sleep(5000);
                System.out.println("Blocking future finished: " + i);
                return i;
            }catch (Exception e){
                return -1;
            }
        },ec);
        return f;
    }

}

class PrintActor extends AbstractBehavior<Integer> {
    public static Behavior<Integer> create() {
        return Behaviors.setup(PrintActor::new);
    }
    private PrintActor(ActorContext<Integer> context) {
        super(context);
    }
    @Override
    public Receive<Integer> createReceive() {
        return newReceiveBuilder()
              .onMessage(
                        Integer.class,
                        i -> {
                            getContext().getLog().info("Print operation finished: " + i);
                            return Behaviors.same();
                        })
              .build();
    }
}