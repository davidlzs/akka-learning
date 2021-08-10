package com.dliu.akka.lab.http;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static akka.util.ByteString.emptyByteString;

public class HttpClientMain {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final ActorSystem system = ActorSystem.create();

        final CompletionStage<HttpResponse> responseFuture =
                //Http.get(system)
                //        .singleRequest(HttpRequest.create("https://integration-api-iot.yardienergy.com/iot-api/monitoring"));
            Http.get(system)
                        .singleRequest(HttpRequest.POST("https://httpbin.org/post"));

        Materializer materializer = ActorMaterializer.create(system);
        // consume by using future compose
        /*
        String result = responseFuture
                .thenCompose(res -> res.entity().toStrict(10000, materializer))
                .thenApply(s -> s.getData())
                .thenApply(d -> d.utf8String())
                .toCompletableFuture()
                .get();
        */
        String result = responseFuture
                .thenCompose(res -> res.entity().toStrict(10000, materializer))
                .thenCompose(strict ->
                                     strict.getDataBytes()
                                             .runFold(emptyByteString(), (acc, b) -> acc.concat(b), system)
                                             .thenApply(s -> s.utf8String())
                )
                .toCompletableFuture()
                .get();

        System.out.println(result);

        system.terminate();
    }
}
