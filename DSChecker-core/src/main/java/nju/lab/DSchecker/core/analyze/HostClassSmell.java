package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IDepJar;

import java.util.Collection;
import java.util.List;

@Slf4j
public class HostClassSmell extends BaseSmell{

    @Override
    public void detect(){
        output("=======HostClassSmell=======");
        List<String> hostClasses= hostProjectInfo.getHostClasses();
        output("Host Classes : " + hostClasses);
        output(hostProjectInfo.getBuildCp());
//        log.warn("DepJars : " + depJars.getUsedDepJars());
//        IDepJar firstDepJar = depJars.getUsedDepJars().stream().findFirst().get();
//        log.warn("FirstClasses :  " +firstDepJar.getAllCls());
        for(String className : hostClasses){
            Collection<IDepJar> depJars = hostProjectInfo.getUsedDepFromClass(className);
            if(!depJars.isEmpty()) {
                log.warn("Duplicate Class Smell: " + className);
                output("Duplicate Class Smell: " + className);
                for (IDepJar depJar : depJars) {
                    log.warn("in " + depJar.getSig());
                    output("in " + depJar.getSig());
                }
            }
        }
    }
}
