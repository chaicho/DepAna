package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DSchecker.util.javassist.GetRefedClasses;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
@Slf4j
public class BloatedSmell extends BaseSmell{
    @Override
    public void detect() {
        // Compare the dependency tree with the Call graph
        // If the dependency tree is "larger" than the call graph, then the project is bloated
        //        Runtime dependencies
        Set<String> importPaths = new HashSet<String>();
        Set<IDepJar> actualUsedDepJars = new HashSet<>();
        Set<IDepJar> reachableRuntimeDepJars = hostProjectInfo.getReachableJars();
        //        Compile time dependencies
        Set<IDepJar> reachableCompileDepJars = new HashSet<>();
        // Get the dependencies used by the code of the project
        Set<String> referencedClasses =  GetRefedClasses.analyzeReferencedClasses(hostProjectInfo.getBuildCp());
        for (String refClass : referencedClasses) {
            /* Get the dependency jar containing the refed class */
            Collection<IDepJar> dep = hostProjectInfo.getUsedDepFromClass(refClass);
            reachableCompileDepJars.addAll(dep);
        }
        actualUsedDepJars.addAll(reachableCompileDepJars);
        actualUsedDepJars.addAll(reachableRuntimeDepJars);

        Set<? extends IDepJar> declaredDepJars = depJars.getUsedDepJars();
        appendToResult("========BloatedSmell========");
        for (IDepJar dep : declaredDepJars) {
            if (!actualUsedDepJars.contains(dep)) {
                log.warn("Bloated Smell: " + dep.getDisplayName());
                appendToResult("Bloated Smell: " + dep.getDisplayName());
                appendToResult(dep.getDepTrail());
                importPaths.add(dep.getDepTrail());
            }
        }
        return;
    }

    /**
     * Get the import paths of jars not used. But need to merge them into the nearest father.
     * @param Set<String> importPaths
     * @return Set<String> mergedImportPaths
     */
     Set<String> mergeImportPaths(Set<String> importPaths) {
        Set<String> mergedImportPaths = new HashSet<String>();
        for (String importPath : importPaths) {
            String[] importPathArray = importPath.split("->");
            String parentPath = "";
            for (int i = 0; i < importPathArray.length - 1; i++) {
                parentPath += importPathArray[i];
                parentPath += "->";
            }
            parentPath = parentPath.substring(0, parentPath.length() - 2);
            if (!importPaths.contains(parentPath)) {
                mergedImportPaths.add(importPath);
            }
        }
        return mergedImportPaths;
    }
}
