package nju.lab.DSchecker.core.analyze;

import nju.lab.DSchecker.core.model.IDepJar;

import java.util.Set;

public class LibraryScopeSmell extends BaseSmell {
    @Override
    public void detect() {
        Set<IDepJar> ABIDepjars = hostProjectInfo.getABIDepJars();
        // Dependecies that are declared as api in the project
        Set<String> ApiDepjars = hostProjectInfo.getApiDepJars();
        for (IDepJar dep : ABIDepjars) {
            if(ApiDepjars.contains(dep.getName()))
                continue;
//            This means that some ABI are used but not declared as api
            log.warn("LibraryScope Smell : " + dep.getName() + "should be declared as api");
        }
    }
}
