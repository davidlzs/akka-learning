package example.myapp.helloworld;

import akka.actor.ActorSystem;
import akka.grpc.GrpcClientSettings;
import akka.stream.ActorMaterializer;
import example.myapp.helloworld.grpc.GreeterService;
import example.myapp.helloworld.grpc.GreeterServiceClient;
import example.myapp.helloworld.grpc.HelloReply;
import example.myapp.helloworld.grpc.HelloRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class GreeterClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GreeterClient.class);

    public static void main(String[] args) {
        String serverHost = "127.0.0.1";
        int serverPort = 8080;

        ActorSystem system = ActorSystem.create("HelloWorldClient");
        ActorMaterializer materializer = ActorMaterializer.create(system);

        GrpcClientSettings settings = GrpcClientSettings.fromConfig(GreeterService.name, system);

        GreeterServiceClient client = GreeterServiceClient.create(settings, materializer, system.dispatcher());

        try {
            HelloRequest request = HelloRequest.newBuilder()
                    .setName("David")
                    .build();
            CompletionStage<HelloReply> reply = client.sayHello(request);
            LOGGER.info("got reply {}", reply.toCompletableFuture().get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
