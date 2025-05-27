package bgu.spl.mics.application.jsonToJava;

import bgu.spl.mics.application.objects.TrackedObject;

import java.util.List;

public class lidarTrackedObjects {
    List<TrackedObject> list;

    public void setList(List<TrackedObject> list) {
        this.list = list;
    }
    public List<TrackedObject> getList(){
        return list;
    }
}
