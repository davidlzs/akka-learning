package com.dliu.akka.lab.doc.modularity;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.actor.ClassicActorSystemProvider;
import akka.stream.ActorMaterializer;
import akka.stream.ActorMaterializer$;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class AkkaDocModularity {
    private ActorSystem system = ActorSystem.create("doc-modularity");
    private ActorMaterializer materializer = ActorMaterializer.create(system);

    public static void main(String[] args) {
        AkkaDocModularity akkaDocModularity = new AkkaDocModularity();

        // running simple graph without nesting
        RunnableGraph<NotUsed> simpleGraph = akkaDocModularity.simpleGraphWithoutNesting();
        akkaDocModularity.runGraph(simpleGraph);


        // running simple graph with nesting
        RunnableGraph<NotUsed> simpleGraphWithNesting = akkaDocModularity.simpleGraphWithNesting();
        akkaDocModularity.runGraph(simpleGraphWithNesting);

    }

    private void runGraph(RunnableGraph<NotUsed> simpleGraph) {
        new Thread(() -> simpleGraph.run(materializer)).start();
    }

    /**
     *
     * @return
     */
    private RunnableGraph<NotUsed> simpleGraphWithoutNesting() {
        return Source.single(0)
                .map(i -> i + 1)
                .filter(i -> i != 0)
                .map(i -> i - 2)
                .log("simple stream without nesting")
                .to(Sink.fold(0, (acc, i) -> acc + i));

        // -- there is no nesting
    }

    private RunnableGraph<NotUsed> simpleGraphWithNesting() {
        Source<Integer, NotUsed> nestedSource = Source.single(0)
                .map(i -> i + 1)
                .named("nestedsource");
        Flow<Integer, Integer, NotUsed> nestedflow = Flow.of(Integer.class)
                .filter(i -> i != 0)
                .map(i -> i + 2)
                .named("nestedflow")
                .log("nestedflow");
        Sink<Integer, NotUsed> nestedSink = nestedflow.to(Sink.fold(0, (acc, i) -> acc + i))
                .named("nestedsink");
        return nestedSource.to(nestedSink);
    }
}
