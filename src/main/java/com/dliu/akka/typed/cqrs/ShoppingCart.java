package com.dliu.akka.typed.cqrs;


import java.util.HashMap;
import java.util.Map;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandlerWithReply;
import akka.persistence.typed.javadsl.CommandHandlerWithReplyBuilder;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehaviorWithEnforcedReplies;
import akka.persistence.typed.javadsl.ReplyEffect;

public class ShoppingCart extends EventSourcedBehaviorWithEnforcedReplies<ShoppingCart.Command, ShoppingCart.Event, ShoppingCart.State> {

    // Difference between PersistenceId and EntityId?

    // EntityTypeKey is from typed sharding.
    public static final EntityTypeKey<Command> ENTITY_TYPE_KEY = EntityTypeKey.create(Command.class, "ShoppingCart");
    private final ActorContext<Command> context;

    private ShoppingCart(String cartId, ActorContext<Command> context) {
        super(PersistenceId.of(ENTITY_TYPE_KEY.name(), cartId)); // What is the name of EntityTypeKey?
        this.context = context;
    }

    public static void init(ActorSystem<?> system) {
        system.log().info("Starting the sharding....");
        ClusterSharding.get(system).init(Entity.of(ENTITY_TYPE_KEY, entityContext -> {
            return Behaviors.setup(ctx -> new ShoppingCart(entityContext.getEntityId(), ctx));
        }));
    }

    // Commands
    public interface Command extends JsonSerializable {}

    public static class AddItem implements  Command {
        public final String itemId;
        public final int quantity;

        public AddItem(String itemId, int quantity) {
            this.itemId = itemId;
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return "AddItem{" +
                    "itemId='" + itemId + '\'' +
                    ", quantity=" + quantity +
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
    }
    // End of State

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
                .onCommand(AddItem.class, this::addItem)
                .build();
    }

    private ReplyEffect<Event, State> addItem(AddItem command) {
        context.getLog().info("Add Item: " + command);
        ItemAdded itemAdded = new ItemAdded(command.itemId, command.quantity);
        // TODO: add reply to the caller actor
        return Effect().persist(itemAdded).thenNoReply();
    }
}
