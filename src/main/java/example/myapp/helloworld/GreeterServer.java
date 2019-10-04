package example.myapp.helloworld;

import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import example.myapp.helloworld.grpc.GreeterService;
import example.myapp.helloworld.grpc.GreeterServiceHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

public class GreeterServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GreeterServer.class);

    public static void main(String[] args) {
        Config config = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")
                .withFallback(ConfigFactory.defaultApplication());
        ActorSystem system = ActorSystem.create("HelloWorld", config);

        run(system).thenAccept(binding -> {
            LOGGER.info("Server started");
        });
    }

    private static CompletionStage<ServerBinding> run(ActorSystem system) {
        Materializer mat = ActorMaterializer.create(system);
        GreeterService iml = new GreeterServiceImpl();
        return Http.get(system).bindAndHandleAsync(
                GreeterServiceHandlerFactory.create(iml, mat, system),
                ConnectHttp.toHost("127.0.0.1", 8080),
                mat
        );
    }
}
