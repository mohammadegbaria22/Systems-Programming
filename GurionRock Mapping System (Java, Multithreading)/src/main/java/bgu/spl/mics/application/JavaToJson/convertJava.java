package bgu.spl.mics.application.JavaToJson;

import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.StatisticalFolder;
import com.google.gson.Gson;

import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class convertJava {

    private int systemRuntime;
    private int numDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;
    Map<String, LandMark> landMarks;


    public int getSystemRuntime(){return systemRuntime;}
    public int getNumDetectedObjects(){return numDetectedObjects;}
    public int getNumTrackedObjects(){return numTrackedObjects;}
    public int getNumLandmarks(){return numLandmarks;}
    public Map<String,LandMark> getLandMarks(){return landMarks;}

    public void setSystemRuntime(int num){systemRuntime = num;}
    public void setNumDetectedObjects(int num){numDetectedObjects = num;}
    public void setNumTrackedObjects(int num){numTrackedObjects = num;}
    public void setLandMarks(int num){numLandmarks = num;}
    public void setLandMarks(Map<String,LandMark> map){landMarks=map;}




}


