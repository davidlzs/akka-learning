package com.dliu.grpc.rpcservice;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

public class RPCServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RPCServer.class);

    public static void main(String[] args) throws IOException {
        Config config = ConfigFactory.load("akka_grpc_server.conf")
                .withFallback(ConfigFactory.defaultApplication());
        ActorSystem system = ActorSystem.create("RPCService", config);

        Settings.SettingsImpl settings = Settings.SettingProvider.get(system);
        String host = settings.GRPC_SERVER_HOST;
        int port = settings.GRPC_SERVER_PORT;
        run(system, host, port).thenAccept(binding -> {
            LOGGER.info("Server started");
        });

        stopServer(system);
    }

    private static void stopServer(ActorSystem system) throws IOException {
        LOGGER.info("Press any key to stop server");
        System.in.read();
        system.terminate();
    }

    private static CompletionStage<ServerBinding> run(ActorSystem system, String host, int port) {
        Materializer mat = ActorMaterializer.create(system);
        RPCService impl = new RPCServiceImpl();
        return Http.get(system).bindAndHandleAsync(
                RPCServiceHandlerFactory.create(impl, system),
                ConnectHttp.toHost(host, port),
                mat
        );
    }
}
