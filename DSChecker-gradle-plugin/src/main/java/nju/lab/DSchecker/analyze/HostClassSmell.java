package nju.lab.DSchecker.analyze;

public class HostClassSmell implements BaseSmell{
    public static HostClassSmell instance;
    private HostClassSmell() {
    }
    public static HostClassSmell i() {
        if (instance == null) {
            instance = new HostClassSmell();
        }
        return instance;
    }

    @Override
    public void detect(){
        //TODO
    }
}
