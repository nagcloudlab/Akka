package com.example.actor;


import akka.actor.AbstractActor;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.example.msg.PrintMsg;

// Step 1: extend AbstractBehavior<T>  (akka.actor.typed.javadsl.AbstractBehavior)
public class PrintActor extends AbstractBehavior<PrintMsg> {

    //Step 4:  Create Behaviour  (Props replacement)
    public static Behavior<PrintMsg> create() {
        return Behaviors.setup(PrintActor::new);
    }

    //Step 3: Create  matching constructor
    public PrintActor(ActorContext<PrintMsg> context) {
        super(context);
    }

    // Step 2:  override createReceive method
    @Override
    public Receive<PrintMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(PrintMsg.class, this::printMsg)
                .build();
    }

    private Behavior<PrintMsg> printMsg(PrintMsg msg) {
        System.out.println("Message to print :" + msg);
        return this;
    }

}
