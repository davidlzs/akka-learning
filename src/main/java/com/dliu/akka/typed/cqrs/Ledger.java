package com.dliu.akka.typed.cqrs;

import org.slf4j.Logger;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.ClusterShardingSettings;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.pattern.StatusReply;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.RecoveryCompleted;
import akka.persistence.typed.javadsl.CommandHandlerWithReply;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehaviorWithEnforcedReplies;
import akka.persistence.typed.javadsl.ReplyEffect;
import akka.persistence.typed.javadsl.SignalHandler;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class Ledger extends EventSourcedBehaviorWithEnforcedReplies<Ledger.Command, Ledger.Event, Ledger.State> {
    public static final EntityTypeKey<Command> ENTITY_TYPE_KEY = EntityTypeKey.create(Command.class, "Ledger");

    private final Logger logger;

    private Ledger(String ledgerName, ActorContext ctx) {
        super(PersistenceId.of(ENTITY_TYPE_KEY.name(), ledgerName));
        logger = ctx.getLog();
        // https://doc.akka.io/docs/akka/current/typed/from-classic.html#lifecycle-hooks
        // In Akka typed actor:
        // There are no PreStart and PostRestart signals because such action can be done from Behaviors.setup or the constructor of the AbstractBehavior class.
        logger.info("Simulated PostStart... the constructor is called from Behaviors.setup");
    }

    public static Behavior<Command> create(String ledgerName) {
        return Behaviors.setup(ctx -> new Ledger(ledgerName, ctx));
    }

    public static void init(ActorSystem<?> system) {
        system.log().info("Starting the sharding....");
        ClusterShardingSettings settings = ClusterShardingSettings.create(system);
                // Enable remember entities, search "remember entities"
                // or your own log message "Recover completed with state"
                // or your own log message "Simulated PostStart"
                //.withRememberEntities(true);
        ClusterSharding.get(system).init(Entity.of(ENTITY_TYPE_KEY, entityContext -> {
            return create(entityContext.getEntityId());
        }).withSettings(settings));
    }

    @Override
    public State emptyState() {
        return State.empty();
    }

    @Override
    public EventHandler<State, Event> eventHandler() {
       return newEventHandlerBuilder().forAnyState()
               .onEvent(Debited.class, (state, debited) -> state.debit(debited.account, debited.amount))
               .onEvent(Credited.class, (state, credited) -> state.credit(credited.account, credited.amount))
               .build();
    }

    @Override
    public CommandHandlerWithReply<Command, Event, State> commandHandler() {

        return newCommandHandlerWithReplyBuilder()
                .forAnyState()
                .onCommand(Debit.class, this::debit)
                .onCommand(Credit.class, this::credit)
                .build();
    }

    @Override
    public SignalHandler<State> signalHandler() {
        return newSignalHandlerBuilder()
                .onSignal(RecoveryCompleted.instance(), (state) -> {
                    logger.info("Recover completed with state: {}", state);
                })
                .build();
    }

    private ReplyEffect<Event, State> credit(State state, Credit cmd) {
        return Effect().persist(new Credited(cmd.account, cmd.amount))
                .thenReply(cmd.replyTo, updateState -> StatusReply.success(updateState.toResult()));
    }

    private ReplyEffect<Event, State> debit(State state, Debit cmd) {
        return Effect().persist(new Debited(cmd.account, cmd.amount))
                .thenReply(cmd.replyTo, updatedState -> StatusReply.success(updatedState.toResult()));
    }


    // Commands
    public interface Command extends JsonSerializable {
    }

    public class Debit implements Command {
        public final String account;
        public final BigDecimal amount;
        public final ActorRef<StatusReply<Result>> replyTo;

        public Debit(String account, BigDecimal amount, ActorRef<StatusReply<Result>> replyTo) {
            this.account = account;
            this.amount = amount;
            this.replyTo = replyTo;
        }
    }

    public static class Credit implements Command {
        public final String account;
        public final BigDecimal amount;
        public final ActorRef<StatusReply<Result>> replyTo;

        public Credit(String account, BigDecimal amount, ActorRef<StatusReply<Result>> replyTo) {
            this.account = account;
            this.amount = amount;
            this.replyTo = replyTo;
        }
    }

    // Events
    public interface Event extends JsonSerializable {
    }

    public static class Debited implements Event {
        public final String account;
        public final BigDecimal amount;

        public Debited(String account, BigDecimal amount) {
            this.account = account;
            this.amount = amount;
        }
    }

    public static class Credited implements Event {
        public final String account;
        public final BigDecimal amount;

        public Credited(String account, BigDecimal amount) {
            this.account = account;
            this.amount = amount;
        }
    }
    // State
    public static final class State {
        private final Map<String, BigDecimal> accounts;

        private State() {
            accounts = new HashMap<>();
        }

        public static State empty() {
            return new State();
        }

        public State debit(String account, BigDecimal amount) {
            // Todo, when there is not enough balance for debit, should be handled in command validation!!!
            if (accounts.containsKey(account)) {
                accounts.put(account, accounts.get(account).subtract(amount));
            } else {
                accounts.put(account, amount);
            }
            return this;
        }

        public State credit(String account, BigDecimal amount) {
            if (accounts.containsKey(account)) {
                accounts.put(account, accounts.get(account).add(amount));
            } else {
                accounts.put(account, amount);
            }
            return this;
        }

        public Result toResult() {
            return new Result(accounts);
        }
    }
    // Result
    public static class Result {
        public final Map<String, BigDecimal> accounts;

        public Result(Map<String, BigDecimal> accounts) {
            this.accounts = Collections.unmodifiableMap(accounts);
        }

        @Override
        public String toString() {
            return "Result{" +
                    "accounts=" + accounts +
                    '}';
        }
    }
}
