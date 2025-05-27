package bgu.spl.mics.application.jsonToJava;

public class Configuration {
    private Cameras Cameras;
    private LidarWorkers LiDarWorkers;
    private String poseJsonFile;
    private int TickTime;
    private int Duration;

    // Getters and setters
    public Cameras getCameras() {
        return Cameras;
    }

    public LidarWorkers getLidarWorkers() {
        return LiDarWorkers;
    }

    public String getPoseJsonFile() {
        return poseJsonFile;
    }

    public int getTickTime() {
        return TickTime;
    }

    public int getDuration() {return Duration;}

}
