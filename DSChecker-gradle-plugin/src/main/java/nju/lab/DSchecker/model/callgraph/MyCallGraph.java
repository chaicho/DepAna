package nju.lab.DSchecker.model.callgraph;


import nju.lab.DSchecker.core.model.ICallGraph;
import nju.lab.DSchecker.model.DepJar;
import nju.lab.DSchecker.model.HostProjectInfo;
import nju.lab.DSchecker.model.Method;
import soot.*;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MyCallGraph  implements ICallGraph {

    private static MyCallGraph INSTANCE;

    private MyCallGraph() {
    }

    public static MyCallGraph i() {
        if (INSTANCE == null) {
            INSTANCE = new MyCallGraph();
        }
        return INSTANCE;
    }

}
