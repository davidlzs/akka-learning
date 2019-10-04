package example.myapp.helloworld;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import example.myapp.helloworld.grpc.GreeterService;
import example.myapp.helloworld.grpc.HelloReply;
import example.myapp.helloworld.grpc.HelloRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class GreeterServiceImpl implements GreeterService {
    Logger LOGGER = LoggerFactory.getLogger(GreeterService.class);

    @Override
    public CompletionStage<HelloReply> sayHello(HelloRequest in) {
        LOGGER.info("say hello to - " + in.getName());
        HelloReply reply = HelloReply.newBuilder()
                .setMessage("Hello, " + in.getName())
                .build();
        return CompletableFuture.completedFuture(reply);
    }

    @Override
    public CompletionStage<HelloReply> itKeepsTalking(Source<HelloRequest, NotUsed> in) {
        return null;
    }

    @Override
    public Source<HelloReply, NotUsed> itKeepsReplying(HelloRequest in) {
        return null;
    }

    @Override
    public Source<HelloReply, NotUsed> streamHellos(Source<HelloRequest, NotUsed> in) {
        return null;
    }
}
