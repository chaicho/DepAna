package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IDepJar;

import java.util.Collection;
import java.util.List;

@Slf4j
public class HostClassSmell extends BaseSmell{

    @Override
    public void detect(){
        appendToResult("========HostClassSmell========");
        List<String> hostClasses= hostProjectInfo.getHostClasses();
        for(String className : hostClasses){
            Collection<IDepJar> depJars = hostProjectInfo.getUsedDepFromClass(className);
            if(!depJars.isEmpty()) {
                if(depJars.size() == 1 && containsHost(depJars)) {
//                  The conflicting classes are in host jar, not in dependency.
                    continue;
                }
                log.warn("Duplicate Class Smell: " + className);
                appendToResult("Duplicate Class Smell: " + className);
                for (IDepJar depJar : depJars) {
                    if (depJar.isHost()) {
                        continue;
                    }
                    log.warn("in " + depJar.getSig());
                    appendToResult("in " + depJar.getSig());
                }
            }
        }
    }
}
