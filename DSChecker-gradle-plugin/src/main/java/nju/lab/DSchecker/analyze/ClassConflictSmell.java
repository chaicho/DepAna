package nju.lab.DSchecker.analyze;

import nju.lab.DSchecker.model.DepJar;
import nju.lab.DSchecker.model.HostProjectInfo;

import java.util.Collection;

public class ClassConflictSmell implements BaseSmell {

    @Override
    public void detect(){
        Collection<String> duplicateClassNames = HostProjectInfo.i().getDuplicateClassNames();
        for(String className : duplicateClassNames){
            Collection<DepJar> depJars = HostProjectInfo.i().getUsedDepFromClass(className);
            System.out.println("Duplicate Class Smell: " + className);
            for(DepJar depJar : depJars){
               System.out.println(   "in " + depJar.getName());
            }
        }
    }

}
