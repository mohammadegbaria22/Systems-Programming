package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.JavaToJson.convertJava;
import bgu.spl.mics.application.Messages.Broadcasts.TerminatedBroadcast;
import bgu.spl.mics.application.Messages.Broadcasts.TickBroadcast;
import bgu.spl.mics.application.objects.GlobalCrashed;
import bgu.spl.mics.application.objects.GlobalTime;
import bgu.spl.mics.application.objects.StatisticalFolder;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import static bgu.spl.mics.application.GurionRockRunner.*;


/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    /////fields/////
    private int tickTime;
    private int duration;
    ////////////////


    /**
     * Constructor for TimeService.
     *
     * @param TickTime The duration of each tick in milliseconds.
     * @param Duration The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        this.tickTime = TickTime;
        this.duration = Duration;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        GlobalTime globalTime = GlobalTime.getInstance();

        try {
            while (globalTime.getGlobalTime() < duration && !GlobalCrashed.getInstance().getCrahs() && !GlobalCrashed.getInstance().getStop()) { //if sensor crashed stop the run
                TickBroadcast t = new TickBroadcast();
                globalTime.increaseGlobaltime(1);
                sendBroadcast(t);
                Thread.sleep(tickTime *1000);
                StatisticalFolder.getInstance().incrementSystemRuntime();

            }
        } catch (Exception e) {
            Thread.currentThread().interrupt(); // Restore the interrupt status
        }


        //if a sensor caused a Crashed
        if (GlobalCrashed.getInstance().getCrahs()){
            crashedToJson();
            terminate();
        }

        //no sensor crashed during the simulation
        else {
            convertJava c = resultFunction();
            convertToJson(c);
            sendBroadcast(new TerminatedBroadcast());
            terminate();
        }
    }
}
