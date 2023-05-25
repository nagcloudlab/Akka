package com.example.strategy;


import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.SupervisorStrategyConfigurator;
import akka.japi.pf.DeciderBuilder;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static akka.actor.SupervisorStrategy.*;

/**
 * Supervision Strategies:
 * 1. One for one
 * 2. All for one
 *
 * Possible actions:
 *
 * restart : kill the child actor, and restart the new one
 * resume : let the child actor keep running as nothing happened
 * stop: kill the child actor
 * escalate: Let supervisor actor to handle this situation
 *
 */

public class SupervisionStrategyMain implements SupervisorStrategyConfigurator {
    public static SupervisorStrategy SUPERVISIONSTRATEGY = new OneForOneStrategy(2,
            Duration.create(1, TimeUnit.MINUTES),
            DeciderBuilder.matchAny((Throwable o) -> escalate()).build());
    @Override
    public SupervisorStrategy create() {
        return SUPERVISIONSTRATEGY;
    }
}
