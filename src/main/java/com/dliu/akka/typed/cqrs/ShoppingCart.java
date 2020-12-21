package com.dliu.akka.typed.cqrs;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.pattern.StatusReply;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.RecoveryCompleted;
import akka.persistence.typed.javadsl.CommandHandlerWithReply;
import akka.persistence.typed.javadsl.CommandHandlerWithReplyBuilder;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehaviorWithEnforcedReplies;
import akka.persistence.typed.javadsl.ReplyEffect;
import akka.persistence.typed.javadsl.SignalHandler;

public class ShoppingCart extends EventSourcedBehaviorWithEnforcedReplies<ShoppingCart.Command, ShoppingCart.Event, ShoppingCart.State> {

    // Difference between PersistenceId and EntityId?

    // EntityTypeKey is from typed sharding.
    public static final EntityTypeKey<Command> ENTITY_TYPE_KEY = EntityTypeKey.create(Command.class, "ShoppingCart");
    private final ActorContext<Command> context;
    private final OpenShoppingCartCommandHandler openShoppingCartCommandHandler = new OpenShoppingCartCommandHandler();

    private ShoppingCart(String cartId, ActorContext<Command> context) {
        super(PersistenceId.of(ENTITY_TYPE_KEY.name(), cartId)); // What is the name of EntityTypeKey?
        this.context = context;
    }

    public static void init(ActorSystem<?> system) {
        system.log().info("Starting the sharding....");
        ClusterSharding.get(system).init(Entity.of(ENTITY_TYPE_KEY, entityContext -> {
            return create(entityContext.getEntityId());
        }));
    }

    public static Behavior<Command> create(String cartId) {
        return Behaviors.setup(ctx -> new ShoppingCart(cartId, ctx));
    }

    // Commands
    public interface Command extends JsonSerializable {}

    public static class AddItem implements  Command {
        public final String itemId;
        public final int quantity;
        public final ActorRef<StatusReply<Summary>> replyTo;

        public AddItem(String itemId, int quantity, ActorRef<StatusReply<Summary>> replyTo) {
            this.itemId = itemId;
            this.quantity = quantity;
            this.replyTo = replyTo;
        }

        @Override
        public String toString() {
            return "AddItem{" +
                    "itemId='" + itemId + '\'' +
                    ", quantity=" + quantity +
                    ", replyTo=" + replyTo +
                    '}';
        }
    }
    // End of Commands
    // Events
    public interface Event extends JsonSerializable {}

    private static class ItemAdded implements Event {
        public final String itemId;
        public final int quantity;

        public ItemAdded(String itemId, int quantity) {
            this.itemId = itemId;
            this.quantity = quantity;
        }
    }
    // End of Events
    // State
    public final class State  implements JsonSerializable {
        private Map<String, Integer> items = new HashMap<>();
        public State updateItem(String itemId, int quantity) {
            items.put(itemId, quantity);
            return this;
        }

        public Summary toSummary() {
            return new Summary(items);
        }
    }
    // End of State
    // Summary
    public static final class Summary {
        public final Map<String, Integer> items;

        public Summary(Map<String, Integer> items) {
            this.items = Collections.unmodifiableMap(items);
        }
    }
    // End of Summary

    @Override
    public State emptyState() {
        return new State();
    }

    @Override
    public EventHandler<State, Event> eventHandler() {
        return newEventHandlerBuilder().forAnyState()
                .onEvent(ItemAdded.class, (state, event) -> state.updateItem(event.itemId, event.quantity))
                .build();
    }

    @Override
    public CommandHandlerWithReply<Command, Event, State> commandHandler() {
        CommandHandlerWithReplyBuilder<Command, Event, State> builder = newCommandHandlerWithReplyBuilder();

        return builder.forAnyState()
                .onCommand(AddItem.class, (state, cmd) -> openShoppingCartCommandHandler.addItem(state, cmd, context))
                .onAnyCommand(this::unhandledCommand);
    }

    @Override
    public SignalHandler<State> signalHandler() {
        return newSignalHandlerBuilder()
                .onSignal(RecoveryCompleted.class, (state, signal) -> {
                    context.getLog().info("Recovery Finished: State is {} and signal is: {}", state, signal);
                })
                .build();

    }

    private ReplyEffect<Event, State> unhandledCommand(Command command) {
        context.getLog().info("Unhandled command: {}", command);
        return Effect().noReply();
    }



    private final class OpenShoppingCartCommandHandler {
        private ReplyEffect<Event, State> addItem(State state, AddItem command, ActorContext<?> context) {
            context.getLog().info("Add Item: " + command);
            ItemAdded itemAdded = new ItemAdded(command.itemId, command.quantity);
            if (!state.items.containsKey(command.itemId)) {
                return Effect().persist(itemAdded).thenReply(command.replyTo, updatedState -> StatusReply.success(updatedState.toSummary()));
            } else {
                return Effect().reply(command.replyTo, StatusReply.error("Item '" + command.itemId + "' was already added to this shopping cart"));
            }
        }
    }
}
