package bgu.spl.mics.application.JavaToJson;

import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

import java.util.List;
import java.util.Map;

public class cameraCrash {
    private String error;
    private String faultySensor;
    private Map<String, StampedDetectedObjects> lastCamerasFrame;
    private Map<String, LiDarWorkerTracker> lastLiDarWorkerTrackersFrame;
    private List<Pose> poses;
    private convertJava statistics;


    public String getError(){return error;}
    public String getFaultySensor(){return faultySensor;}
    public Map<String ,StampedDetectedObjects> getLastCamerasFrame(){return lastCamerasFrame;}
    public Map<String , LiDarWorkerTracker> getLastLiDarWorkerTrackersFrame(){return lastLiDarWorkerTrackersFrame;}
    public List<Pose> getPoses(){return poses;}
    public convertJava getStatistics(){return statistics;}

    public void setError(String error) {
        this.error = error;
    }
    public void setFaultySensor(String faultySensor){this.faultySensor = faultySensor;}

    public void setLastCamerasFrame(Map<String, StampedDetectedObjects> lastCamerasFrame) {
        this.lastCamerasFrame = lastCamerasFrame;
    }

    public void setLastLiDarWorkerTrackersFrame(Map<String, LiDarWorkerTracker> lastLiDarWorkerTrackersFrame) {
        this.lastLiDarWorkerTrackersFrame = lastLiDarWorkerTrackersFrame;
    }

    public void setPoses(List<Pose> poses) {
        this.poses = poses;
    }

    public void setStatistics(convertJava statistics) {
        this.statistics = statistics;
    }
}