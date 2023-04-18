package neu.lab.conflict.ConflictHandler;

import neu.lab.conflict.util.javassist.GetRefedClasses;
import neu.lab.conflict.vo.HostProjectInfo;

import java.util.Set;

public class UnUsedSmell extends BaseSmell {
    public static UnUsedSmell instance;
    private UnUsedSmell() {
    }
    public static UnUsedSmell i() {
        if (instance == null) {
            instance = new UnUsedSmell();
        }
        return instance;
    }

    @Override
    public void detect(){
//        GetRefedClasses getRefedClasses = new GetRefedClasses();
        Set<String> referencedClasses = GetRefedClasses.analyzeReferencedClasses(HostProjectInfo.i().getBuildCp());
        ;

        // Print the referenced classes
        System.out.println("Referenced classes:");
        for (String refClass : referencedClasses) {
            System.out.println(refClass);
        }
    }
}
