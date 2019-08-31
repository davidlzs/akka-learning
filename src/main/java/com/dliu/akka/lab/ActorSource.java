package com.dliu.akka.lab;

import org.reactivestreams.Publisher;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.http.javadsl.model.ws.Message;
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.AsPublisher;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class ActorSource {
    private final ActorRef actor;
    private final Publisher<Message> publisher;
    public ActorSource(Materializer materializer) {
        Pair<ActorRef, Publisher<Message>> pair = Source.<Message>actorRef(1, OverflowStrategy.fail())
                .toMat(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), Keep.both())
                .run(materializer);
        actor = pair.first();
        publisher = pair.second();
    }

    public ActorRef getActor() {
        return actor;
    }

    public Source<Message, NotUsed> getPublisher() {
        return Source.fromPublisher(publisher);
    }
}
