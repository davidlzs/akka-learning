package com.dliu.akka.typed.cqrs;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Message implements JsonSerializable {
    public final String text;

    @JsonCreator
    public Message(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Message{" +
                "text='" + text + '\'' +
                '}';
    }
}
