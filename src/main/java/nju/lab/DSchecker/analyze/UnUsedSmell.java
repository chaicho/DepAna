package nju.lab.DSchecker.analyze;

import nju.lab.DSchecker.model.DepJar;
import nju.lab.DSchecker.util.soot.javassist.GetRefedClasses;
import nju.lab.DSchecker.model.HostProjectInfo;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        Set<String> referencedClasses = GetRefedClasses.analyzeReferencedClasses(HostProjectInfo.i().getBuildCp());

        for (String refClass : referencedClasses) {
            Collection<DepJar> dep = HostProjectInfo.i().getUsedDepFromClass(refClass);

           if(dep.size() == 0){
               // Mostly the case where standard Java libraries are used;
               continue;
           }

           /* Since there are possibly several Depjars containing the same class ,we select the closest one */
           DepJar closestDep =  dep.stream()
                    .min(Comparator.comparingInt(DepJar::getDepth))
                    .orElse(null); // 如果dep集合为空，则返回null

            if(closestDep != null ){
                if(closestDep.getDepth() > 0){
                    System.out.println("Used undeclared dependencies " + refClass + " in " + closestDep.getName() + "" + closestDep.getDepth());
                }
            }
        }
    }
//    public
}
