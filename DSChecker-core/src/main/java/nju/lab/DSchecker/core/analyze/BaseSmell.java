package nju.lab.DSchecker.core.analyze;

import nju.lab.DSchecker.core.model.ICallGraph;
import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DSchecker.core.model.IDepJars;
import nju.lab.DSchecker.core.model.IHostProjectInfo;

public abstract class BaseSmell {
    public void init(IHostProjectInfo hostProjectInfo, ICallGraph callGraph, IDepJars<? extends IDepJar> depJars) {
        this.hostProjectInfo = hostProjectInfo;
        this.callGraph = callGraph;
        this.depJars = depJars;
    }
    IHostProjectInfo hostProjectInfo;
    ICallGraph  callGraph;
    IDepJars<? extends IDepJar> depJars;



    public abstract void detect();
}
