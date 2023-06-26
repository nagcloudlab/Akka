package org.example;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.persistence.typed.*;

import java.util.concurrent.*;

public class Application {


    public static Behavior<Void> guardianBehavior() {
        return Behaviors.setup(context -> {

            ActorRef<MyPersistentBehavior.Command> actorRef = context.spawn(MyPersistentBehavior.create(PersistenceId.ofUniqueId("MyPersistentBehavior")), "guardian");

//            for (int i = 0; i < 10; i++) {
//                actorRef.tell(new MyPersistentBehavior.Add("data-" + i));
//            }

//            actorRef.tell(MyPersistentBehavior.Display.INSTANCE);


            return Behaviors.empty();
        });
    }

    public static void main(String[] args) {

        ActorSystem<Void> actorSystem = ActorSystem.create(guardianBehavior(), "guardian");

    }
}
