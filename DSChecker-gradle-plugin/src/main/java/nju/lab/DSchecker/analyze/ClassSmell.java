package nju.lab.DSchecker.analyze;

import neu.lab.conflict.container.DepJars;
import nju.lab.DSchecker.model.DepJar;
import nju.lab.DSchecker.model.HostProjectInfo;

import java.util.Collection;

public class ClassSmell implements BaseSmell {
    public static  ClassSmell instance;
    private ClassSmell() {
    }
    public static ClassSmell i() {
        if (instance == null) {
            instance = new ClassSmell();
        }
        return instance;
    }

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
