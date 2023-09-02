package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IDepJar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class LibraryClassConflictSmell extends BaseSmell {

    @Override
    public void detect() {
        appendToResult("========LibraryClassConflictSmell========");
        Collection<String> duplicateClassNames = hostProjectInfo.getDuplicateClassNames();
        Map<List<IDepJar>,Set<String>> jarToDuplicateClassMap = new HashMap<List<IDepJar>,Set<String>>();
        for (String className : duplicateClassNames) {
            if (!validClass(className)) {
                continue;
            }
            Collection<IDepJar> depJars = hostProjectInfo.getUsedDepFromClass(className);
            if (depJars.size() == 1 && containsHost(depJars)) {
//                  The conflicting classes are in host jar, not in dependency.
                continue;
            }
            List<IDepJar> depJarSets = new ArrayList<>();
            Boolean containsHost = false;
            for(IDepJar depJar : depJars){
                if(depJar.isHost()){
                    containsHost = true;
                    break;
                }
                depJarSets.add(depJar);
            }
            if(containsHost){
                continue;
            }
            if(!jarToDuplicateClassMap.containsKey(depJarSets)){
                jarToDuplicateClassMap.put(depJarSets,new HashSet<String>());
            }
            jarToDuplicateClassMap.get(depJarSets).add(className);
        }

        for(List<IDepJar> depJarLists : jarToDuplicateClassMap.keySet()){
            Set<String> duplicateClasses = jarToDuplicateClassMap.get(depJarLists);
            duplicateClasses.stream().filter(className -> hostProjectInfo.isUsedByHost(className));
            if (duplicateClasses.isEmpty()) {
                return;
            }
            for (IDepJar depJar : depJarLists) {
                log.warn("Dep " + depJar.getSig());
                appendToResult("Dep " + depJar.getSig());
                appendToResult("    Pulled in by: " + depJar.getUsedDepTrail());
            }
            appendToResult("Contains Duplicate Classes Used By the project: " + duplicateClasses.size());
            for (String className : duplicateClasses) {
                log.warn("      Duplicate Class Smell: " + className);
                appendToResult("    " + className);
            }
            appendToResult("Acutal Used Dependency" + depJarLists.get(0));
            appendToResult("---------");
        }
    }

}
