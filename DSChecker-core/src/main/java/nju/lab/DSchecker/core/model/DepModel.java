package nju.lab.DSchecker.core.model;


public class DepModel {
    public ICallGraph callGraph;

    public IDepJars depJars;

    public IHostProjectInfo hostProjectInfo;
    public DepModel(ICallGraph callGraph, IDepJars depJars, IHostProjectInfo hostProjectInfo) {
        this.callGraph = callGraph;
        this.depJars = depJars;
        this.hostProjectInfo = hostProjectInfo;
    }
}
