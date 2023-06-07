package com.example.vii;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class ConfigurationExample {
    public static void main(String[] args) {

//        var config=ConfigFactory.load(); // -Dconfig.file=application
//
//        System.out.println(config.getInt("MyApp.version"));
//        var databaseConfig=config.getConfig("MyApp.database");
//        System.out.println(databaseConfig.getString("driver"));
//        System.out.println(databaseConfig.getString("url"));
//        System.out.println(databaseConfig.getString("user"));


        //-----------------------------------------------------------------------


        var system = ActorSystem.create(Boss.create(), "MyActorSystem");
//        var config=system.settings().config();
//        System.out.println(config.getInt("MyApp.version"));
//        var databaseConfig = config.getConfig("MyApp.database");
//        System.out.println(databaseConfig.getString("driver"));
//        System.out.println(databaseConfig.getString("url"));
//        System.out.println(databaseConfig.getString("user"));

        //-----------------------------------------------------------------------


    }
}


class Boss {
    public static Behavior<Object> create() {
        return Behaviors.setup(context -> {
            context.spawn(Worker.create(), "Worker");
            return Behaviors.empty();
        });
    }
}


class Worker {
    public static Behavior<Object> create() {
        return Behaviors.setup(context -> {

            var config = context.getSystem().settings().config();
            System.out.println(config.getInt("MyApp.version"));
            var databaseConfig = config.getConfig("MyApp.database");
            System.out.println(databaseConfig.getString("driver"));
            System.out.println(databaseConfig.getString("url"));
            System.out.println(databaseConfig.getString("user"));

            return Behaviors.empty();
        });
    }
}