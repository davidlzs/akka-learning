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

import java.util.Arrays;
import java.util.List;
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

        LOGGER.info("say hello to - " +  in.getName());
        List<HelloReply> replies = Arrays.asList("David", "Susan", "Bob")
                .stream()
                .map(name -> HelloReply.newBuilder().setMessage("Hello from " + name + " to " + in.getName()).build())
                .collect(Collectors.toList());
        return Source.from(replies);
    }

    @Override
    public Source<HelloReply, NotUsed> streamHellos(Source<HelloRequest, NotUsed> in) {
        LOGGER.info("say hello to - streaming with a streaming");
        return in.map(request -> HelloReply.newBuilder().setMessage("Hello " + request.getName()).build());
    }
}
