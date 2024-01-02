package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IDepJar;

//import nju.lab.DSchecker.core;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class UnDeclaredSmell extends BaseSmell {


    @Override
    public void detect(){
        appendToResult("========UnDeclaredSmell========");
        Set<IDepJar> declaredDepJars = depJars.getUsedDepJars().stream()
                                                               .filter(depJar -> depJar.getDepth() == 1)
                                                               .collect(Collectors.toSet());
        // Get DepJars with their used scenario.
        Set<IDepJar> actualTestDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("test");
        Set<IDepJar> actualCompileDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("compile");
        // The line is needed to calculate runtime used classes
        Set<IDepJar> actualRuntimeDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("runtime");
        Set<IDepJar> actualByteCodeDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("bytecode");
        Set<IDepJar> allUsedDepJars = new HashSet<>();
        allUsedDepJars.addAll(actualTestDepJars);
        allUsedDepJars.addAll(actualCompileDepJars);
        // allUsedDepJars.addAll(actualByteCodeDepJars);
        // allUsedDepJars.addAll(actualRuntimeDepJars);

        allUsedDepJars.removeAll(declaredDepJars);
        removeDepJarsWithSameGA(allUsedDepJars, declaredDepJars);

        for (IDepJar dep : allUsedDepJars) {
            if(dep.isHost()) {
                continue;
            }

            log.warn("Undeclared Dependency: " + dep.getSig());
            appendToResult("Undeclared dependency: " + dep.getDisplayName());
            appendToResult("    Dependency scope: " + dep.getScope());
            appendToResult("    Pulled in by: " + dep.getUsedDepTrail());
            appendToResult(dep.getUsedClassesAsString());
            appendToResult("---------");
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
