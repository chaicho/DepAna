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
        Set<? extends IDepJar> compileDepJars = depJars.getDirectDepJarsWithScene("compile");

        Set<IDepJar> projectABIDepJars = hostProjectInfo.getABIDepJars();

        // Get DepJars with their used scenario.
        Set<IDepJar> actualTestDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("test");
        Set<IDepJar> actualCompileDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("compile");
        Set<IDepJar> actualRuntimeDepJars = hostProjectInfo.getActualDepJarsUsedAtScene("runtime");
        if (hostProjectInfo.useJavaLibraryPlugin()) {
            for (IDepJar depJar : projectABIDepJars) {
                if (implementationDepJars.contains(depJar)) {
                    appendToResult("implementation scope dep " + depJar.getName() + " is acutally used only at api scene");
                    appendToResult(depJar.getUsedClassesAsString());
                    appendToResult("---------");
                }
            }
        }
        for (IDepJar depJar : actualRuntimeDepJars) {
            if (compileOnlyDepJars.contains(depJar)) {
                appendToResult("compileOnly scope dep " + depJar.getName() + " is acutally used only at implementation scene");
                appendToResult(depJar.getUsedClassesAsString());
                appendToResult("---------");
            }
        }

        Set<IDepJar> compileDepJarsUsedOnlyAtTest = new HashSet<>(compileDepJars);
        compileDepJarsUsedOnlyAtTest.removeAll(actualCompileDepJars);
        compileDepJarsUsedOnlyAtTest.removeAll(actualRuntimeDepJars);
        compileDepJarsUsedOnlyAtTest.retainAll(actualTestDepJars);

        for (IDepJar depJar : compileDepJarsUsedOnlyAtTest) {
            appendToResult("compile scope dep " + depJar.getName() + " is acutally used only at test scene");
            appendToResult(depJar.getUsedClassesAsString());
            appendToResult("---------");
        }

        return;
    }

}
