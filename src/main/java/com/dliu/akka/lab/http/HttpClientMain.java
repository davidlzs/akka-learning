package com.dliu.akka.lab.http;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class HttpClientMain {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final ActorSystem system = ActorSystem.create();

        final CompletionStage<HttpResponse> responseFuture =
                Http.get(system)
                        .singleRequest(HttpRequest.create("https://integration-api-iot.yardienergy.com/iot-api/monitoring"));

        Materializer materializer = ActorMaterializer.create(system);
        String result = responseFuture
                .thenCompose(res -> res.entity().toStrict(10000, materializer))
                .thenApply(s -> s.getData())
                .thenApply(d -> d.utf8String())
                .toCompletableFuture()
                .get();

        System.out.println(result);
    }
}
