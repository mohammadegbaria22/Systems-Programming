package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.JavaToJson.convertJavaCrash;
import bgu.spl.mics.application.Messages.Broadcasts.CrashedBroadcast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */

//represents a Lidar//
public class LiDarWorkerTracker {

    private int id;
    private int frequency;
    private STATUS status;
    private List<TrackedObject> lastTrackedObjects;

    public LiDarWorkerTracker(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        status = STATUS.UP;
        lastTrackedObjects = Collections.synchronizedList(new ArrayList<>());
    }

    public int getId() {return id;}
    public void add(TrackedObject obj){lastTrackedObjects.add(obj);}
    public STATUS getStatus() {return status;}
    public void setStatus(STATUS status) {this.status = status;}
    public int getFrequency() {return frequency;}
    public List<TrackedObject> getList() {
        return lastTrackedObjects;
    }


    //methods_for_LidarService//////
    public void clearList() {lastTrackedObjects.clear();}

    public void addTrackedObj(TrackedObject t){
        lastTrackedObjects.add(t);
    }


    public void crashLidar(String name){
        convertJavaCrash.getInstance().addLastLiDarWorkerTrackersFrame(name ,lastTrackedObjects);
    }

    public boolean isCrashLidar( int time) {
        StampedCloudPoints point_crash = LiDarDataBase.getObject("ERROR", time);
        if (point_crash != null) {
            convertJavaCrash.getInstance().setError("Lidar disconnected");
            convertJavaCrash.getInstance().setFaultySensor("LidarWorkerTracker" + id);
            convertJavaCrash.getInstance().addLastLiDarWorkerTrackersFrame("LidarWorkerTracker" + id, lastTrackedObjects);
            GlobalCrashed.getInstance().setCrash();
            return true;
        }
        return false;
    }
}


