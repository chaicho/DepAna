package nju.lab.DScheckerGradle.core.analyze;
import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.analyze.BaseSmell;
import nju.lab.DSchecker.core.model.IDepJar;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class GradleLibraryScopeMisuseSmell extends BaseSmell{
    @Override
    public void detect(){
        appendToResult("========LibraryScopeSmell========");
        // Get DepJars with their declared scope.
        Set<? extends IDepJar> compileOnlyDepJars = depJars.getDirectDepJarsWithScope("compileOnly");
        Set<? extends IDepJar> apiDepJars = depJars.getDirectDepJarsWithScope("api");
        Set<? extends IDepJar> implementationDepJars = depJars.getDirectDepJarsWithScope("implementation");
        Set<? extends IDepJar> runtimeOnlyDepJars = depJars.getDirectDepJarsWithScope("runtimeOnly");
        Set<? extends IDepJar> testScopeDepJars = depJars.getDirectDepJarsWithScope("testImplementation");
        // testScopeDepJars.addAll(depJars.getDirectDepJarsWithScope("testCompileOnly").stream());
        // testScopeDepJars.addAll(depJars.getDirectDepJarsWithScope("testRuntimeOnly").stream());
        Set<IDepJar> projectABIDepJars = hostProjectInfo.getABIDepJars();

        // Get DepJars with their used scenario.
        Set<IDepJar> actualTestDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("test");
        Set<IDepJar> actualCompileDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("compile");
        Set<IDepJar> actualRuntimeDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("runtime");
        if (hostProjectInfo.useJavaLibraryPlugin()) {
            for (IDepJar depJar : projectABIDepJars) {
                if (implementationDepJars.contains(depJar)) {
                    appendToResult("implementation scope dep " + depJar.getDisplayName() + " is acutally used only at api scene");
                    appendToResult(depJar.getUsedClassesAsString());
                    appendToResult("---------");
                }
            }
        }
//        for (IDepJar depJar : actualRuntimeDepJars) {
//            if (compileOnlyDepJars.contains(depJar)) {
//                appendToResult("compileOnly scope dep " + depJar.getDisplayName() + " is acutally used only at implementation scene");
//                appendToResult(depJar.getUsedClassesAsString());
//                appendToResult("---------");
//            }
//        }

        Set<IDepJar> compileDepJarsUsedOnlyAtTest = new HashSet<>(implementationDepJars);
        compileDepJarsUsedOnlyAtTest.addAll(apiDepJars);
        compileDepJarsUsedOnlyAtTest.removeAll(actualCompileDepJars);
        compileDepJarsUsedOnlyAtTest.removeAll(actualRuntimeDepJars);
        compileDepJarsUsedOnlyAtTest.retainAll(actualTestDepJars);
        log.error("Test scope depJars");
        log.error(testScopeDepJars.toString());
        log.error("Pre compile2test depJars");
        log.error(compileOnlyDepJars.toString());
        removeDepJarsWithSameGA(compileDepJarsUsedOnlyAtTest, testScopeDepJars);
        log.error("After compile2test depJars");
        log.error(compileOnlyDepJars.toString());
        for (IDepJar depJar : compileDepJarsUsedOnlyAtTest) {
            appendToResult("compile scope dep " + depJar.getDisplayName() + " is acutally used only at test scene");
            appendToResult(depJar.getUsedClassesAsString());
            appendToResult("---------");
        }

        Set<IDepJar> compileDepJarsUsedOnlyAtProvided = new HashSet<>(implementationDepJars);
        compileDepJarsUsedOnlyAtProvided.addAll(apiDepJars);
        compileDepJarsUsedOnlyAtProvided.removeAll(actualTestDepJars);
        compileDepJarsUsedOnlyAtProvided.removeAll(actualRuntimeDepJars);
        compileDepJarsUsedOnlyAtProvided.retainAll(actualCompileDepJars);
        removeDepJarsWithSameGA(compileDepJarsUsedOnlyAtProvided, compileOnlyDepJars);

        for (IDepJar depJar : compileDepJarsUsedOnlyAtProvided) {
            appendToResult("compile scope dep " + depJar.getDisplayName() + " is acutally used only at provided scene");
            appendToResult(depJar.getUsedClassesAsString());
            appendToResult("---------");
        }

        Set<IDepJar> compileDepJarsUsedOnlyAtRuntime = new HashSet<>(implementationDepJars);
        compileDepJarsUsedOnlyAtRuntime.addAll(apiDepJars);
        compileDepJarsUsedOnlyAtRuntime.removeAll(actualCompileDepJars);
        compileDepJarsUsedOnlyAtRuntime.removeAll(actualTestDepJars);
        compileDepJarsUsedOnlyAtRuntime.retainAll(actualRuntimeDepJars);
        removeDepJarsWithSameGA(compileDepJarsUsedOnlyAtRuntime, runtimeOnlyDepJars);
        
        for (IDepJar depJar : compileDepJarsUsedOnlyAtRuntime) {
            appendToResult("compile scope dep " + depJar.getDisplayName() + " is acutally used only at runtime scene");
            appendToResult(depJar.getUsedClassesAsString());
            appendToResult("---------");
        }
        return;
    }

}
