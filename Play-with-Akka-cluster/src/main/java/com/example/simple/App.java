package com.example.simple;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import com.typesafe.config.*;

import java.util.*;

public class App {
    public static void main(String[] args) {
        if (args.length == 0) {
            startup(25251);
            startup(25252);
            startup(0);
        } else
            Arrays.stream(args).map(Integer::parseInt).forEach(App::startup);
    }

    private static Behavior<Void> rootBehavior() {
        return Behaviors.setup(context -> {

            // Create an actor that handles cluster domain events
            context.spawn(ClusterListener.create(), "ClusterListener");

            return Behaviors.empty();
        });
    }

    private static void startup(int port) {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.port", port);

        Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load());
        // Create an Akka system
        ActorSystem<Void> system = ActorSystem.create(rootBehavior(), "ClusterSystem", config);
    }
}
