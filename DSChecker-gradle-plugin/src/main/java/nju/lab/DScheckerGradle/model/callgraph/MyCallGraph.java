package nju.lab.DScheckerGradle.model.callgraph;


import nju.lab.DSchecker.core.model.ICallGraph;

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
