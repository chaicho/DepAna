package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IDepJar;

import java.util.Set;

@Slf4j
public class LibraryScopeSmell extends BaseSmell {
    @Override
    public void detect() {
        output("========LibraryScopeSmell========");
        Set<IDepJar> ABIDepjars = hostProjectInfo.getABIDepJars();
        // Dependecies that are declared as api in the project
        Set<String> ApiDepjars = hostProjectInfo.getApiDepJars();
        if (ApiDepjars.isEmpty()) {
            log.info("No api dependencies");
            return;
        }
        if (ABIDepjars.isEmpty()) {
            log.info("No ABI dependencies");
            return;
        }
        for (IDepJar dep : ABIDepjars) {
            if (ApiDepjars.contains(dep.getSig()))
                continue;
//            This means that some ABI are used but not declared as api
            log.warn("LibraryScope Smell : " + dep.getSig() + "should be declared as api");
            output("LibraryScope Smell : " + dep.getSig() + "should be declared as api");
        }

//        TODO : API that are not used as ABI

    }
}
