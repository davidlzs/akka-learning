package com.dliu.grpc.rpcservice;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import akka.actor.ActorSystem;
import akka.grpc.GrpcClientSettings;
import akka.stream.ActorMaterializer;
import example.myapp.helloworld.grpc.HelloReply;
import example.myapp.helloworld.grpc.HelloRequest;

public class RPCClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RPCClient.class);

    public static void main(String[] args) throws IOException {

        Config config = ConfigFactory.load("akka_grpc_client.conf");

        ActorSystem system = ActorSystem.create("RPCClient", config);
        ActorMaterializer materializer = ActorMaterializer.create(system);

        GrpcClientSettings settings = GrpcClientSettings.fromConfig(RPCService.name, system);

        RPCServiceClient client = RPCServiceClient.create(settings, materializer, system.dispatcher());

        singleRequestReply(client);

        stopClient(system);

    }

    private static void stopClient(ActorSystem system) throws IOException {
        LOGGER.info("Press any key to stop client");
        System.in.read();
        system.terminate();
    }

    private static void singleRequestReply(RPCServiceClient client) {
        try {
            ExecuteCommandRequest request = ExecuteCommandRequest.newBuilder()
                    .setRequestId("MyRequest")
                    .build();
            CompletionStage<ExecuteCommandResponse> reply = client.executeCommand(request);
            LOGGER.info("got reply {}", reply.toCompletableFuture().get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
