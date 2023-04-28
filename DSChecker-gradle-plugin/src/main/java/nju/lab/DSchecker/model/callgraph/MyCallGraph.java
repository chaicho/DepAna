package nju.lab.DSchecker.model.callgraph;


import nju.lab.DSchecker.model.DepJar;
import nju.lab.DSchecker.model.HostProjectInfo;
import nju.lab.DSchecker.model.Method;
import soot.*;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MyCallGraph {

    private static MyCallGraph INSTANCE;

    private MyCallGraph() {
    }

    public static MyCallGraph i() {
        if (INSTANCE == null) {
            INSTANCE = new MyCallGraph();
        }
        return INSTANCE;
    }

    public Set<DepJar> getReachableJars() {
        Set<DepJar> ret = new java.util.HashSet<>();
        ReachableMethods reachableMethods = Scene.v().getReachableMethods();
        QueueReader queueReader = reachableMethods.listener();


        while (queueReader.hasNext()) {
            MethodOrMethodContext sootMethodContext = ((MethodOrMethodContext) queueReader.next());
            SootMethod sootMethod = sootMethodContext.method();
            Context context = sootMethodContext.context();
            if (sootMethod.isJavaLibraryMethod()) {
                continue;
            }
            String declaringclassName = sootMethod.getDeclaringClass().getName();
            DepJar depJar = HostProjectInfo.i().getSingleUsedDepFromClass(declaringclassName);
            if (depJar != null) {
                ret.add(depJar);
            }
        }
        return ret;
    }
}
