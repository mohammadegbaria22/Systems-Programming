package bgu.spl.mics.application;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.JavaToJson.convertJava;
import bgu.spl.mics.application.JavaToJson.convertJavaCrash;
import bgu.spl.mics.application.jsonToJava.Configuration;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.*;


/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {
    static String baseDirectory;
    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        // TODO: Parse configuration file.
        // TODO: Initialize system components and services.
        // TODO: Start the simulation.









        if (args.length == 0) {
            System.err.println("Usage: java GurionRockRunner <configFilePath>");
            return;
        }
         String configFilePath = args[0];
         baseDirectory = Paths.get(configFilePath).getParent().toFile().getAbsolutePath() + "/";
    
         Configuration configuration = conv(configFilePath);
        if (configuration == null) {
            System.out.println(configFilePath);
            System.err.println("Failed to load configuration from: " + baseDirectory);
            return;
        }

        String lidarDataPath = configuration.getLidarWorkers().getLidars_data_path();


        //---------------------------------lidarDataBase--------------------------------
        LiDarDataBase l =  LiDarDataBase.getInstance();
        l.lidarInitialize(baseDirectory + lidarDataPath);

        //-----------------------------Cameras------------------------------------
        String cameraDataPath = configuration.getCameras().getCamera_datas_path();
        String c = Paths.get(cameraDataPath).getParent().toFile().getAbsolutePath() + "/";
        Map<String, List<StampedDetectedObjects>> mapCamera = parseCameraData(baseDirectory + cameraDataPath);


        //-----------------------------Poses------------------------------------
        String poseDataPath = configuration.getPoseJsonFile();
        GPSIMU gi = parsePoseData(baseDirectory+ poseDataPath );


        //-------------------------------configFile----------------------------------
        List<Thread> threads = parseConfigData(configFilePath , mapCamera , gi);
        for (Thread thread : threads){
            thread.start();
        }
    }

    public static Configuration conv (String name) {
        Gson gson = new Gson();
        Configuration configuration = null;
        try (FileReader reader = new FileReader(name)) {

            // Define the type for the list of employees
            Type configFileType = new TypeToken<Configuration>() {
            }.getType();
            configuration = gson.fromJson(reader, configFileType);

        }
        catch (IOException e) {
            e.printStackTrace();

        }
        return configuration;
    }




    ///////////////////////////////////////functions/////////////////////////////////////////////////////////////////////////
