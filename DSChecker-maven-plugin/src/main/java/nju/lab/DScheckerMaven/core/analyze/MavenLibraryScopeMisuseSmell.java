package nju.lab.DScheckerMaven.core.analyze;
import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.analyze.BaseSmell;
import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DSchecker.util.javassist.GetRefedClasses;
import nju.lab.DScheckerMaven.model.DepJar;
import nju.lab.DScheckerMaven.model.HostProjectInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class MavenLibraryScopeMisuseSmell extends BaseSmell{
    @Override
    public void detect(){
        appendToResult("========LibraryScopeSmell========");
        // Get DepJars with their declared scope.
        Set<? extends IDepJar> testDepJars = depJars.getDepJarsWithScope("test");
        Set<? extends IDepJar> compileDepJars = depJars.getDepJarsWithScope("compile");
        Set<? extends IDepJar> runtimeDepJars = depJars.getDepJarsWithScope("runtime");
        Set<? extends IDepJar> providedDepJars = depJars.getDepJarsWithScope("provided");

        // Get DepJars with their used scenario.
        Set<IDepJar> actualTestDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("test");
        Set<IDepJar> actualCompileDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("compile");
        Set<IDepJar> actualRuntimeDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("runtime");

        // Check if compile scope dep is acutally used only at test scene
        Set<IDepJar> compileDepJarsUsedOnlyAtTest = new HashSet<>(compileDepJars);
        compileDepJarsUsedOnlyAtTest.removeAll(actualCompileDepJars);
        compileDepJarsUsedOnlyAtTest.removeAll(actualRuntimeDepJars);
        compileDepJarsUsedOnlyAtTest.retainAll(actualTestDepJars);

        // Check if compile scope dep is acutally used only at runtime scene
        Set<IDepJar> compileDepJarsUsedOnlyAtRuntime = new HashSet<>(compileDepJars);
        compileDepJarsUsedOnlyAtRuntime.removeAll(actualCompileDepJars);
        compileDepJarsUsedOnlyAtRuntime.removeAll(actualTestDepJars);
        compileDepJarsUsedOnlyAtRuntime.retainAll(actualRuntimeDepJars);

        // Check if compile scope dep is acutally used only at provided scene
        Set<IDepJar> compileDepJarsUsedOnlyAtProvided = new HashSet<>(compileDepJars);
        compileDepJarsUsedOnlyAtProvided.removeAll(actualRuntimeDepJars);
        compileDepJarsUsedOnlyAtProvided.removeAll(actualTestDepJars);
        compileDepJarsUsedOnlyAtProvided.retainAll(actualCompileDepJars);

        for (IDepJar depJar : compileDepJarsUsedOnlyAtTest) {
            appendToResult("compile scope dep " + depJar.getName() + " is acutally used only at test scene");
            log.error("compile scope dependency " + depJar.getName() + " is acutally used only at test scene");
        }
        for (IDepJar depJar : compileDepJarsUsedOnlyAtRuntime) {
            appendToResult("compile scope dep " + depJar.getName() + " is acutally used only at runtime scene");
            log.error("compile scope dependency " + depJar.getName() + " is acutally used only at runtime scene");
        }
        for (IDepJar depJar : compileDepJarsUsedOnlyAtProvided) {
            appendToResult("compile scope dep " + depJar.getName() + " is acutally used only at provided scene");
            log.error("compile scope dependency " + depJar.getName() + " is acutally used only at provided scene");
        }

        return;
    }

}
