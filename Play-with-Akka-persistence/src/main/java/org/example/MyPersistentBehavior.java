package org.example;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.persistence.typed.*;
import akka.persistence.typed.javadsl.*;

import java.util.*;

public class MyPersistentBehavior extends EventSourcedBehavior<MyPersistentBehavior.Command, MyPersistentBehavior.Event, MyPersistentBehavior.State> {


    public MyPersistentBehavior(PersistenceId persistenceId) {
        super(persistenceId);
    }


    public static Behavior<Command> create(PersistenceId persistenceId) {
        return Behaviors.setup(
                context -> {
                    return new MyPersistentBehavior(persistenceId);
                });
    }

    interface Command {
    }

    public enum Display implements Command {
        INSTANCE
    }

    public static class Add implements Command {
        public final String data;

        public Add(String data) {
            this.data = data;
        }
    }

    public enum Clear implements Command {
        INSTANCE
    }

    interface Event {
    }

    public static class Added implements Event {
        public final String data;

        public Added(String data) {
            this.data = data;
        }
    }

    public enum Cleared implements Event {
        INSTANCE
    }


    public static class State {
        private final List<String> items;

        private State(List<String> items) {
            this.items = items;
        }

        public State() {
            this.items = new ArrayList<>();
        }

        public State addItem(String data) {
            List<String> newItems = new ArrayList<>(items);
            newItems.add(0, data);
            // keep 5 items
            List<String> latest = newItems.subList(0, Math.min(5, newItems.size()));
            return new State(latest);
        }

        public List<String> getItems() {
            return items;
        }
    }


    @Override
    public State emptyState() {
        return new State();
    }

    @Override
    public CommandHandler<Command, Event, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(Display.class, (state, command) -> {
                    System.out.println(state.getItems());
                    return Effect().none();
                })
                .onCommand(Add.class, command -> Effect().persist(new Added(command.data)))
                .onCommand(Clear.class, command -> Effect().persist(Cleared.INSTANCE))
                .build();
    }


    @Override
    public EventHandler<State, Event> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(Added.class, (state, event) -> {
                    System.out.println(event);
                    return state.addItem(event.data);
                })
                .onEvent(Cleared.class, (state, event) -> new State())
                .build();
    }


}
