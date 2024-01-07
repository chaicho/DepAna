package nju.lab.DSchecker.core.model;


import lombok.extern.slf4j.Slf4j;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface ICallGraph {
    // Record the possible methods that invoke specific classes.
    public Map<String , Set<SootMethod> > invokedMethods = new HashMap<String , Set<SootMethod>>();
    public CallGraph runtimeCallGraph = null;
    public CallGraph testCallGraph = null;
    public default Set<String> getSourceMethods (String className) {
        if (!invokedMethods.containsKey(className)) {
            return new HashSet<String>();
        }
        return invokedMethods.get(className)
                .stream()
                .map(method -> method.getSignature())
                .collect(Collectors.toSet());
    }
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
        if (!Scene.v().hasCallGraph()) {
            System.out.println("No CallGraph Found!");
            return reachableClasses;
        }
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
                    String className = targetMethod.getDeclaringClass().getName();
                    // Add its class name to the set
                    reachableClasses.add(targetMethod.getDeclaringClass().getName());
                    if (invokedMethods.containsKey(className)){
                        invokedMethods.get(className).add(entryMethod);
                    }
                    else {
                        Set<SootMethod> methods = new HashSet<SootMethod>();
                        methods.add(entryMethod);
                        invokedMethods.put(className, methods);
                    }
                }
            }
        }
        return reachableClasses;
    }

}
