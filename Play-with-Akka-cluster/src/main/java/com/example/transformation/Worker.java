package com.example.transformation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import com.fasterxml.jackson.annotation.JsonCreator;

public class Worker {

    interface Command {
    }

    public static final class TransformText implements Command {
        public final String text;

        public TransformText(String text, ActorRef<TextTransformed> replyTo) {
            this.text = text;
            this.replyTo = replyTo;
        }

        public final ActorRef<TextTransformed> replyTo;
    }

    public static final class TextTransformed {
        public final String text;

        @JsonCreator
        public TextTransformed(String text) {
            this.text = text;
        }
    }

    public static ServiceKey<Worker.TransformText> WORKER_SERVICE_KEY = ServiceKey.create(TransformText.class, "Worker");

    public static Behavior<Command> create() {
        return Behaviors.setup(context -> {
            context.getLog().info("Registering myself with receptionist");
            context.getSystem().receptionist().tell(Receptionist.register(WORKER_SERVICE_KEY, context.getSelf().narrow()));

            return Behaviors.receive(Command.class)
                    .onMessage(TransformText.class, command -> {
                        command.replyTo.tell(new TextTransformed(command.text.toUpperCase()));
                        return Behaviors.same();
                    }).build();
        });
    }

}
