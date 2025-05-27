package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.Messages.Events.TrackedObjectsEvent;
import bgu.spl.mics.application.services.FusionSlamService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    // Singleton instance holder
    private static class FusionSlamHolder {
        private static FusionSlam instance = new FusionSlam();
    }

    private List<LandMark> landmarks;
    private List<Pose> poses;
    private List<TrackedObject> trackedObjects;

    public FusionSlam() {
        landmarks = Collections.synchronizedList(new ArrayList<>());
        poses = Collections.synchronizedList(new ArrayList<>());
        trackedObjects = Collections.synchronizedList(new ArrayList<>());
    }

    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }

    //@PRE:none.
    //@POST:landmarks.size() == @PRE(landmarks.size()) + 1
    public void addLandMark(LandMark landMark) {
        landmarks.add(landMark);
    }

    public void addPose(Pose pose) {
        poses.add(pose);
    }

    public List<LandMark> getLandMarks() {
        return landmarks;
    }

    public List<TrackedObject> getObjects() {
        return trackedObjects;
    }

    public boolean isObjectAvailable(TrackedObject instance) {
        for (LandMark obj : landmarks) {
            if (obj.getId().equals(instance.getId())) {
                return true;
            }
        }
        return false;
    }

    public LandMark getLandMArk(String id) {
        for (LandMark LM : landmarks) {
            if (LM.getId().equals(id)) {
                return LM;
            }
        }
        return null;
    }

    public void addTrackedObj(TrackedObject obj) {
        trackedObjects.add(obj);
    }

    public Pose getPose(int tick) {
        for (Pose pose : poses) {
            if (pose.getTime() == tick) {
                return pose;
            }
        }
        return null;
    }

    public boolean isAvailablePose(int time) {
        for (Pose pose : poses) {
            if (pose.getTime() == time) {
                return true;
            }
        }
        return false;
    }

    public CloudPoint mathCalc(double x, double y, Pose pose) {
        double xLocal = x;
        double yLocal = y;
        double deltaDegree = pose.getYaw();
        double xGlobal;
        double yGlobal;
        double xRobot = pose.getX();
        double yRobot = pose.getY();
        double xRotated;
        double yRotated;

        double deltaRad = deltaDegree * ((double) (Math.PI / 180));

        double cos = Math.cos(deltaRad);
        double sin = Math.sin(deltaRad);

        xRotated = (cos * xLocal) - (sin * yLocal);
        yRotated = (sin * xLocal) + (cos * yLocal);

        xGlobal = xRotated + xRobot;
        yGlobal = yRotated + yRobot;

        CloudPoint globalPoint = new CloudPoint(xGlobal, yGlobal);
        return globalPoint;
    }


    //@PRE:for(obj in trackedObjects): obj.time >= 1
    //@POST:landmarks.size() >= @PRE(landmarks.size()) > 0
    public void trackedObjectsToLandMarks(List<TrackedObject> trackedObjects) {
        for (TrackedObject obj : trackedObjects) {
            if (obj.getTime() < 1) {
                throw new IllegalArgumentException("time of tracked object must be greater than 0!");
            }
            Pose poseObj = getPose(obj.getTime());
            List<CloudPoint> globalPointsList = new ArrayList<>();

            for (CloudPoint localPoint : obj.getCoordinates()) {
                double x = localPoint.getX();
                double y = localPoint.getY();
                CloudPoint globalPoint = mathCalc(x, y, poseObj);
                globalPointsList.add(globalPoint);
            }

            //case1 - object was never detected before - checks if he is a landMark
            if (!FusionSlam.getInstance().isObjectAvailable(obj)) {
                LandMark landMark = new LandMark(obj.getId(), obj.getDescription(), globalPointsList);
                addLandMark(landMark);
                StatisticalFolder.getInstance().incrementNumberOfLandmarks();

            } else { //case2 - object was detected before - we do average
                LandMark landMark = FusionSlam.getInstance().getLandMArk(obj.getId());
                List<CloudPoint> gPoints = landMark.getList();

                for (int i = 0; i < Math.min(gPoints.size(), globalPointsList.size()); i += 1) {

                    double newX = averageX(gPoints.get(i).getX(), globalPointsList.get(i).getX());
                    double newY = averageY(gPoints.get(i).getY(), globalPointsList.get(i).getY());

                    gPoints.get(i).setX(newX);
                    gPoints.get(i).setY(newY);

                }

                //in case we found that the cloudPointList not size equaled as in example_input_2!!
                List<CloudPoint> bigger = biggerObj(obj.getCoordinates(), gPoints);

                if (bigger.size() == obj.getCoordinates().size() && bigger.size() != gPoints.size()) {
                    List<CloudPoint> l = new ArrayList<>();


                    for (int j = Math.min(gPoints.size(), globalPointsList.size()); j < bigger.size(); j += 1) {
                        CloudPoint newPoint = bigger.get(j);
                        l.add(newPoint);
                    }

                    for (CloudPoint p : l) {
                        gPoints.add(p);
                    }
                }
            }
        }
    }

    public double averageX(double oldP, double newP) {
        return (oldP + newP) / 2.0;
    }

    public double averageY(double oldP, double newP) {
        return (oldP + newP) / 2.0;
    }


    public List<CloudPoint> biggerObj(List<CloudPoint> o1, List<CloudPoint> o2) {
        if (o1.size() >= o2.size()) {
            return o1;
        }
        return o2;
    }
}

