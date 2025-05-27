package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    private static class SingletonHolder {
        private static StatisticalFolder instance = new StatisticalFolder();
    }

    public StatisticalFolder(){
        systemRuntime = new AtomicInteger(0);
        numberOfDetectedObjects = new AtomicInteger(0);
        numberOfTrackedObjects = new AtomicInteger(0);
        numberOfLandmarks = new AtomicInteger(0);
    }

    public static StatisticalFolder getInstance() {
        return SingletonHolder.instance;
    }

    private AtomicInteger systemRuntime;
    private AtomicInteger numberOfDetectedObjects;
    private AtomicInteger numberOfTrackedObjects;
    private AtomicInteger numberOfLandmarks;

    public AtomicInteger getSystemRuntime() {return systemRuntime;}
    public AtomicInteger getNumberOfDetectedObjects() {return numberOfDetectedObjects;}
    public AtomicInteger getNumberOfTrackedObjects() {
        return numberOfTrackedObjects;
    }
    public AtomicInteger getNumberOfLandmarks() {return numberOfLandmarks;}

    //incrementMethods
    public void incrementSystemRuntime() {systemRuntime.incrementAndGet();}
    public void incrementNumberOfDetectedObjects(int num) {numberOfDetectedObjects.addAndGet(num);;}
    public void incrementNumberOfLandmarks() {numberOfLandmarks.incrementAndGet();}
    public void incrementNumberOfTrackedObjects(int size) {numberOfTrackedObjects.addAndGet(size);}
}
