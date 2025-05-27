import static org.junit.jupiter.api.Assertions.*;

import bgu.spl.mics.*;
import bgu.spl.mics.application.Messages.Broadcasts.TickBroadcast;
import bgu.spl.mics.application.Messages.Events.DetectObjectEvent;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageBusTest {
    //OUT-object under test
    MessageBusImpl msgBus;
    MicroService service1;
    MicroService service2;


    /*this methodTest initialize the object under test*/
    @BeforeEach
    public void setUp() {
        msgBus=MessageBusImpl.getInstance();
        service1=new MicroService("service1") {
            @Override
            protected void initialize() {}
        };
        service2=new MicroService("service2") {
            @Override
            protected void initialize() {}
        };
        msgBus.register(service1);
        msgBus.register(service2);
    }

    @AfterEach
    public void tearDown() {
        msgBus.unregister(service1);
        msgBus.unregister(service2);
        msgBus = null;
    }

    //subscribeEvent Tests
    @Test
    public void subscribeEventValidityTest(){
        DetectObjectEvent e = new DetectObjectEvent(new StampedDetectedObjects(0));
        msgBus.subscribeEvent(e.getClass(),service1);
        int size= msgBus.getQueueOfEvent(e.getClass()).size();
        msgBus.subscribeEvent(e.getClass(),service2);
        assertEquals(size+1,msgBus.getQueueOfEvent(e.getClass()).size());
    }


    //subscribeBroadcast Tests
    @Test
    public void SubscribeBroadcastNonNullTest() {
        TickBroadcast broadcast = new TickBroadcast();
        msgBus.subscribeBroadcast(broadcast.getClass(), service1);
        ConcurrentLinkedQueue<Class<? extends Broadcast>> queue = msgBus.getQueueMicroBroad(service1);
        assertNotNull(queue);
    }

    @Test
    public void subscribeBroadcastValidityTest() {
        TickBroadcast broadcast = new TickBroadcast();
        msgBus.subscribeBroadcast(broadcast.getClass(), service1);
        int size= msgBus.getQueueOfBroad(broadcast.getClass()).size();
        msgBus.subscribeBroadcast(broadcast.getClass(), service2);
        assertEquals(size+1,msgBus.getQueueOfBroad(broadcast.getClass()).size());
    }

    //awaitMessage Tests
    @Test
    public void awaitMessageTest() throws InterruptedException {
        DetectObjectEvent event = new DetectObjectEvent(new StampedDetectedObjects(0));
        msgBus.subscribeEvent(DetectObjectEvent.class, service1);
        msgBus.sendEvent(event);
        Message message = msgBus.awaitMessage(service1);
        assertEquals(event, message);
    }

    @Test
    public void awaitMessageOrderTest() throws InterruptedException {
        DetectObjectEvent event1 = new DetectObjectEvent(new StampedDetectedObjects(0));
        msgBus.subscribeEvent(DetectObjectEvent.class, service1);
        msgBus.subscribeEvent(DetectObjectEvent.class, service2);
        msgBus.sendEvent(event1);
        Message message = msgBus.awaitMessage(service1);
        assertEquals(event1, message);
        assertEquals(0,msgBus.getQueueMicroAll(service2).size());
    }

}
