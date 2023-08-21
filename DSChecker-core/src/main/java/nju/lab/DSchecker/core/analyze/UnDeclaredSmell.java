package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DSchecker.util.javassist.GetRefedClasses;

//import nju.lab.DSchecker.core;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class UnDeclaredSmell extends BaseSmell {


    @Override
    public void detect(){
        appendToResult("========UnDeclaredSmell========");
        Set<? extends IDepJar> testDepJars = depJars.getDepJarsWithScope("test");
        Set<? extends IDepJar> compileDepJars = depJars.getDepJarsWithScope("compile");
        Set<? extends IDepJar> runtimeDepJars = depJars.getDepJarsWithScope("runtime");
        Set<? extends IDepJar> providedDepJars = depJars.getDepJarsWithScope("provided");
        // Get DepJars with their used scenario.
        Set<IDepJar> actualTestDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("test");
        Set<IDepJar> actualCompileDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("compile");
        Set<IDepJar> actualRuntimeDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("runtime");
        Set<IDepJar> allUsedDepJars = new HashSet<>();
        allUsedDepJars.addAll(actualTestDepJars);
        allUsedDepJars.addAll(actualCompileDepJars);
        allUsedDepJars.addAll(actualRuntimeDepJars);

        allUsedDepJars.removeAll(testDepJars);
        allUsedDepJars.removeAll(compileDepJars);
        allUsedDepJars.removeAll(providedDepJars);
        allUsedDepJars.removeAll(runtimeDepJars);
        for (IDepJar dep : allUsedDepJars) {
            log.warn("Undeclared Dependency: " + dep.getSig());
            appendToResult("Undeclared dependency: " + dep.getSig());
            appendToResult("Dependency scope: " + dep.getScope());
            appendToResult("Pulled in by: " + dep.getDepTrail());
        }

//        //        Get the classes from the build directory of Host Project and analyze the refed classes of them.
//        Set<String> referencedClasses =  GetRefedClasses.analyzeReferencedClasses(hostProjectInfo.getBuildCp());
//
//        for (String refClass : referencedClasses) {
//            Collection<IDepJar> dep = hostProjectInfo.getUsedDepFromClass(refClass);
//           if(dep.size() == 0){
//               continue;
//           }
//           /* Since there are possibly several Depjars containing the same class ,we select the closest one */
//           IDepJar closestDep =  dep.stream()
//                    .min(Comparator.comparingInt(IDepJar::getDepth))
//                    .orElse(null); // 如果dep集合为空，则返回null
//           if(closestDep != null ){
//                log.debug("Closest Dependency : " + closestDep.getSig() + " in " + refClass + " depth : " + closestDep.getDepth());
//                if(closestDep.getDepth() > 1){
//                    /* If the closest dependency is not the directly Declared Dependency, then it is an undeclared dependency */
//                    log.warn("UnDeclared Smell : " + refClass + " in " + closestDep.getSig());
//                    appendToResult("UnDeclared Smell : " + refClass + " in " + closestDep.getSig());
//                    appendToResult("Pulled in By" + closestDep.getDepTrail());
//                }
//            }
//        }
    }
}
