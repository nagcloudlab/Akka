package com.example;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.example.actor.BalanceActor;
import com.example.actor.CashActor;
import com.example.actor.PrintActor;
import com.example.message.BalanceRequest;
import com.example.message.CashoutRequest;
import com.example.repository.FakeDB;

public class Application {
    public static void main(String[] args) {


        ActorSystem system = ActorSystem.create("system-1");
        ActorRef printActor1 = system.actorOf(PrintActor.props(), "print-actor-1");
        ActorRef printActor2 = system.actorOf(PrintActor.props(), "print-actor-2");

//        printActor1.tell("Hello",ActorRef.noSender());
//        printActor2.tell("World",ActorRef.noSender());

        FakeDB.balanceSheet.put(1, 100.0);
        FakeDB.balanceSheet.put(2, 150.0);
        FakeDB.balanceSheet.put(3, 200.0);

        // Lets create the Actors
        ActorRef balanceActor = system.actorOf(BalanceActor.props());
        ActorRef cashActor = system.actorOf(CashActor.props());

        // Lets assume a balance enquiry come through
        BalanceRequest balanceRequestMessage = new BalanceRequest("Foo", 1);
        balanceActor.tell(balanceRequestMessage, ActorRef.noSender());

//        // Lets assume a cash out request came now
//        CashoutRequest cashoutRequestMessage = new CashoutRequest(1,10.50);
//        cashActor.tell(cashoutRequestMessage, ActorRef.noSender());
//
//        // Lets assume a balance enquiry come through
//        balanceActor.tell(balanceRequestMessage, ActorRef.noSender());


    }
}
