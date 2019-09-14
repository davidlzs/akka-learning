package com.dliu.akka.lab;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;

public class WebSocketServerExample extends AllDirectives {
    public static void main(String[] args) {
        new WebSocketServerExample();
    }

    public WebSocketServerExample() {
        ActorSystem system = ActorSystem.create("ws-system");
        Materializer materializer = ActorMaterializer.create(system);
        Http http = Http.get(system);

        Route route = path("ws", () -> handleWebSocketMessages(Flow.of(Message.class)));

        Flow<HttpRequest, HttpResponse, NotUsed> flow = route.flow(system, materializer);
        http.bindAndHandle(flow, ConnectHttp.toHost("127.0.0.1", 9090), materializer);
    }
}
