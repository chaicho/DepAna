package nju.lab.DSchecker.core.analyze;

import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DSchecker.util.javassist.GetRefedClasses;

//import nju.lab.DSchecker.core;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

public class UnDeclaredSmell extends BaseSmell {


    @Override
    public void detect(){
        System.out.println("UnDeclaredSmell detect");
//        Get the classes from the build directory of Host Project and analyze the refed classes of them.
        Set<String> referencedClasses =  GetRefedClasses.analyzeReferencedClasses(hostProjectInfo.getBuildCp());

        for (String refClass : referencedClasses) {
            /* Get the dependency jar containing the refed class */
            Collection<IDepJar> dep = hostProjectInfo.getUsedDepFromClass(refClass);

           if(dep.size() == 0){
//               ERROR: Unable to find jar containing class.
               // Mostly the case where standard Java libraries are used;
               continue;
           }

           /* Since there are possibly several Depjars containing the same class ,we select the closest one */
           IDepJar closestDep =  dep.stream()
                    .min(Comparator.comparingInt(IDepJar::getDepth))
                    .orElse(null); // 如果dep集合为空，则返回null

            if(closestDep != null ){
                if(closestDep.getDepth() > 0){
                    /* If the closest dependency is not the directly Declared Dependency, then it is an undeclared dependency */
                    System.out.println("UnDeclared Smell : " + refClass + " in " + closestDep.getName());
                }
            }
        }
    }
//    public
}
