package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IDepJar;

import java.util.Collection;

@Slf4j
public class ClassConflictSmell extends BaseSmell {

    @Override
    public void detect(){
        Collection<String> duplicateClassNames = hostProjectInfo.getDuplicateClassNames();
        for(String className : duplicateClassNames){
            Collection<IDepJar> depJars = hostProjectInfo.getUsedDepFromClass(className);
            log.warn("Duplicate Class Smell: " + className);
            for(IDepJar depJar : depJars){
               log.warn(   "in " + depJar.getName());
            }
        }
    }

}
