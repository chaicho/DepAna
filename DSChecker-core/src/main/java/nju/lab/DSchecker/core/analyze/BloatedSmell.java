package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IDepJar;

import java.util.HashSet;
import java.util.Set;
@Slf4j
public class BloatedSmell extends BaseSmell{
    @Override
    public void detect() {
        // Compare the dependency tree with the Call graph
        // If the dependency tree is "larger" than the call graph, then the project is bloated
        Set<String> importPaths = new HashSet<String>();
        Set<IDepJar> reachableDepJars = hostProjectInfo.getReachableJars();
        Set<? extends IDepJar> declaredDepJars = depJars.getUsedDepJars();
        output("========BloatedSmell========");
        for (IDepJar dep : declaredDepJars) {
            if (!reachableDepJars.contains(dep)) {
                log.warn("Bloated Smell: " + dep.getDisplayName());
                output("Bloated Smell: " + dep.getDisplayName());
                output(dep.getDepTrail());
                Set<String> depTrails = dep.getDepTrails();
                importPaths.addAll(depTrails);
            }
        }
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
