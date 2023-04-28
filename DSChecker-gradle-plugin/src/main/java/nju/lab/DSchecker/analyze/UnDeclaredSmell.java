package nju.lab.DSchecker.analyze;

import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import nju.lab.DSchecker.model.DepJar;
import nju.lab.DSchecker.model.HostProjectInfo;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import  nju.lab.DSchecker.util.soot.javassist.GetRefedClasses;

public class UnDeclaredSmell implements BaseSmell {
    public static UnDeclaredSmell instance;
    private UnDeclaredSmell() {
    }
    public static UnDeclaredSmell i() {
        if (instance == null) {
            instance = new UnDeclaredSmell();
        }
        return instance;
    }

    @Override
    public void detect(){
//        Get the classes from the build directory of Host Project and analyze the refed classes of them.
        Set<String> referencedClasses =  GetRefedClasses.analyzeReferencedClasses(HostProjectInfo.i().getBuildCp());

        for (String refClass : referencedClasses) {
            /* Get the dependency jar containing the refed class */
            Collection<DepJar> dep = HostProjectInfo.i().getUsedDepFromClass(refClass);

           if(dep.size() == 0){
//               ERROR: Unable to find jar containing class.
               // Mostly the case where standard Java libraries are used;
               continue;
           }

           /* Since there are possibly several Depjars containing the same class ,we select the closest one */
           DepJar closestDep =  dep.stream()
                    .min(Comparator.comparingInt(DepJar::getDepth))
                    .orElse(null); // 如果dep集合为空，则返回null

            if(closestDep != null ){
                if(closestDep.getDepth() > 0){
                    /* If the closest dependency is not the directly Declared Dependency, then it is an undeclared dependency */
                    System.out.println("UnDeclared Smell : " + refClass + " in " + closestDep.getName());
                }
            }
        }
    }
//    public
}
