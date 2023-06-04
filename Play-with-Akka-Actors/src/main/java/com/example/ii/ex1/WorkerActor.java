package com.example.ii.ex1;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.AbstractBehavior;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class WorkerActor extends AbstractBehavior<WorkerActor.Command> {


    interface Command { }
    @AllArgsConstructor
    @Getter
    static final class Parse implements Command{
        final String text;
        final ActorRef<Response> replyTo;
    }
    interface Response { }
    @AllArgsConstructor
    @Getter
    final static class Done implements Response{
        final String text;
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
        m.replyTo.tell(new Done(m.text.replaceAll("-","")));
        //return Behaviors.same();
        return Behaviors.stopped();
    }


}
