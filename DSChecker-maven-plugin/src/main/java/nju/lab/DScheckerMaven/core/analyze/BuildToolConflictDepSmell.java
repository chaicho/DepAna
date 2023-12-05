package nju.lab.DScheckerMaven.core.analyze;

import nju.lab.DSchecker.core.analyze.BaseSmell;
import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DScheckerMaven.util.GradleDependencyTreeFetcher;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BuildToolConflictDepSmell extends BaseSmell {
    @Override
    public void detect() {
        appendToResult("========GradleConflictDepSmell========");
        String rootPath = hostProjectInfo.getRootDir().getAbsolutePath();
        File gradleFile = new File(rootPath + File.separator + "build.gradle");
        if (!gradleFile.exists()) {
            appendToResult("The project is managed by maven only.");
            return;
        }

        String modulePath = hostProjectInfo.getModulePath();
        Set<IDepJar> firstLevelDepJars =  depJars.getUsedDepJars().stream().filter(depJar -> depJar.getDepth() == 1).collect(Collectors.toSet());
        Map<String,String> depVersionMap = GradleDependencyTreeFetcher.getDepVersFromProject(modulePath,"testCompileClasspath");
        Map<String,String> depVersionMap1 = GradleDependencyTreeFetcher.getDepVersFromProject(modulePath,"runtimeClasspath");
        Map<String,String> depVersionMap2 = GradleDependencyTreeFetcher.getDepVersFromProject(modulePath,"compileClasspath");
        depVersionMap.putAll(depVersionMap2);
        depVersionMap.putAll(depVersionMap1);
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
