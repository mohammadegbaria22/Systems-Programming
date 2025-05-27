package bgu.spl.mics.application.jsonToJava;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cameras {
    private List<Camera> CamerasConfigurations;
    private String camera_datas_path;

    public Cameras(){
        CamerasConfigurations = Collections.synchronizedList(new ArrayList<>());
        camera_datas_path="";
    }
    // Getters and setters
    public List<Camera> getCamerasConfigurations() { return CamerasConfigurations; }
    public String getCamera_datas_path() { return camera_datas_path; }

    public void toStringCamera() {
        for (Camera c : CamerasConfigurations){
            System.out.println(c);
        }
    }

}
