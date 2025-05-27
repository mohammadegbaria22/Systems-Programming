package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    private int time;
    private String id;
    private List<CloudPoint> coordinates;
    private String description;

    public TrackedObject(String id, int time, String description) {
        this.time = time;
        this.id = id;
        this.coordinates = Collections.synchronizedList(new ArrayList<>());
        this.description = description;
    }

    public String getId() {return id;}
    public int getTime() {return time;}
    public String getDescription() {return description;}
    public void addCoordinate(CloudPoint point) {coordinates.add(point);}
    public List<CloudPoint> getCoordinates() {return coordinates;}
}
