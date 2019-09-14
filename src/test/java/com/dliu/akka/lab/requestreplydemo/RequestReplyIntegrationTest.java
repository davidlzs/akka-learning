package com.dliu.akka.lab.requestreplydemo;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.dliu.akka.lab.requestreplydemo.actors.BackendEchoActor;
import com.dliu.akka.lab.requestreplydemo.actors.BackendProtocol;
import com.dliu.akka.lab.requestreplydemo.actors.BackendRouterActor;
import com.dliu.akka.test.utils.Faker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RequestReplyIntegrationTest {
    public static final int NUMBER_OF_REQUESTS = 100000;
    public static final long MAX_ASSERT_WAITING_IN_SECONDS = 30000L;
    private ActorSystem system;

    @Before
    public void setUp() throws Exception {
        system = ActorSystem.create("integration-system");
    }

    @After
    public void tearDown() throws Exception {
        system.terminate();
    }

    @Test
    public void endToEnd() {
        // Setup
        ActorRef backendActor = system.actorOf(BackendEchoActor.props());
        ActorRef backendRouter = system.actorOf(BackendRouterActor.props(backendActor));

        TestKit probe =  new TestKit(system);

        UUID requestId = Faker.fakeId();
        BackendProtocol.ExecuteCommandCmd cmd = new BackendProtocol.ExecuteCommandCmd(requestId, "getTemperature");

        BackendProtocol.ExecuteCommandResponse expectedResponse = new BackendProtocol.ExecuteCommandResponse(requestId, "processed getTemperature");

        // Execute
        backendRouter.tell(cmd, probe.getRef());

        // Assert
        probe.expectMsg(expectedResponse);

    }


    @Test
    public void multipleRequestEndToEnd() throws InterruptedException {
        // Setup
        ActorRef backendActor = system.actorOf(BackendEchoActor.props());
        ActorRef backendRouter = system.actorOf(BackendRouterActor.props(backendActor));

        List<TestKit>  probes = new ArrayList<>();
        List<BackendProtocol.ExecuteCommandCmd> cmds = new ArrayList<>();
        List<BackendProtocol.ExecuteCommandResponse> expectedResponses = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_REQUESTS; i++) {
            probes.add(new TestKit(system));
            BackendProtocol.ExecuteCommandCmd cmd = createExecuteCommandCmd(i);
            cmds.add(cmd);
            expectedResponses.add(createExpectedExecuteCommandResponse(cmd));
        }

        System.out.println("done setup");
        // Execute
        for (int i = 0; i < NUMBER_OF_REQUESTS; i++) {
            System.out.println("sending request: " + cmds.get(i));
            backendRouter.tell(cmds.get(i), probes.get(i).getRef());
        }

        System.out.println("sent all messages" );

        // Assert
        for (int i = 0; i < NUMBER_OF_REQUESTS; i++) {
            probes.get(i).expectMsg(expectedResponses.get(i));
        }
        System.out.println("all expected responses received" );


        Thread.sleep(MAX_ASSERT_WAITING_IN_SECONDS);
    }

    private BackendProtocol.ExecuteCommandResponse createExpectedExecuteCommandResponse(BackendProtocol.ExecuteCommandCmd cmd) {
        return new BackendProtocol.ExecuteCommandResponse(cmd.correlationId, "processed getTemperature");
    }

    private BackendProtocol.ExecuteCommandCmd createExecuteCommandCmd(int sequence) {
        UUID requestId = Faker.fakeId(sequence);
        return new BackendProtocol.ExecuteCommandCmd(requestId, "getTemperature");
    }
}
