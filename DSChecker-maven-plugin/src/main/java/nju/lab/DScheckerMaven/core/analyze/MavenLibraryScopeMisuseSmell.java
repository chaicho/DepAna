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
        log.warn("actualCompileDepJars: " + actualCompileDepJars);
        log.warn("actualRuntimeDepJars: " + actualRuntimeDepJars);
        log.warn("actualTestDepJars: " + actualTestDepJars);
        log.warn("compileScopeDepJars: " + compileDepJars);

//        // Check if provided scope dep is also used at runtime scene, which means the scope should be compile.
//        Set<IDepJar> providedDepJarsUsedAtRuntime = new HashSet<>(providedDepJars);
//        providedDepJarsUsedAtRuntime.retainAll(actualRuntimeDepJars);
//
//        // Check if runtime scope dep is also used at compile scene, which means the scope should be compile.
//        Set<IDepJar> runtimeDepJarsUsedAtCompile = new HashSet<>(runtimeDepJars);
//        runtimeDepJarsUsedAtCompile.retainAll(actualCompileDepJars);

        for (IDepJar depJar : compileDepJarsUsedOnlyAtTest) {
            appendToResult("compile scope dep " + depJar.getName() + " is acutally used only at test scene");
            appendToResult("---------");
            log.error("compile scope dependency " + depJar.getName() + " is acutally used only at test scene");
        }
        for (IDepJar depJar : compileDepJarsUsedOnlyAtRuntime) {
            appendToResult("compile scope dep " + depJar.getName() + " is acutally used only at runtime scene");
            appendToResult("---------");
            log.error("compile scope dependency " + depJar.getName() + " is acutally used only at runtime scene");
        }
        for (IDepJar depJar : compileDepJarsUsedOnlyAtProvided) {
            appendToResult("compile scope dep " + depJar.getName() + " is acutally used only at provided scene");
            appendToResult("---------");
            log.error("compile scope dependency " + depJar.getName() + " is acutally used only at provided scene");
        }
//        for (IDepJar depJar : providedDepJarsUsedAtRuntime) {
//            appendToResult("provided scope dep " + depJar.getName() + " is also used at runtime scene, should be compile scope");
//            appendToResult("---------");
//            log.error("provided scope dependency " + depJar.getName() + " is also used at runtime scene");
//        }
//        for (IDepJar depJar : runtimeDepJarsUsedAtCompile) {
//            appendToResult("runtime scope dep " + depJar.getName() + " is also used at compile scene, should be compile scope");
//            appendToResult("---------");
//            log.error("runtime scope dependency " + depJar.getName() + " is also used at compile scene");
//        }

        return;
    }

}
