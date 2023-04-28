package nju.lab.DSchecker.analyze;

import nju.lab.DSchecker.model.DepJar;
import nju.lab.DSchecker.model.HostProjectInfo;

import java.util.Set;

public class LibraryScopeSmell implements BaseSmell {
    @Override
    public void detect() {
        Set<DepJar> ABIDepjars = HostProjectInfo.i().getABIDepJars();
        // Dependecies that are declared as api in the project
        Set<String> ApiDepjars = HostProjectInfo.i().getApiDepJars();
        for (DepJar dep : ABIDepjars) {
            if(ApiDepjars.contains(dep.getName()))
                continue;
//            This means that some ABI are used but not declared as api
            System.out.println("LibraryScope Smell : " + dep.getName() + "should be declared as api");
        }
    }
}
