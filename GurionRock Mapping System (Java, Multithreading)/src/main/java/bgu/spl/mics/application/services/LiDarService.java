package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Event;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.JavaToJson.convertJavaCrash;
import bgu.spl.mics.application.Messages.Broadcasts.*;
import bgu.spl.mics.application.Messages.Events.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.Messages.Events.DetectObjectEvent;

import javax.sound.midi.Soundbank;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;


/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {

    /////fields/////
    LiDarWorkerTracker lidar;
    List<TrackedObjectsEvent> tEvents;
    private int counter;
    ////////////////

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker  , int counter) {
        super("Lidar" + LiDarWorkerTracker.getId());
        this.lidar = LiDarWorkerTracker;
        tEvents = Collections.synchronizedList(new ArrayList<>());
        this.counter = counter;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {

        subscribeEvent(DetectObjectEvent.class , event -> {
            lidar.clearList();

            //insures no other service is crashed
            if (!GlobalCrashed.getInstance().getCrahs()) {


                StampedDetectedObjects stampedObjects = event.getObj();
                int stampTime = stampedObjects.getTime();


                //check if lidar crashed
                boolean sign = lidar.isCrashLidar(stampTime);
                if (sign){
                    sendBroadcast(new CrashedBroadcast());
                    lidar.setStatus(STATUS.ERROR);
                    terminate();
                    return;
                }


                //returns a list of trackedObjects to send TrackedObjectEvent
                List<TrackedObject> tObjects = stampedObjects.makeTrackedObj();


                //adding each trackedObject to the fusionSlam & lidarList
                for (TrackedObject trackedObject : tObjects) {
                    lidar.addTrackedObj(trackedObject);
                    FusionSlam.getInstance().addTrackedObj(trackedObject);
                }


                //create trackedObjectEvent and add it to the list waiting for his correct time to be sent
                TrackedObjectsEvent toe = new TrackedObjectsEvent(tObjects , stampTime);
                tEvents.add(toe);

                complete(event,true);

        }
            else {
                lidar.crashLidar("LidarWorkerTracker" + lidar.getId());
        terminate();}});




        subscribeBroadcast(TickBroadcast.class, callback -> {

            //insures no other service is crashed
            if (!GlobalCrashed.getInstance().getCrahs()) {

                Iterator<TrackedObjectsEvent> iterator = tEvents.iterator(); // Using an iterator for safe removal
                while (iterator.hasNext()) {
                    TrackedObjectsEvent t = iterator.next(); // Get the next element
                    List<TrackedObject> lstOfTrackedObjects = t.getTrackedObjects();
                    int sizeOfList = lstOfTrackedObjects.size();

                    if (sizeOfList > 0 && t.getTime() + lidar.getFrequency() <= GlobalTime.getInstance().getGlobalTime()) {
                        sendEvent(t);
                        StatisticalFolder.getInstance().incrementNumberOfTrackedObjects(sizeOfList);
                        iterator.remove(); //remove the element to avoid sending it again
                    }

                }
                if(counter==0) {
                    terminate();
                    sendBroadcast(new DoneBroadcast());
                }
            }

            else {
                //set the lastTrackedObj
                lidar.crashLidar("LidarWorkerTracker" + lidar.getId());
                terminate();}
        });



        subscribeBroadcast(TerminatedBroadcast.class, callback ->{
            terminate();
                }
        );



        subscribeBroadcast(CrashedBroadcast.class, callback ->{
            lidar.crashLidar("LidarWorkerTracker" + lidar.getId());
            terminate();}

        );

        subscribeBroadcast(CameraDoneBroadcast.class, callback -> {
            counter--;
        });

    }


}
