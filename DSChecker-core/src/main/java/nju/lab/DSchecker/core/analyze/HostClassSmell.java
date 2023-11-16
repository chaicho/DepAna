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
public class HostClassSmell extends BaseSmell{

    @Override
    public void detect(){
        appendToResult("========HostClassConflictSmell========");
        List<String> hostClasses= hostProjectInfo.getHostClasses();
        Map<List<IDepJar>,Set<String>> jarToDuplicateClassMap = new HashMap<List<IDepJar>,Set<String>>();
        for(String hostClass : hostClasses){
            if (!validClass(hostClass)){
                continue;
            }
            Collection<IDepJar> depJars = hostProjectInfo.getUsedDepFromClass(hostClass);
            if((depJars.size() == 1 && containsHost(depJars)) || depJars.size() == 0){
                continue;
            }
            List<IDepJar> depJarSets = new ArrayList<>();
            depJarSets.addAll(depJars);
            if(!jarToDuplicateClassMap.containsKey(depJarSets)){
                jarToDuplicateClassMap.put(depJarSets,new HashSet<String>());
            }
            jarToDuplicateClassMap.get(depJarSets).add(hostClass);
        }

        for(List<IDepJar> depJarSets : jarToDuplicateClassMap.keySet()) {
            Set<String> duplicateClasses = jarToDuplicateClassMap.get(depJarSets);
            appendToResult("Host Project");
            for (IDepJar depJar : depJarSets) {
                log.warn("Dep " + depJar.getSig());
                appendToResult("Dep " + depJar.getSig());
                appendToResult("    Pulled in by: " + depJar.getUsedDepTrail());
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
