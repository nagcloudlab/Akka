package com.example.ix;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.MailboxSelector;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.config.ConfigFactory;

import java.util.concurrent.TimeUnit;

public class MailBoxExample {
    public static void main(String[] args) {

        var config= ConfigFactory.load();
        ActorSystem<String> actor = ActorSystem.create(doLog(), "MailBoxExample", config,MailboxSelector.bounded(100));
        for (int i = 0; i < 200; i++) {
            actor.tell("log-" + i);
        }

    }

    public static Behavior<String> doLog() {
        return Behaviors.receive(String.class)
                .onMessage(String.class, (message) -> {
                    System.out.println(message);
                    TimeUnit.SECONDS.sleep(1);
                    return Behaviors.same();
                }).build();
    }

}
