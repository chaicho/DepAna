package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IDepJar;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ClassConflictSmell extends BaseSmell {

    @Override
    public void detect() {
        appendToResult("========ClassConflictSmell========");
        Collection<String> duplicateClassNames = hostProjectInfo.getDuplicateClassNames();
        Map<Set<IDepJar>,Set<String>> jarToDuplicateClassMap = new HashMap<Set<IDepJar>,Set<String>>();
        for (String className : duplicateClassNames) {
            if (!validClass(className)) {
                continue;
            }
            Collection<IDepJar> depJars = hostProjectInfo.getUsedDepFromClass(className);
            if (depJars.size() == 1 && containsHost(depJars)) {
//                  The conflicting classes are in host jar, not in dependency.
                continue;
            }
            Set<IDepJar> depJarSets = new HashSet<IDepJar>();
            for(IDepJar depJar : depJars){
                if(depJar.isHost()){
                    continue;
                }
                depJarSets.add(depJar);
            }
            if(!jarToDuplicateClassMap.containsKey(depJarSets)){
                jarToDuplicateClassMap.put(depJarSets,new HashSet<String>());
            }
            jarToDuplicateClassMap.get(depJarSets).add(className);
        }

        for(Set<IDepJar> depJarSets : jarToDuplicateClassMap.keySet()){
            Set<String> duplicateClasses = jarToDuplicateClassMap.get(depJarSets);
            for (IDepJar depJar : depJarSets) {
                    log.warn("Dep " + depJar.getSig());
                    appendToResult("Dep " + depJar.getSig());
                    appendToResult("    Pulled in by: " + depJar.getDepTrail());

            }
            appendToResult("Contains Duplicate Classes: " + duplicateClasses.size());
            for (String className : duplicateClasses) {
                log.warn("      Duplicate Class Smell: " + className);
                appendToResult("    " + className);
            }

            appendToResult("---------");
        }
    }

}
