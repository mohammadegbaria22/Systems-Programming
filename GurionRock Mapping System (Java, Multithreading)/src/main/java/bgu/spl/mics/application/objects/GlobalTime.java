package bgu.spl.mics.application.objects;

import bgu.spl.mics.MessageBusImpl;

public class GlobalTime {
    private static class SingletonHolder{
        private static GlobalTime instance = new GlobalTime();
    }
    private int globalTime = 0;
    public static GlobalTime getInstance(){
        return SingletonHolder.instance;
    }

    public void increaseGlobaltime(int add) {
        globalTime += add;
    }
    public int getGlobalTime(){return globalTime;}

    public void setGlobalTime(int i) {
        this.globalTime=i;
    }
}
