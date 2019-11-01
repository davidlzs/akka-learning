package com.dliu.natsio.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import io.nats.client.Connection;
import io.nats.client.Nats;

public class PublishEvents {
    static class EventEnvelope {
        public UUID id;
        public String source;
        public String type;
        public Instant createAt;
        public Changes body;

        private EventEnvelope(UUID id, String source, String type, Instant createAt, Changes body) {
            this.id = id;
            this.source = source;
            this.type = type;
            this.createAt = createAt;
            this.body = body;
        }

        public static class EventEnvelopeBuilder {
            private UUID id;
            private String source;
            private String type;
            private Instant createAt;
            private Changes body;

            public EventEnvelopeBuilder withId(UUID id) {
                this.id = id;
                return this;
            }

            public EventEnvelopeBuilder withSource(String source) {
                this.source = source;
                return this;
            }

            public EventEnvelopeBuilder withType(String type) {
                this.type = type;
                return this;
            }

            public EventEnvelopeBuilder withCreateAt(Instant createAt) {
                this.createAt = createAt;
                return this;
            }

            public EventEnvelopeBuilder withBody(Changes changes) {
                this.body = changes;
                return this;
            }

            public EventEnvelope build() {
                return new EventEnvelope(id, source, type, createAt, body);
            }
        }
    }

    static class Changes {
        public Map<String, String> changes;
        public Operator operator;

        private Changes(Map<String, String> changes, Operator operator) {
            this.changes = changes;
            this.operator = operator;
        }

        public static class ChangesBuilder {
            private Map<String, String> changes;
            private Operator operator;

            public ChangesBuilder withChanges(Map<String, String> changes) {
                this.changes = changes;
                return this;
            }

            public ChangesBuilder withOperator(Operator operator) {
                this.operator = operator;
                return this;
            }

            public Changes build() {
                return new Changes(changes, operator);
            }
        }
    }

    static class Operator {
        public String type;
        public String id;

        private Operator(String type, String id) {
            this.type = type;
            this.id = id;
        }

        public static class OperatorBuilder {
            private String type;
            private String id;

            public OperatorBuilder withType(String type) {
                this.type = type;
                return this;
            }

            public OperatorBuilder withId(String id) {
                this.id = id;
                return this;
            }

            public Operator build() {
                return new Operator(type, id);
            }
        }
    }

    public static void main(String[] args) {
        try (Connection nc = Nats.connect("nats://localhost:4222")) {
            while(true) {
                List<EventEnvelope> eventEnvelopes = Arrays.asList(
                        randomLockEvent(),
                        randomThermostatEvent()
                );

                for (EventEnvelope eventEnvelope : eventEnvelopes) {
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    String json = gson.toJson(eventEnvelope);

                    nc.publish(eventEnvelope.source.replace("/", ".").substring(1), json.getBytes(StandardCharsets.UTF_8));

                    nc.flush(Duration.ZERO);
                }
                //Thread.sleep(1000L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    private static EventEnvelope randomThermostatEvent() {
        String operatorType = randomOperatorType();
        int temp = randomTemperature();
        int coolingSetpoint = randomCoolingSetpoint();
        int heatingSetpoint = randomHeatingSetpoint();
        Map<String, String> changeSet = new HashMap<>();
        changeSet.put("temperature", String.valueOf(temp));
        changeSet.put("coolingSetpoint", String.valueOf(coolingSetpoint));
        changeSet.put("heatingSetpoint", String.valueOf(heatingSetpoint));
        Changes changes = new Changes.ChangesBuilder().withChanges(changeSet)
                .build();
        return new EventEnvelope.EventEnvelopeBuilder().withId(UUID.randomUUID())
                .withCreateAt(Instant.now())
                .withType("DeviceStateChange")
                .withSource("/hub/e0b8c1d3-ab3b-454b-a8f7-86d34acc4594/device/a1234567-ad3b-11e8-98d0-529269fb1459/Thermostat")
                .withBody(changes)
                .build();
    }

    private static int randomTemperature() {
        Random random = new Random();
        return 20 + random.nextInt(10);
    }

    private static int randomCoolingSetpoint() {
        Random random = new Random();
        return 25 + random.nextInt(10);
    }

    private static int randomHeatingSetpoint() {
        Random random = new Random();
        return 10 + random.nextInt(7);
    }





    private static EventEnvelope randomLockEvent() {
        String operatorType = randomOperatorType();
        String userId = operatorType.equals("Remote") || operatorType.equals("KeyPad") ? randomUserId() : null;
        Operator operator = new Operator.OperatorBuilder().withId(userId).withType(operatorType).build();
        String lockState = randomLockState();
        Changes changes = new Changes.ChangesBuilder().withChanges(Collections.singletonMap("lockState", lockState))
                .withOperator(operator)
                .build();
        return new EventEnvelope.EventEnvelopeBuilder().withId(UUID.randomUUID())
                .withCreateAt(Instant.now())
                .withType("DeviceStateChange")
                .withSource("/gateway/e0b8c1d3-ab3b-454b-a8f7-86d34acc4594/device/516920d4-ad3b-11e8-98d0-529269fb1459/lock")
                .withBody(changes)
                .build();
    }

    private static String randomUserId() {
        String userId;
        Random random = new Random();
        userId = "User" + random.nextInt(1000);
        return userId;
    }

    private static String randomLockState() {
        Random random = new Random();
        return random.nextInt() > 0.5 ? "LOCKED" : "UNLOCKED";
    }

    private static String randomOperatorType() {
        List<String> operatorTypes = Arrays.asList("Remote", "KeyPad", "Manual");
        Random random = new Random();
        int index = random.nextInt(3);
        return operatorTypes.get(index);
    }

}
