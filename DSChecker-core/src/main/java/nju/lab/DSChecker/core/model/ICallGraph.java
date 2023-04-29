package nju.lab.DSchecker.core.model;


import soot.*;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;

import java.util.Set;

public interface ICallGraph {

    public default Set<String> getReachableClasses() {
        Set<String> ret = new java.util.HashSet<>();
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
            ret.add(declaringclassName);
        }
        return ret;
    }
}
