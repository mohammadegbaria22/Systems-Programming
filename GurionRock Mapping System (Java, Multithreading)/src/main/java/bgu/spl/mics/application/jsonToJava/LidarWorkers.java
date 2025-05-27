package bgu.spl.mics.application.jsonToJava;

import bgu.spl.mics.application.objects.LiDarWorkerTracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LidarWorkers {
    private List<LiDarWorkerTracker> LidarConfigurations;
    private String lidars_data_path;

    public LidarWorkers(){
        LidarConfigurations = Collections.synchronizedList(new ArrayList<>());
        lidars_data_path = "";
    }

    // Getters
    public List<LiDarWorkerTracker> getLidarConfigurations() { return LidarConfigurations; }
    public String getLidars_data_path() { return lidars_data_path; }

}
