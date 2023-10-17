package nju.lab.DScheckerMaven.core.analyze;

import nju.lab.DSchecker.core.analyze.BaseSmell;
import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DSchecker.core.model.IHostProjectInfo;
import nju.lab.DScheckerMaven.model.DepJar;
import nju.lab.DScheckerMaven.util.GradleDependencyTreeFetcher;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GradleConflictDepSmell extends BaseSmell {
    @Override
    public void detect() {
        appendToResult("========GradleConflictDepSmell========");
        String modulePath = hostProjectInfo.getModulePath();
        Set<IDepJar> firstLevelDepJars =  depJars.getUsedDepJars().stream().filter(depJar -> depJar.getDepth() == 1).collect(Collectors.toSet());
        Map<String,String> depVersionMap = GradleDependencyTreeFetcher.getDepVersFromProject(modulePath,"compileClassPath");
        for (String dep : depVersionMap.keySet()) {
            for (IDepJar depJar : firstLevelDepJars) {
                if (depJar.getName().equals(dep)) {
                    if (!depJar.getVersion().equals(depVersionMap.get(dep))) {
                        appendToResult("Dependency " + depJar.getName() + " has inconsistent versions between modules." );
                        appendToResult("    Maven : " + depJar.getVersion());
                        appendToResult("    Gradle: " + depVersionMap.get(dep));
                    }
                }
            }
        }
    }
}
