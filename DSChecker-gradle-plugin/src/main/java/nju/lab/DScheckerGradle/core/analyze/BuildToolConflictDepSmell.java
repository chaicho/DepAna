package nju.lab.DScheckerGradle.core.analyze;

import nju.lab.DSchecker.core.analyze.BaseSmell;
import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DScheckerGradle.util.MavenDependencyTreeFetcher;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BuildToolConflictDepSmell extends BaseSmell {
    @Override
    public void detect() {
        appendToResult("========BuildToolConflictDepSmell========");
        String rootPath = hostProjectInfo.getRootDir().getAbsolutePath();
        File gradleFile = new File(rootPath + File.separator + "pom.xml");
        if (!gradleFile.exists()) {
            appendToResult("The project is managed by gradle only.");
            return;
        }

        String modulePath = hostProjectInfo.getModulePath();
        Set<IDepJar> firstLevelDepJars =  depJars.getAllDepJar().stream().filter(depJar -> depJar.getDepth() == 1).collect(Collectors.toSet());
        Map<String,String> depVersionMap = MavenDependencyTreeFetcher.getDepVersFromProject(modulePath);
        for (IDepJar depJar : depJars.getAllDepJar()) {
            System.out.println(depJar.getName());
            System.out.println(depJar.getVersion());
        }
        System.out.println("---------");
        for (String dep : depVersionMap.keySet()) {
            System.out.println(dep);
            System.out.println(depVersionMap.get(dep));
            for (IDepJar depJar : firstLevelDepJars) {
                if (depJar.getName().equals(dep)) {
                    if (!depJar.getVersion().equals(depVersionMap.get(dep))) {
                        appendToResult("Dependency " + depJar.getName() + " has inconsistent versions between modules." );
                        appendToResult("    Maven : " + depJar.getVersion());
                        appendToResult("    Gradle: " + depVersionMap.get(dep));
                        appendToResult("---------");
                    }
                }
            }
        }
    }
}
