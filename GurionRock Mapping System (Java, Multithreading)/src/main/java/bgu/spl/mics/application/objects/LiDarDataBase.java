package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;


/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private static class SingletonHolder{
        private static LiDarDataBase instance = new LiDarDataBase();
    }

    private static List<StampedCloudPoints> list;


    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
   public void lidarInitialize (String filePath) {
            list = Collections.synchronizedList(new ArrayList<>());
            try (FileReader reader = new FileReader(filePath)) {

                Gson gson = new Gson();
                Type lst = new TypeToken<List<LidarJson>>() {}.getType();

                // Parse the JSON string into a list of LidarJsonEntry objects
                ArrayList<LidarJson> lidarJsonEntries = gson.fromJson(reader, lst);

                for (LidarJson entry : lidarJsonEntries) {
                    StampedCloudPoints stampedPoint = new StampedCloudPoints(entry.getTime(), entry.getId());
                    for (List<Double> point : entry.getCloudPoints()) {
                        CloudPoint cloudPoint = new CloudPoint(point.get(0), point.get(1));
                        stampedPoint.addCloudPoint(cloudPoint);
                    }
                    list.add(stampedPoint);
                }
            } catch (Exception e) {
                System.out.println("im in extension");
            }
    }

    public static LiDarDataBase getInstance(){
       return SingletonHolder.instance;
    }

    public static StampedCloudPoints getObject(String id, int time) {
        for (StampedCloudPoints obj : list) {
            if (obj.getId().equals(id) && obj.getTime() == time) {
                return obj;
            }
        }
        return null;
    }
}








//package bgu.spl.mics.application.objects;
//
//import java.io.FileReader;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//
//import java.lang.reflect.Type;
//
//
///**
// * LiDarDataBase is a singleton class responsible for managing LiDAR data.
// * It provides access to cloud point data and other relevant information for tracked objects.
// */
//public class LiDarDataBase {
//
//
//    private static LiDarDataBase instance;
//    private static List<StampedCloudPoints> list;
//
//
//    /**
//     * Returns the singleton instance of LiDarDataBase.
//     *
//     * @param filePath The path to the LiDAR data file.
//     * @return The singleton instance of LiDarDataBase.
//     */
//    public static LiDarDataBase getInstance(String filePath) {
//        if (instance == null) {
//            instance = new LiDarDataBase();
//            list = Collections.synchronizedList(new ArrayList<>());
//            try (FileReader reader = new FileReader(filePath)) {
//
//                Gson gson = new Gson();
//                Type lst = new TypeToken<List<LidarJson>>() {}.getType();
//
//                // Parse the JSON string into a list of LidarJsonEntry objects
//                ArrayList<LidarJson> lidarJsonEntries = gson.fromJson(reader, lst);
//
//                for (LidarJson entry : lidarJsonEntries) {
//                    StampedCloudPoints stampedPoint = new StampedCloudPoints(entry.getTime(), entry.getId());
//                    for (List<Double> point : entry.getCloudPoints()) {
//                        CloudPoint cloudPoint = new CloudPoint(point.get(0), point.get(1));
//                        stampedPoint.addCloudPoint(cloudPoint);
//                    }
//                    list.add(stampedPoint);
//                }
//            } catch (Exception e) {
//                System.out.println("im in extension");
//            }
//        }
//        return instance;
//    }
//
//    public static StampedCloudPoints getObject(String id, int time) {
//        for (StampedCloudPoints obj : list) {
//            if (obj.getId().equals(id) && obj.getTime() == time) {
//                return obj;
//            }
//        }
//        return null;
//    }
//}
//
//






