package com.dliu.akka.typed.actorandfuture;

public class BaseMessage {
    public String message;

    public BaseMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "BaseMessage{" +
                "message='" + message + '\'' +
                '}';
    }
}
