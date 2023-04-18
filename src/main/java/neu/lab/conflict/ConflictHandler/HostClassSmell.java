package neu.lab.conflict.ConflictHandler;

public class HostClassSmell extends BaseSmell{
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
