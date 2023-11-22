package nju.lab.DScheckerMaven.model;

import nju.lab.DSchecker.core.model.ICallGraph;

public class CallGraphMaven  implements ICallGraph {

    private static CallGraphMaven INSTANCE;

    private CallGraphMaven() {
        INSTANCE = null;
    }

    public static CallGraphMaven i() {
        if (INSTANCE == null) {
            INSTANCE = new CallGraphMaven();
        }
        return INSTANCE;
    }

}

