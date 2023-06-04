package com.example.ii.ex1;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class GuardianActor extends AbstractBehavior<GuardianActor.Command> {


    interface Command { }
    @AllArgsConstructor
    @Getter
    final static class Start implements Command{
        final List<String> texts;
    }


    public GuardianActor(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(GuardianActor::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Start.class, this::onStart)
                .build();
    }

    private  Behavior<Command> onStart(Start m) {
        ActorRef<ManagerActor.Command> manager=getContext().spawn(ManagerActor.create(),"manager");
        manager.tell(new ManagerActor.Delegate(m.getTexts()));
        return Behaviors.same();
    }


}
