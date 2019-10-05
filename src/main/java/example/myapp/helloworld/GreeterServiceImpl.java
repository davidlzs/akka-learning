package example.myapp.helloworld;

import akka.NotUsed;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import example.myapp.helloworld.grpc.GreeterService;
import example.myapp.helloworld.grpc.HelloReply;
import example.myapp.helloworld.grpc.HelloRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class GreeterServiceImpl implements GreeterService {
    Logger LOGGER = LoggerFactory.getLogger(GreeterService.class);
    private Materializer mat;

    public GreeterServiceImpl(Materializer mat) {
        this.mat = mat;
    }

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

        LOGGER.info("say hello to  - stream");
        return in.runWith(Sink.seq(), mat)
                .thenApply(elements -> {
                    String elementsString = elements.stream().map(element -> element.getName())
                            .collect(Collectors.toList())
                            .toString();
                    return HelloReply.newBuilder()
                            .setMessage("Hello " + elementsString)
                            .build();
                });
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