package nju.lab.DSchecker.core.analyze;

import nju.lab.DSchecker.core.model.IDepJar;

import java.util.Set;

public class BloatedSmell extends BaseSmell{
    @Override
    public void detect() {
        // Compare the dependency tree with the Call graph
        // If the dependency tree is "larger" than the call graph, then the project is bloated
        Set<IDepJar> reachableDepJars = hostProjectInfo.getReachableJars();
        Set<? extends IDepJar> declaredDepJars = depJars.getUsedDepJars();
        for (IDepJar dep : declaredDepJars) {
            if (!reachableDepJars.contains(dep)) {
                System.out.println("Bloated Smell: " + dep.getDisplayName());
            }
        }
    }
}
