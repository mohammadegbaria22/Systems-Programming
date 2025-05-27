package bgu.spl.mics.application.services;
import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.JavaToJson.convertJavaCrash;
import bgu.spl.mics.application.Messages.Broadcasts.*;
import bgu.spl.mics.application.Messages.Events.DetectObjectEvent;
import bgu.spl.mics.application.objects.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;


/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 *
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    /// //fields/////
    private Camera camera;
    StampedDetectedObjects lastObj;
    private int limit;
    ////////////////

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("camera " + camera.get_id());
        this.camera = camera;
        limit = camera.getListSize();
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {

        subscribeBroadcast(TickBroadcast.class, callback -> {

            //if no other service is crashed
            if (!GlobalCrashed.getInstance().getCrahs()){

                //sends detected event at tick + camera frequency
                StampedDetectedObjects stampObj = camera.getStampWithFrequency();

                //if no stamped object at this time - exit
                if (stampObj == null){return;}
                limit --;


                //in case of an ERROR in the camera
                if (stampObj.isIdError()){
                    convertJavaCrash.getInstance().setError("Camera disconnected");
                    convertJavaCrash.getInstance().setFaultySensor(camera.getCamera_key());
                    convertJavaCrash.getInstance().setLastCamerasFrame(camera.getCamera_key() , lastObj);
                    GlobalCrashed.getInstance().setCrash(); //tell other sensors there is a crash!
                    sendBroadcast(new CrashedBroadcast());
                    terminate();
                    camera.setStatus(STATUS.ERROR);
                    return; //get out of the functions
                }

                //no ERROR in the camera
                DetectObjectEvent event = new DetectObjectEvent(stampObj);


                //maintain the last object the camera detected
                lastObj = stampObj;
                sendEvent(event);

                if(limit==0){
                    sendBroadcast(new CameraDoneBroadcast());
                    sendBroadcast(new DoneBroadcast());
                    terminate();
                }

            }


            //if other sensor crashed - update last object detected by the camera and terminate
            else {
                convertJavaCrash.getInstance().setLastCamerasFrame(camera.getCamera_key() , lastObj);
                terminate();}
            }
        );


        subscribeBroadcast(TerminatedBroadcast.class, callback -> {
            terminate();
                }
        );


        subscribeBroadcast(CrashedBroadcast.class, callback -> {
            //if other sensor crashed - update last object detected by the camera
            convertJavaCrash.getInstance().setLastCamerasFrame(camera.getCamera_key() , lastObj);
            terminate();
        });


    }
}
