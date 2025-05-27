package bgu.spl.mics.application.JavaToJson;

import bgu.spl.mics.application.objects.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public  class convertJavaCrash {

    private static convertJavaCrash instance;

    public static convertJavaCrash getInstance(){
        if(instance==null){
            instance=new convertJavaCrash();
        }
        return instance;
    }



    private String error;
    private String faultySensor;
    private  Map<String,StampedDetectedObjects> lastCamerasFrame;
    private Map<String , List<TrackedObject>> lastLiDarWorkerTrackersFrame;
    private List<Pose> poses;
    private Statistics statistics;

    convertJavaCrash(){
        lastLiDarWorkerTrackersFrame = new ConcurrentHashMap<>();
        lastCamerasFrame = new ConcurrentHashMap<>();
    }


    public String getError() {
        return error;
    }

    public String getFaultySensor() {
        return faultySensor;
    }

    public StampedDetectedObjects getLastCamerasFrame(String name) {
        return lastCamerasFrame.get(name);
    }

    public List<TrackedObject> getLastLiDarWorkerTrackersFrame(String name) {
        return lastLiDarWorkerTrackersFrame.get(name);
    }

    public List<Pose> getPoses() {
        return poses;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setFaultySensor(String faultySensor) {
        this.faultySensor = faultySensor;
    }

    public void setLastCamerasFrame(String name , StampedDetectedObjects s) {
        lastCamerasFrame.put(name , s);
    }

    public void addLastLiDarWorkerTrackersFrame(String name , List<TrackedObject> list) {
        lastLiDarWorkerTrackersFrame.put(name , list);
    }

    public void setPoses(List<Pose> poses) {
        this.poses = poses;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }
}
