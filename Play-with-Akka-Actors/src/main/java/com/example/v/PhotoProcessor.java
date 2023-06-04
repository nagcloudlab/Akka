package com.example.v;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.ServiceKey;

public class PhotoProcessor {

    public static ServiceKey<String> key = ServiceKey.create(String.class, "photo-processor-key");

    public static Behavior<String> create(){
        return Behaviors.ignore();
    }

}