public static List<Thread> parseConfigData(String filepath , Map<String, List<StampedDetectedObjects>> map , GPSIMU gpsimu){
    List<Thread> threads = new LinkedList<>();

    Gson gson = new Gson();
    Configuration configuration=null;
    try (FileReader reader = new FileReader(filepath)) {

        // Define the type for the list of employees
        Type configFileType = new TypeToken<Configuration>() {}.getType();
        configuration = gson.fromJson(reader, configFileType);







        //create threads for each object in the simulation correspondingly
        //cameras initializing
        List<Camera> cameras = configuration.getCameras().getCamerasConfigurations();
        for (Camera c : cameras){
            List<StampedDetectedObjects> lst = map.get(c.getCamera_key());
            c.setList(lst);

            CameraService camService = new CameraService(c);
            threads.add(new Thread(camService));
        }


        //lidars initializing
        List<LiDarWorkerTracker> lidars = configuration.getLidarWorkers().getLidarConfigurations();
        for (LiDarWorkerTracker l : lidars){
            LiDarWorkerTracker lidar = new LiDarWorkerTracker(l.getId() , l.getFrequency());

            MicroService lidarService = new LiDarService(lidar , cameras.size());
            threads.add(new Thread(lidarService));
        }


        //pose initialize
        GPSIMU GI = gpsimu;
        MicroService poseService = new PoseService(GI);
        threads.add(new Thread(poseService));



        //TimeService initialize
        MicroService timeService = new TimeService(configuration.getTickTime(), configuration.getDuration() );
        Thread t = new Thread(timeService);
        threads.add(t);


        //fusionSlam initialize
        MicroService fusionService = new FusionSlamService(FusionSlam.getInstance() , threads.size() + 1);
        threads.add(new Thread(fusionService));
    }
    catch (IOException e) {
        e.printStackTrace();

    }
    return threads;
}




    //output json file if there is a crash
    public static void crashedToJson(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(FileWriter writer = new FileWriter(baseDirectory+"OutputError.json")){
            gson.toJson(convertJavaCrash.getInstance() , writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }





    //out put to json if nothing crashed
    public static void convertToJson(convertJava input) {
     convertJava convertjava = input;

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    try(FileWriter writer = new FileWriter(baseDirectory+"output_file.json")){
        gson.toJson(convertjava , writer);
    }
    catch (IOException e) {
        e.printStackTrace();
        }
    }

    //set the final results of the simulation before returning a json file
    public static convertJava resultFunction(){
        convertJava res = new convertJava();
        Map<String , LandMark> map = new HashMap<>();

        List<LandMark> lst = FusionSlam.getInstance().getLandMarks();
        for (LandMark lm : lst){
            map.put(lm.getId() , lm);
        }
        res.setLandMarks(map);
        res.setNumDetectedObjects(StatisticalFolder.getInstance().getNumberOfDetectedObjects().get());
        res.setLandMarks(StatisticalFolder.getInstance().getNumberOfLandmarks().get());
        res.setSystemRuntime(StatisticalFolder.getInstance().getSystemRuntime().get());
        res.setNumTrackedObjects(StatisticalFolder.getInstance().getNumberOfTrackedObjects().get());
        return res;
    }


    //provide the cameras their data from the json file
    public static Map<String, List<StampedDetectedObjects>> parseCameraData(String filepath){
        Gson gson = new Gson();
        String filePath = filepath;

        try (FileReader reader = new FileReader(filePath)) {
            // Parse the raw JSON into a Map of JsonElement
            Type rawType = new TypeToken<Map<String, JsonElement>>() {}.getType();
            Map<String, JsonElement> rawCameras = gson.fromJson(reader, rawType);

            // Process and map each camera to a list of StampedDetectedObjects
            Map<String, List<StampedDetectedObjects>> cameras = new java.util.HashMap<>();
            for (Map.Entry<String, JsonElement> entry : rawCameras.entrySet()) {
                String camera = entry.getKey();
                JsonElement element = entry.getValue();

                List<StampedDetectedObjects> flattenedList = new ArrayList<>();
                if (element.isJsonArray()) {
                    JsonArray array = element.getAsJsonArray();

                    for (JsonElement innerElement : array) {
                        if (innerElement.isJsonObject()) {
                            // Handle a flat list of objects
                            StampedDetectedObjects obj = gson.fromJson(innerElement, StampedDetectedObjects.class);
                            flattenedList.add(obj);
                        } else if (innerElement.isJsonArray()) {
                            // Handle nested lists
                            for (JsonElement nestedElement : innerElement.getAsJsonArray()) {
                                StampedDetectedObjects obj = gson.fromJson(nestedElement, StampedDetectedObjects.class);
                                flattenedList.add(obj);
                            }
                        }
                    }
                }
                cameras.put(camera, flattenedList);
            }
            return cameras;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    //provide the GPSIMU his data from the json file
    public static GPSIMU parsePoseData(String filepath ){
        GPSIMU GI = new GPSIMU();
        Gson gson = new Gson();

        String filePath = filepath; // Replace with the actual file path
        try (FileReader reader = new FileReader(filePath)){
            Type lst = new TypeToken<ArrayList<Pose>>(){}.getType();
            ArrayList<Pose> poses = gson.fromJson(reader, lst);

            for (Pose pose : poses) {
                GI.getList().add(pose);
            }

        }    catch (IOException e) {
            e.printStackTrace();
        }
        return GI;
    }







}
