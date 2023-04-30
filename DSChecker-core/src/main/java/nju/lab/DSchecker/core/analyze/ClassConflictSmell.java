package nju.lab.DSchecker.core.analyze;

import nju.lab.DSchecker.core.model.IDepJar;

import java.util.Collection;

public class ClassConflictSmell extends BaseSmell {

    @Override
    public void detect(){
        Collection<String> duplicateClassNames = hostProjectInfo.getDuplicateClassNames();
        for(String className : duplicateClassNames){
            Collection<IDepJar> depJars = hostProjectInfo.getUsedDepFromClass(className);
            System.out.println("Duplicate Class Smell: " + className);
            for(IDepJar depJar : depJars){
               System.out.println(   "in " + depJar.getName());
            }
        }
    }

}
