package bgu.spl.mics.application.Messages.Events;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

import java.util.List;

public class DetectObjectEvent implements Event<Boolean> {

    /////fields/////
    private StampedDetectedObjects obj;
    private String id;

    /////////////////

    public DetectObjectEvent(StampedDetectedObjects obj ) {
        this.obj = obj;
    }

    public String getId() {return id;}
    public StampedDetectedObjects getObj() {return obj;}


}
