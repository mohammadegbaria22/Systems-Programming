package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.JavaToJson.convertJavaCrash;
import bgu.spl.mics.application.JavaToJson.Statistics;
import bgu.spl.mics.application.Messages.Broadcasts.CrashedBroadcast;
import bgu.spl.mics.application.Messages.Broadcasts.DoneBroadcast;
import bgu.spl.mics.application.Messages.Broadcasts.TerminatedBroadcast;
import bgu.spl.mics.application.Messages.Broadcasts.TickBroadcast;
import bgu.spl.mics.application.Messages.Events.PoseEvent;
import bgu.spl.mics.application.Messages.Events.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 *
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    /// //fields/////
    private FusionSlam fusionSlam;
    private List<TrackedObjectsEvent> trackedEvents;
    private int counter;
    ///////////////

    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam, int counter) {
        super("FusionSlam");
        this.fusionSlam = fusionSlam;
        this.trackedEvents = Collections.synchronizedList(new ArrayList<>());
        this.counter = counter;
    }



    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {

        subscribeEvent(TrackedObjectsEvent.class, event -> {

            //insures no other service is crashed
            if (!GlobalCrashed.getInstance().getCrahs()) {
                if (fusionSlam.isAvailablePose(event.getTime())) {

                    List<TrackedObject> trackedObjects = event.getTrackedObjects();
                    fusionSlam.trackedObjectsToLandMarks(trackedObjects);
                    complete(event, true);
                } else {
                    addTrackedObjectEvent(event);
                }
            }

        });


        subscribeEvent(PoseEvent.class, event -> {
            if (!GlobalCrashed.getInstance().getCrahs()) {
                Pose pose = event.getPose();
                fusionSlam.addPose(pose);

                List<TrackedObjectsEvent> lst = getTrackedEventsList();

                Iterator<TrackedObjectsEvent> iterator = lst.iterator();
                while (iterator.hasNext()) {
                    TrackedObjectsEvent t = iterator.next(); // Get the next event
                    int time = t.getTime();

                    if (fusionSlam.isAvailablePose(time)) {
                        List<TrackedObject> trackedObjects = t.getTrackedObjects();
                        fusionSlam.trackedObjectsToLandMarks(trackedObjects);
                        complete(event, true);
                    }
                    // Safely remove the event from the list after processing
                    iterator.remove();
                }

            } else {
                Statistics s = new Statistics();
                s.setStatisticalFolder(StatisticalFolder.getInstance());
                s.setLandMarks(fusionSlam.getLandMarks());
                convertJavaCrash.getInstance().setStatistics(s);
                terminate();
            }
        });

        subscribeBroadcast(DoneBroadcast.class, callback -> {
            counter--;
            if (counter == 2) {
                GlobalCrashed.getInstance().setStop();
                Statistics s = new Statistics();
                s.setStatisticalFolder(StatisticalFolder.getInstance());
                s.setLandMarks(fusionSlam.getLandMarks());
                convertJavaCrash.getInstance().setStatistics(s);
                terminate();
            }
        });


        subscribeBroadcast(TerminatedBroadcast.class, callback -> {
                    terminate();
                }
        );

        subscribeBroadcast(CrashedBroadcast.class, callback -> {
                    Statistics s = new Statistics();
                    s.setStatisticalFolder(StatisticalFolder.getInstance());
                    s.setLandMarks(fusionSlam.getLandMarks());
                    convertJavaCrash.getInstance().setStatistics(s);
                    terminate();
                }
        );
    }

    ///methods///
    private void addTrackedObjectEvent(TrackedObjectsEvent obj) {
        trackedEvents.add(obj);
    }
    private List<TrackedObjectsEvent> getTrackedEventsList() {
        return trackedEvents;
    }

}