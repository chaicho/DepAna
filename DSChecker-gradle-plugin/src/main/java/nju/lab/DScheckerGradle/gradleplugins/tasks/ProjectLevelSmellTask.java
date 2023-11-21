package nju.lab.DScheckerGradle.gradleplugins.tasks;

import nju.lab.DSchecker.core.analyze.SmellFactory;
import nju.lab.DSchecker.core.analyze.WrapperConfMissingSmell;
import nju.lab.DSchecker.core.analyze.WrapperJarAbnormalSmell;
import nju.lab.DSchecker.core.analyze.WrapperJarMissingSmell;
import nju.lab.DSchecker.core.model.DepModel;
import nju.lab.DSchecker.util.soot.TypeAna;
import nju.lab.DSchecker.util.source.analyze.FullClassExtractor;
import nju.lab.DScheckerGradle.core.analyze.GradleConflictLibrarySmell;
import nju.lab.DScheckerGradle.core.analyze.GradleSharedLibrarySmell;
import nju.lab.DScheckerGradle.model.DepJars;
import nju.lab.DScheckerGradle.model.HostProjectInfo;
import nju.lab.DScheckerGradle.model.callgraph.MyCallGraph;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;

public abstract class ProjectLevelSmellTask extends BaseConflictTask {
    void initValues() {
        project = getProject();
        mainSourceSet = project.getExtensions().getByType(SourceSetContainer.class)
                .getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        testSourceSet = project.getExtensions().getByType(SourceSetContainer.class)
                .getByName(SourceSet.TEST_SOURCE_SET_NAME);
        compileSrcDirs = mainSourceSet.getAllJava().getSrcDirs();
        // Get the output of the main source set
        mainOutput = mainSourceSet.getOutput();
        // Get the classes directories of the main output
        classesDirs = mainOutput.getClassesDirs();
        buildDir = project.getBuildDir();
    }
    @Override
    void execute() throws Exception {
        if (getProject().getParent() != null) {
            return;
        }
        System.out.println("ProjectLevelSmellTask");
        initValues();

        HostProjectInfo.i().setResultFileName("DScheckerResultProjectLevel.txt");
        HostProjectInfo.i().setCompileSrcFiles(compileSrcDirs);
        HostProjectInfo.i().setClassesDirs(classesDirs);
        HostProjectInfo.i().setBuildDir(buildDir);
        HostProjectInfo.i().setRootDir(project.getRootDir());
        HostProjectInfo.i().setModuleFile(project.getProjectDir());
        HostProjectInfo.i().setBuildTestCp(project.getBuildDir().getAbsoluteFile() + File.separator + "test-classes");
        HostProjectInfo.i().setTestCompileSrcFiles(testSourceSet.getAllJava().getSrcDirs());
        HostProjectInfo.i().init(MyCallGraph.i(), DepJars.i());

        DepModel depModel = new DepModel(MyCallGraph.i(), DepJars.i(), HostProjectInfo.i());

        SmellFactory smellFactory = new SmellFactory();
        smellFactory.initOnly(HostProjectInfo.i(), DepJars.i(), MyCallGraph.i());
        WrapperConfMissingSmell wrapperConfMissingSmell = new WrapperConfMissingSmell();
        WrapperJarMissingSmell wrapperJarMissingSmell = new WrapperJarMissingSmell();
        WrapperJarAbnormalSmell wrapperJarAbnormalSmell = new WrapperJarAbnormalSmell();
        GradleSharedLibrarySmell gradleSharedLibrarySmell = new GradleSharedLibrarySmell(project, project.getChildProjects());
        GradleConflictLibrarySmell gradleConflictLibrarySmell = new GradleConflictLibrarySmell(project, project.getChildProjects());
        smellFactory.addSmell(wrapperConfMissingSmell);
        smellFactory.addSmell(wrapperJarMissingSmell);
        smellFactory.addSmell(wrapperJarAbnormalSmell);
        smellFactory.addSmell(gradleSharedLibrarySmell);
        smellFactory.addSmell(gradleConflictLibrarySmell);
        smellFactory.detectAll();
        return;
    }
}
