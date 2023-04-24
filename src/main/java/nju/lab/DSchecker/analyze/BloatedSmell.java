package nju.lab.DSchecker.analyze;

import neu.lab.conflict.container.DepJars;
import nju.lab.DSchecker.model.DepJar;
import nju.lab.DSchecker.model.Method;
import nju.lab.DSchecker.model.callgraph.MyCallGraph;

import java.util.List;
import java.util.Set;

public class BloatedSmell implements BaseSmell{
    public static BloatedSmell instance;
    private BloatedSmell() {
    }
    public static BloatedSmell i() {
        if (instance == null) {
            instance = new BloatedSmell();
        }
        return instance;
    }

    @Override
    public void detect() {
        // Compare the dependency tree with the Call graph
        // If the dependency tree is "larger" than the call graph, then the project is bloated
        Set<DepJar> reachableDepJars = MyCallGraph.i().getReachableJars();
        Set<DepJar> declaredDepJars = DepJars.i().getUsedDepJars();
//        System.out.println("Used Depjars");
//        for (DepJar dep : DepJars.i().getUsedDepJars()) {
//            System.out.println(dep.getDisplayName());
//        }
//        System.out.println("Reachable Depjars");
//        for (DepJar dep : reachableDepJars) {
//            System.out.println(dep.getDisplayName());
//        }
        for (DepJar dep : declaredDepJars) {
            if (!reachableDepJars.contains(dep)) {
                System.out.println("Bloated Smell: " + dep.getDisplayName());
            }
        }
    }
}
