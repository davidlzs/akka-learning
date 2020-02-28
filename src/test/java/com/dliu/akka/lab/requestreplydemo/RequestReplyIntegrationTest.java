package com.dliu.akka.lab.requestreplydemo;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.dliu.akka.lab.requestreplydemo.actors.BackendEchoActor;
import com.dliu.akka.lab.requestreplydemo.actors.BackendProtocol;
import com.dliu.akka.lab.requestreplydemo.actors.BackendRouterActor;
import com.dliu.akka.test.utils.Faker;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import kamon.Kamon;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Ignore("Ignore long running test, uncomment when you need to run this test")
public class RequestReplyIntegrationTest {
    private static final String MAX_ASSERT_WAITING_IN_MILLISECONDS_CONFIG = "max_assert_waiting_in_milliseconds";
    private static final String NUMBER_OF_REQUESTS_CONFIG = "number_of_requests";
    private static ActorSystem system;
    private static Config config;


    @BeforeClass
    public static void setupClass() {
        Kamon.init();
        system = ActorSystem.create("integration-system");
        config = ConfigFactory.load().getConfig("integration");
    }


    @AfterClass
    public static void teardownClass() throws Exception {
        system.terminate();
        system = null;
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
        for (int i = 0; i < getNumberOfRequestsConfig(); i++) {
            probes.add(new TestKit(system));
            BackendProtocol.ExecuteCommandCmd cmd = createExecuteCommandCmd(i);
            cmds.add(cmd);
            expectedResponses.add(createExpectedExecuteCommandResponse(cmd));
        }

        System.out.println("done setup");
        // Execute
        for (int i = 0; i < getNumberOfRequestsConfig(); i++) {
            System.out.println("sending request: " + cmds.get(i));
            backendRouter.tell(cmds.get(i), probes.get(i).getRef());
        }

        System.out.println("sent all messages" );

        // Assert
        for (int i = 0; i < getNumberOfRequestsConfig(); i++) {
            probes.get(i).expectMsg(expectedResponses.get(i));
        }
        System.out.println("all expected responses received" );


        Thread.sleep(config.getInt(MAX_ASSERT_WAITING_IN_MILLISECONDS_CONFIG));
    }

    private int getNumberOfRequestsConfig() {
        return config.getInt(NUMBER_OF_REQUESTS_CONFIG);
    }

    private BackendProtocol.ExecuteCommandResponse createExpectedExecuteCommandResponse(BackendProtocol.ExecuteCommandCmd cmd) {
        return new BackendProtocol.ExecuteCommandResponse(cmd.correlationId, "processed getTemperature");
    }

    private BackendProtocol.ExecuteCommandCmd createExecuteCommandCmd(int sequence) {
        UUID requestId = Faker.fakeId(sequence);
        return new BackendProtocol.ExecuteCommandCmd(requestId, "getTemperature");
    }
}
