package nju.lab.DSchecker.core.analyze;

import nju.lab.DSchecker.core.model.ICallGraph;
import nju.lab.DSchecker.core.model.IDepJars;
import nju.lab.DSchecker.core.model.IHostProjectInfo;

import java.util.ArrayList;
import java.util.List;

public class SmellFactory {

    private IHostProjectInfo hostProjectInfo;
    private IDepJars depJars;
    private ICallGraph callGraph;
    /**
     * Creates a new instance of each smell implementation.
     *
     * @return a list of BaseSmell objects
     */
    public  List<BaseSmell> createSmells() {
        List<BaseSmell> smells = new ArrayList<>();
        smells.add(new ClassConflictSmell());
        smells.add(new LibraryConflictSmell());
        smells.add(new HostClassSmell());
        smells.add(new LibraryScopeSmell());
        smells.add(new UnDeclaredSmell());
        smells.add(new BloatedSmell());
        smells.add(new WrapperSmell());
        for (BaseSmell smell : smells) {
            smell.init(hostProjectInfo, callGraph, depJars);
        }
        return smells;
    }

    /**
     * Detects all smells for the specified list of BaseSmell objects.
     *
     * */
    public void detectAll() {
        List<BaseSmell> smells = createSmells();
        for (BaseSmell smell : smells) {
            smell.detect();
        }
    }

    public void init(IHostProjectInfo hostProjectInfo, IDepJars depJars, ICallGraph callGraph) {
        this.hostProjectInfo = hostProjectInfo;
        this.callGraph = callGraph;
        this.depJars = depJars;
    }

}
