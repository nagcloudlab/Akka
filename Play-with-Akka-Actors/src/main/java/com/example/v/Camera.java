package com.example.v;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.GroupRouter;
import akka.actor.typed.javadsl.Routers;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Camera {

    @AllArgsConstructor
    @Getter
    final static class Photo {
        final String content;
    }

    public static Behavior<Photo> create() {
        GroupRouter<String> routingBehavior = Routers.group(PhotoProcessor.key)
                .withRoundRobinRouting();
        ActorRef<String> router = ActorSystem.create(routingBehavior, "group-router");

        return Behaviors.receive(Camera.Photo.class)
                .onMessage(Camera.Photo.class, message -> {
                    router.tell(message.getContent());
                    return Behaviors.same();
                })
                .build();
    }

}
