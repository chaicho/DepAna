package nju.lab.DSchecker.analyze;

import nju.lab.DSchecker.model.DepJar;
import nju.lab.DSchecker.model.HostProjectInfo;

import java.util.Set;

public class LibraryScopeSmell implements BaseSmell {
    @Override
    public void detect() {
        Set<DepJar> ABIDepjars = HostProjectInfo.i().getABIDepJars();

    }
}
