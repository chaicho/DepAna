package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IDepJar;

import java.util.Set;

@Slf4j
public class LibraryScopeSmell extends BaseSmell {
    @Override
    public void detect() {
        appendToResult("========LibraryScopeSmell========");
        Set<IDepJar> ABIDepjars = hostProjectInfo.getABIDepJars();
        // Dependecies that are declared as api in the project
        Set<String> ApiDepjars = hostProjectInfo.getApiDepJars();
        Set<String> ABIClasses = hostProjectInfo.getABIClasses();
        appendToResult("ApiDepjars: " + ApiDepjars);
        if (ApiDepjars.isEmpty()) {
            log.info("No api dependencies");
            return;
        }
        if (ABIDepjars.isEmpty()) {
            log.info("No ABI dependencies");
            return;
        }
        for (IDepJar dep : ABIDepjars) {
            if (ApiDepjars.contains(dep.getName()))
                continue;
//            This means that some ABI are used but not declared as api
            log.warn("LibraryScope Smell : " + dep.getName() + "should be declared as api");
            Set<String> ABICls = dep.getAllCls();
            ABICls.retainAll(ABIClasses);
            appendToResult("LibraryScope Smell : " + dep.getSig() + "should be declared as api");
            for (String cls : ABICls) {
                log.warn("in " + cls);
                appendToResult("in " + cls);
            }
        }

//        TODO : API that are not used as ABI

    }
}
