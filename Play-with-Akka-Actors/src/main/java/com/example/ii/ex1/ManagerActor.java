package com.example.ii.ex1;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ManagerActor extends AbstractBehavior<ManagerActor.Command> {


    interface Command { }
    @AllArgsConstructor
    @Getter
    static final class Delegate implements Command{
        final List<String> texts;
    }
    @AllArgsConstructor
    @Getter
    static final class WorkerDoneAdapter implements Command {
        final WorkerActor.Response response;
    }


    static ActorRef<WorkerActor.Response> adapter;

    public static Behavior<Command> create() {
        // Behaviors can be Nested
        return Behaviors.setup(context->{
            adapter=context.messageAdapter(WorkerActor.Response.class,response->{
                return new WorkerDoneAdapter(response);
            });
            return new ManagerActor(context);
        });
    }

    private ManagerActor(akka.actor.typed.javadsl.ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Delegate.class, this::onDelegate)
                .onMessage(WorkerDoneAdapter.class, this::onWorkerDoneAdapter)
                .build();
    }

    private  Behavior<Command> onWorkerDoneAdapter(WorkerDoneAdapter m) {
        if(m.getResponse() instanceof WorkerActor.Done){
            WorkerActor.Done done=(WorkerActor.Done)m.getResponse();
            getContext().getLog().info(done.getText());
        }
        return Behaviors.same();
    }

    private  Behavior<Command> onDelegate(Delegate m) {
        m.getTexts().forEach(text->{
            ActorRef<WorkerActor.Command> worker=getContext().spawn(WorkerActor.create(),"worker-"+text);
            worker.tell(new WorkerActor.Parse(text,adapter));
        });
        return Behaviors.same();
    }



}
