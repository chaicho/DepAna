package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IDepJar;

import java.util.Set;
@Slf4j
public class BloatedSmell extends BaseSmell{
    @Override
    public void detect() {
        // Compare the dependency tree with the Call graph
        // If the dependency tree is "larger" than the call graph, then the project is bloated

        Set<IDepJar> reachableDepJars = hostProjectInfo.getReachableJars();
        Set<? extends IDepJar> declaredDepJars = depJars.getUsedDepJars();
        output("========BloatedSmell========");
        for (IDepJar dep : declaredDepJars) {
            if (!reachableDepJars.contains(dep)) {
                log.warn("Bloated Smell: " + dep.getDisplayName());
                output("Bloated Smell: " + dep.getDisplayName());
            }
        }
    }
}
