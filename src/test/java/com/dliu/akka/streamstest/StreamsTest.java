package com.dliu.akka.streamstest;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.testkit.TestPublisher;
import akka.stream.testkit.TestSubscriber;
import akka.stream.testkit.javadsl.TestSink;
import akka.stream.testkit.javadsl.TestSource;
import akka.testkit.javadsl.TestKit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class StreamsTest {
    private ActorSystem system;
    private ActorMaterializer mat;

    @Before
    public void setup() {
        this.system = ActorSystem.create("StreamsTest");
        this.mat = ActorMaterializer.create(system);
    }

    @Test
    public void basicAssertion() throws InterruptedException, ExecutionException, TimeoutException {
        // setup
        Source<Integer, NotUsed> simpleSource = Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        Sink<Integer, CompletionStage<Integer>> simpleSink = Sink.fold(0, (Integer a, Integer b) -> a + b);
        // execute
        CompletionStage<Integer> sumFuture = simpleSource.toMat(simpleSink, Keep.right()).run(mat);
        Integer sum = sumFuture.toCompletableFuture().get(5, TimeUnit.SECONDS);
        // assert
        assertEquals(55, sum.intValue());
    }

    @Test
    public void assertViaTestActorForMaterializedValue() {
        // setup
        Source<Integer, NotUsed> simpleSource = Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        Sink<Integer, CompletionStage<Integer>> simpleSink = Sink.fold(0, Integer::sum);
        TestKit probe = new TestKit(system);
        // execute
        Patterns.pipe(simpleSource.toMat(simpleSink, Keep.right()).run(mat), system.dispatcher()).to(probe.getRef());
        // assert
        probe.expectMsg(55);
    }

    @Test
    public void assertViaTestActorBasedSink() {
        // setup
        Source<Integer, NotUsed> simpleSource = Source.from(Arrays.asList(1, 2, 3, 4, 5));
        Flow<Integer, Integer, NotUsed> flow = Flow.of(Integer.class).scan(0, Integer::sum); // 0, 1, 3, 6, 10, 15
        Source<Integer, NotUsed> streamUnderTest = simpleSource.via(flow);
        TestKit probe = new TestKit(system);
        Sink<Integer, NotUsed> probeSink = Sink.<Integer>actorRef(probe.getRef(), "Completion message");
        // execute
        streamUnderTest.to(probeSink).run(mat);
        // assert
        probe.expectMsgAllOf(0, 1, 3, 6, 10, 15);
    }

    @Test
    public void assertViaStreamsTestKitSink() {
        // setup
        Source<Integer, NotUsed> sourceUnderTest = Source.from(Arrays.asList(1, 2, 3, 4, 5)).map((Integer a) -> 2 * a);
        Sink<Integer, TestSubscriber.Probe<Integer>> testSink = TestSink.<Integer>probe(system);
        // execute
        TestSubscriber.Probe<Integer> materializedValue = sourceUnderTest.runWith(testSink, mat);
        // assert
        materializedValue.request(5)
                .expectNext(2, 4, 6, 8, 10)
                .expectComplete();
    }

    @Test
    public void integrateWithStreamsTestSource() {
        // setup
        Sink<Integer, CompletionStage<Done>> sinkUnderTest = Sink.foreach((Integer a) -> {
            if (a == 13) throw new RuntimeException("bad luck");
        });

        Source<Integer, TestPublisher.Probe<Integer>> testSource = TestSource.<Integer>probe(system);
        // execute
        Pair<TestPublisher.Probe<Integer>, CompletionStage<Done>> materialized = testSource.toMat(sinkUnderTest, Keep.both()).run(mat);
        TestPublisher.Probe<Integer> testPublisher = materialized.first();
        CompletionStage<Done> resultFuture = materialized.second();
        testPublisher
                .sendNext(1)
                .sendNext(5)
                .sendNext(13)
                .sendComplete();
        // assert
        resultFuture.toCompletableFuture()
                .thenAccept((done) -> fail("should fail on 13")).join();
        //assertNotNull(done);

    }
}
