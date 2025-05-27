package bgu.spl.mics.application.objects;

public class GlobalCrashed {
    private static class SingletonHolder{
        private static GlobalCrashed instance = new GlobalCrashed();
    }
    private boolean crash =  false;
    private boolean stop = false;

    public static GlobalCrashed getInstance(){
        return SingletonHolder.instance;
    }

    public boolean getStop(){return stop;}
    public void setStop(){stop = true;}
    public boolean getCrahs(){return crash;}
    public void setCrash(){crash = true;}
}
