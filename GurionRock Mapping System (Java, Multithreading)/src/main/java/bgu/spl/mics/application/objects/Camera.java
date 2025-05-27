package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.Messages.Events.DetectObjectEvent;

import java.util.*;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Semaphore;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    int id;
    int frequency;
    String camera_key;
    STATUS status;
    List<StampedDetectedObjects> list;


    public Camera(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        status = STATUS.UP;
        list = Collections.synchronizedList(new ArrayList<>());
    }

    public int get_id() {
        return id;
    }

    public String getCamera_key() {
        return camera_key;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public void clearList() {
        list.clear();
    }

    public void add(StampedDetectedObjects obj) {
        list.add(obj);
    }

    public StampedDetectedObjects get(int time) {
        for (StampedDetectedObjects obj : list) {
            if (obj.getTime() == time) {
                return obj;
            }
        }
        return null;
    }

    public List<StampedDetectedObjects> getList() {
        return list;
    }

    public void setList(List<StampedDetectedObjects> newlist) {
        list = newlist;
    }

    public String toString() {
        return "Camera id: " + id + " with frequency: " + frequency;
    }


    /// ////methods_for_CameraService////////
    /*
     * @PRE:The `list` must not be null and must contain valid `StampedDetectedObjects` instances.
     * @POST:Returns the first `StampedDetectedObjects` instance satisfying the condition, or null if none match.
     * @return The `StampedDetectedObjects` with a matching timestamp, or null.
     * */
    public StampedDetectedObjects getStampWithFrequency() {
        for (StampedDetectedObjects s : list) {
            if (GlobalTime.getInstance().getGlobalTime() == s.getTime() + frequency) {
                return s;
            }
        }
        return null;
    }

    public int getListSize() {
        return list.size();
    }
}