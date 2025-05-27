package bgu.spl.mics.application.objects;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */

public class GPSIMU {
    private STATUS status;
    private List<Pose> list;

    public GPSIMU() {
        this.status = STATUS.UP;
        this.list = Collections.synchronizedList(new ArrayList<>());
    }

    public void add(Pose obj){list.add(obj);}
    public STATUS getStatus() {return status;}
    public void setStatus(STATUS status) {this.status = status;}
    public List<Pose> getList(){return list;}


    public Pose findPose (int time) {
        Pose pose;
        for (int i = 0; i < list.size(); i++) {
            if (time == list.get(i).getTime()) {
                pose = list.get(i);
                return pose;
            }
        }
        return null;
    }
}
