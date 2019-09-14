import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.dliu.akka.lab.requestreplydemo.actors.BackendEchoActor;
import com.dliu.akka.lab.requestreplydemo.actors.BackendProtocol;
import com.dliu.akka.test.utils.Faker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class BackendEchoActorTest {

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
    public void processRPCRequest() {
        TestKit probe = new TestKit(system);
        UUID correlationId = Faker.fakeId();
        BackendProtocol.ExecuteCommandResponse expectedResponse = new BackendProtocol.ExecuteCommandResponse(correlationId, "processed getUserId");

        ActorRef backendActor = system.actorOf(BackendEchoActor.props(), "backend-actor");
        BackendProtocol.ExecuteCommandCmd requestCmd = new BackendProtocol.ExecuteCommandCmd(correlationId, "getUserId");


        backendActor.tell(requestCmd, probe.getRef());

        probe.expectMsg(expectedResponse);
    }
}