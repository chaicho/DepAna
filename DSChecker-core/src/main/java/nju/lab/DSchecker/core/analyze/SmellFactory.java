package nju.lab.DSchecker.core.analyze;

import nju.lab.DSchecker.core.model.ICallGraph;
import nju.lab.DSchecker.core.model.IDepJars;
import nju.lab.DSchecker.core.model.IHostProjectInfo;
import nju.lab.DSchecker.util.monitor.PerformanceMonitor;
import java.util.ArrayList;
import java.util.List;

public class SmellFactory {

    private IHostProjectInfo hostProjectInfo;
    private IDepJars depJars;
    private ICallGraph callGraph;
    List<BaseSmell> smells = new ArrayList<>();
    /**
     * Creates a new instance of each smell implementation.
     *
     * @return a list of BaseSmell objects
     */
    public void initSmells() {
        smells.add(new LibraryClassConflictSmell());
        smells.add(new LibraryConflictSmell());
        smells.add(new HostClassSmell());
//        smells.add(new LibraryScopeSmell());
        smells.add(new UnDeclaredSmell());
        smells.add(new UnUsedSmell());
        smells.add(new LibraryScopeConflictSmell());
        smells.add(new WrapperJarMissingSmell());
        smells.add(new WrapperConfMissingSmell());
        smells.add(new WrapperJarAbnormalSmell());
        for (BaseSmell smell : smells) {
            smell.init(hostProjectInfo, callGraph, depJars);
        }
    }

    /**
     * Detects all smells for the specified list of BaseSmell objects.
     *
     * */
    public void detectAll() {
        if ( hostProjectInfo.getResultFile().exists()){
            hostProjectInfo.getResultFile().delete();
        }
        for (BaseSmell smell : smells) {
            System.out.println("detecting smell: " + smell.getClass().getSimpleName());
            PerformanceMonitor.start(smell.getClass().getSimpleName());
            smell.detectSmell();
            smell.outputResult();
            PerformanceMonitor.stop(smell.getClass().getSimpleName());
        }
    }

    public void addSmell(BaseSmell smell) {
        smell.init(hostProjectInfo, callGraph, depJars);
        System.out.println(smells);
        smells.add(smell);
        System.out.println(smells);
    }
    public void init(IHostProjectInfo hostProjectInfo, IDepJars depJars, ICallGraph callGraph) {
        this.hostProjectInfo = hostProjectInfo;
        this.callGraph = callGraph;
        this.depJars = depJars;
        initSmells();
    }

}
