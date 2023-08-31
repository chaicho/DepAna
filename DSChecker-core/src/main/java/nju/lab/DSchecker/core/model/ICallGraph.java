package nju.lab.DSchecker.core.model;


import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public interface ICallGraph {
//    public default boolean isJDKClass
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
    public default boolean outCallingMethod(SootMethod method) {
        return method.getDeclaringClass().isLibraryClass()
                && !method.getDeclaringClass().isJavaLibraryClass();

    }
    public default Set<String> getReachableDirectClasses() {
        Set<String> reachableClasses = new HashSet<String>();
        List<SootMethod> entryMthds = new ArrayList<SootMethod>();
        CallGraph cg = Scene.v().getCallGraph();
        for (SootClass sootClass : Scene.v().getApplicationClasses()) {
            for (SootMethod method : sootClass.getMethods()) {
                if (outCallingMethod(method)) {
                    reachableClasses.add(method.getDeclaringClass().getName());
                }
                else {
                    entryMthds.add(method);
                }
            }
        }

        for (SootMethod entryMethod : entryMthds) {
            // Get the iterator of the edges out of this method
            Iterator<Edge> edgeIt = cg.edgesOutOf(entryMethod);
            // Loop over the edges
            while (edgeIt.hasNext()) {
                // Get the target method of each edge
                SootMethod targetMethod = edgeIt.next().tgt();
                // Check if it is in an application class

                if (outCallingMethod(targetMethod)) {
                    // Add its class name to the set
                    reachableClasses.add(targetMethod.getDeclaringClass().getName());
                }
            }
        }
        return reachableClasses;
    }

}
