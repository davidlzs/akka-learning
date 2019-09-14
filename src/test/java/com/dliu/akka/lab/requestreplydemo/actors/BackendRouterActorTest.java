package com.dliu.akka.lab.requestreplydemo.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.dliu.akka.test.utils.Faker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class BackendRouterActorTest {

    private ActorSystem system;

    @Before
    public void setUp() throws Exception {
        system = ActorSystem.create("test-system");
    }

    @After
    public void tearDown() throws Exception {
        system.terminate();
    }

    @Test
    public void forwardExecuteCommandCmdToBackend() {
        TestKit probe = new TestKit(system);
        UUID correlationId = Faker.fakeId();

        ActorRef backendRouter = system.actorOf(BackendRouterActor.props(probe.getRef()));

        BackendProtocol.ExecuteCommandCmd expectedCmd = new BackendProtocol.ExecuteCommandCmd(correlationId, "getAddress");
        backendRouter.tell(expectedCmd, ActorRef.noSender());

        probe.expectMsg(expectedCmd);
    }
}