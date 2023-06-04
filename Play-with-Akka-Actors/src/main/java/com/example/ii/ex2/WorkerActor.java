package com.example.ii.ex2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Random;

public class WorkerActor extends AbstractBehavior<WorkerActor.Command> {


    interface Response { }
    @AllArgsConstructor
    @Getter
    final static class Done implements Response{
        final String text;
    }

    interface Command { }
    @AllArgsConstructor
    @Getter
    static final class Parse implements Command{
        final String text;
        final ActorRef<Response> replyTo;
    }



    public static Behavior<Command> create() {
        return Behaviors.setup(WorkerActor::new);
    }

    private WorkerActor(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Parse.class, this::onParse)
                .build();
    }

    private  Behavior<Command> onParse(Parse m) {
        fakeLengthyParsing(m.text);
        m.replyTo.tell(new Done(m.text.replaceAll("-","")));
        return Behaviors.same();
    }

    private void fakeLengthyParsing(String text){
        long endTime =
                System.currentTimeMillis() + new Random().nextInt(5000);
        while (endTime > System.currentTimeMillis()) {}
    }


}
