package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.JavaToJson.convertJavaCrash;
import bgu.spl.mics.application.Messages.Broadcasts.CrashedBroadcast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    private int time;
    private List<DetectedObject> detectedObjects;

    public StampedDetectedObjects(int time) {
        this.time = time;
        this.detectedObjects = Collections.synchronizedList(new ArrayList<>());
    }

    public int getTime() {return time;}
    public int getSize(){return detectedObjects.size();}
    public void setTime(int num){time = num;}



    //////methods_for_cameraService/////////////
    public boolean isIdError(){
        for (DetectedObject o : detectedObjects){
            if (o.getId().equals("ERROR")){
                return true;}
            else {
                //update the numOfDetectedObjects
                StatisticalFolder.getInstance().incrementNumberOfDetectedObjects(1);
            }
        }
        return false;
    }

    //create a trackedObject with it's cloudPoints for each DetectedObject
    public List<TrackedObject> makeTrackedObj(){
        List<TrackedObject> result = new ArrayList<>();

        for (DetectedObject d : detectedObjects){
            StampedCloudPoints points = LiDarDataBase.getObject(d.getId(), time);

            //create tracked object for the detected object
            TrackedObject trackedObject = new TrackedObject(d.getId(), time, d.getDescription());

            List<CloudPoint> cloudPoints = points.getCloudPoints(); //points of the object
            for (CloudPoint p : cloudPoints) {
                trackedObject.addCoordinate(p);
            }

            result.add(trackedObject);
        }
        return result;
    }
}
