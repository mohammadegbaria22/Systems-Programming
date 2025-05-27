package bgu.spl.mics.application.objects;

import java.util.List;

public class LidarJson {
    private int time;
    private String id;
    private List<List<Double>> cloudPoints;

    // Getters and Setters
    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<List<Double>> getCloudPoints() {
        return cloudPoints;
    }

    public String toString(){
        return "obj detected at time " + time + " with id " + id + " and cloud points " + cloudPoints;
    }
}

