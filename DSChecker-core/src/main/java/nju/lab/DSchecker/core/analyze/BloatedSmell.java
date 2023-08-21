package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DSchecker.util.javassist.GetRefedClasses;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class BloatedSmell extends BaseSmell{
    @Override
    public void detect() {
        Set<? extends IDepJar> allDepJars = depJars.getUsedDepJars();
        Set<? extends IDepJar> allDirectDepJars = allDepJars.stream()
                                                            .filter(dep -> dep.getDepth() == 1)
                                                            .collect(Collectors.toSet());
        Set<IDepJar> actualTestDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("test");
        Set<IDepJar> actualCompileDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("compile");
        Set<IDepJar> actualRuntimeDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("runtime");
        allDirectDepJars.removeAll(actualTestDepJars);
        allDirectDepJars.removeAll(actualCompileDepJars);
        allDirectDepJars.removeAll(actualRuntimeDepJars);

        appendToResult("========BloatedSmell========");
        for (IDepJar dep : allDirectDepJars) {
                log.warn("Bloated Smell: " + dep.getDisplayName());
                appendToResult("Bloated Smell: " + dep.getDisplayName());
                appendToResult("Dep Scope: " + dep.getScope());
                appendToResult(dep.getDepTrail());
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
