package bgu.spl.mics.application.JavaToJson;

import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.StatisticalFolder;

import java.util.List;

public class Statistics {
    private StatisticalFolder statisticalFolder;
    private List<LandMark> landMarks;

    public StatisticalFolder getStatisticalFolder() {
        return statisticalFolder;
    }

    public List<LandMark> getLandMarks() {
        return landMarks;
    }

    public void setLandMarks(List<LandMark> landMarks) {
        this.landMarks = landMarks;
    }

    public void setStatisticalFolder(StatisticalFolder statisticalFolder) {
        this.statisticalFolder = statisticalFolder;
    }
}
