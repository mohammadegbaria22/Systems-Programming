import bgu.spl.mics.application.objects.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

 class CameraTest {
     //the OUT-object
    Camera camera;
    GlobalTime globalTime;

    @BeforeEach
    void setUp() {
        // Initialize global time
        globalTime = GlobalTime.getInstance();
        globalTime.setGlobalTime(10); // Example global time for testing

        // Initialize camera
        camera = new Camera(1, 5); // Camera with frequency of 5

        // Add stamped detected objects to the camera's list
        StampedDetectedObjects obj1 = new StampedDetectedObjects(5);  // Time: 5
        StampedDetectedObjects obj2 = new StampedDetectedObjects(10); // Time: 10
        StampedDetectedObjects obj3 = new StampedDetectedObjects(15); // Time: 15
        camera.add(obj1);
        camera.add(obj2);
        camera.add(obj3);
    }

     @AfterEach
     public void tearDown() {
         camera = null;
         globalTime=null;
     }

    @Test
    void testGetStampWithFrequency() {
        // Global time is 10, frequency is 5 -> should match obj1 (time 5)
        StampedDetectedObjects result = camera.getStampWithFrequency();
        assertNotNull(result, "Expected a non-null result.");
        assertEquals(5, result.getTime(), "Expected time of the returned object to be 5.");

        // Update global time to 15, frequency is 5 -> should match obj2 (time 10)
        globalTime.setGlobalTime(15);
        result = camera.getStampWithFrequency();
        assertNotNull(result, "Expected a non-null result.");
        assertEquals(10, result.getTime(), "Expected time of the returned object to be 10.");

        // Update global time to 20, frequency is 5 -> no matching objects
        globalTime.setGlobalTime(20);
        result = camera.getStampWithFrequency();
        assertNotNull(result, "Expected a null result as no objects match.");
    }

    @Test
    void testGetStampWithFrequencyWithEmptyList() {
        camera.clearList(); // Clear the list
        StampedDetectedObjects result = camera.getStampWithFrequency();
        assertNull(result, "Expected null as the list is empty.");
    }
}
