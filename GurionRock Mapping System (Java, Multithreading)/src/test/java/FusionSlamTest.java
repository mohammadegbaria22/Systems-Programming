import bgu.spl.mics.application.objects.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FusionSlamTest {
    //OUT-object under test
    private FusionSlam fusionSlam;

    @BeforeEach
    public void setUp() {
        fusionSlam = FusionSlam.getInstance();
    }
    @AfterEach
    public void tearDown() {
        fusionSlam = null;
    }

    @Test
    public void testTrackedObjectsToLandMarks_NewObject() {
        List<TrackedObject> trackedObjects = new ArrayList<>();
        TrackedObject obj = new TrackedObject("1", 1, "Test Object");
        obj.addCoordinate(new CloudPoint(1.0, 2.0));
        trackedObjects.add(obj);

        Pose pose = new Pose(-3.2076,  0.0755, -87.48, 1);
        fusionSlam.addPose(pose);

        fusionSlam.trackedObjectsToLandMarks(trackedObjects);

        LandMark landMark = fusionSlam.getLandMArk("1");
        assertFalse(fusionSlam.getLandMarks().isEmpty());
        assertNotNull(landMark);
        assertEquals("Test Object", landMark.getDescription());
        assertEquals(1, landMark.getList().size());
        assertEquals(fusionSlam.mathCalc(1,2,pose ).getX(), landMark.getList().get(0).getX());
        assertEquals(fusionSlam.mathCalc(1,2,pose ).getY(), landMark.getList().get(0).getY());
    }

    @Test
    public void testTrackedObjectsToLandMarks_ExistingObject() {
        List<CloudPoint> initialPoints = new ArrayList<>();
        initialPoints.add(new CloudPoint(1.0, 2.0));
        LandMark existingLandMark = new LandMark("2", "Test Object", initialPoints);
        fusionSlam.addLandMark(existingLandMark);

        List<TrackedObject> trackedObjects = new ArrayList<>();
        TrackedObject obj = new TrackedObject("2", 2, "Test Object");
        obj.addCoordinate(new CloudPoint(4.0, 6.0));
        trackedObjects.add(obj);

        Pose pose = new Pose(1.9, 4.9, 12.12, 2);
        fusionSlam.addPose(pose);

        fusionSlam.trackedObjectsToLandMarks(trackedObjects);

        LandMark updatedLandMark = fusionSlam.getLandMArk("2");
        double expectedX = fusionSlam.averageX( 1.0,fusionSlam.mathCalc(4.0,6.0,pose ).getX());
        double expectedY = fusionSlam.averageY( 2.0,fusionSlam.mathCalc(4.0,6.0,pose ).getY());

        assertNotNull(updatedLandMark);
        assertEquals(2, fusionSlam.getLandMarks().size());//with the last test.
        assertEquals(expectedX, updatedLandMark.getList().get(0).getX());
        assertEquals(expectedY, updatedLandMark.getList().get(0).getY());
    }

}
