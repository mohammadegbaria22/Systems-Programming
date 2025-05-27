package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.JavaToJson.convertJavaCrash;
import bgu.spl.mics.application.Messages.Broadcasts.CrashedBroadcast;
import bgu.spl.mics.application.Messages.Broadcasts.DoneBroadcast;
import bgu.spl.mics.application.Messages.Broadcasts.TerminatedBroadcast;
import bgu.spl.mics.application.Messages.Broadcasts.TickBroadcast;
import bgu.spl.mics.application.Messages.Events.PoseEvent;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.GlobalCrashed;
import bgu.spl.mics.application.objects.GlobalTime;
import bgu.spl.mics.application.objects.Pose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    /// //fields/////
    private GPSIMU gpsimu;
    List<Pose> poses;
////////////////


    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("GPSIMU");
        this.gpsimu = gpsimu;
        this.poses = Collections.synchronizedList(new ArrayList<>());

    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {

        subscribeBroadcast(TickBroadcast.class, callback -> {

            //insures no other service is crashed
            if (!GlobalCrashed.getInstance().getCrahs()) {

                if (gpsimu.findPose(GlobalTime.getInstance().getGlobalTime()) != null) {
                    Pose pose;
                    pose = gpsimu.findPose(GlobalTime.getInstance().getGlobalTime());
                    poses.add(pose);
                    PoseEvent event = new PoseEvent(pose);
                    sendEvent(event);
                }
                else {
                    sendBroadcast(new DoneBroadcast());
                    terminate();
                }
            }

            else {
                convertJavaCrash.getInstance().setPoses(poses);
                terminate();
            }
        });


        subscribeBroadcast(TerminatedBroadcast.class, callback -> {
            terminate();
        });


        subscribeBroadcast(CrashedBroadcast.class, callback -> {
                    convertJavaCrash.getInstance().setPoses(poses);
                    terminate();
                }
        );
    }
}
