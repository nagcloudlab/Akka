package com.example.ii.ex2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ManagerActor extends AbstractBehavior<ManagerActor.Command> {



    public static Behavior<Command> create() {
        return Behaviors.setup(context->{
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
                .onMessage(Report.class, this::onReport)
                .build();
    }

    private  Behavior<Command> onReport(Report m) {
        getContext().getLog().info("text parsing has been finished -"+m.getDescription());
        return Behaviors.same();
    }

    private  Behavior<Command> onDelegate(Delegate m) {
        m.getTexts().forEach(text->{
            ActorRef<WorkerActor.Command> worker=getContext().spawn(WorkerActor.create(),"worker-"+text);
            getContext().ask(WorkerActor.Response.class,worker, Duration.ofSeconds(1), me->new WorkerActor.Parse(text,me), (response,failure)->{
               if(response!=null){
                  return new Report("success => "+((WorkerActor.Done)response).getText());
               }
               return new Report("failed");
           });
        });
        return Behaviors.same();
    }


    interface Command { }
    @AllArgsConstructor
    @Getter
    static final class Delegate implements Command{
        final List<String> texts;
    }
    @AllArgsConstructor
    @Getter
    static final class Report implements Command{
        final String description;
    }

}
