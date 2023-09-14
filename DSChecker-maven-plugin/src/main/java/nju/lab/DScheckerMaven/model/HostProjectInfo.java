package nju.lab.DScheckerMaven.model;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.*;
import nju.lab.DSchecker.util.javassist.GetRefedClasses;
import nju.lab.DSchecker.util.source.analyze.ImportExtractor;
import soot.SourceLocator;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class HostProjectInfo extends IHostProjectInfo {
    private static HostProjectInfo instance;
    private HostProjectInfo() {
    }

    public static HostProjectInfo i() {
        if (instance == null) {
            instance = new HostProjectInfo();
        }
        return instance;
    }

    @Override
    public void buildDepClassMap() {
        super.buildDepClassMap();
        for ( String compileSrcDir: compileSrcDirs){
            consumerClasses.addAll(SourceLocator.v().getClassesUnder(compileSrcDir)
                    .stream()
                    .map(ClassVO::new)
                    .collect(Collectors.toSet()));
            consumerClasses.forEach(consumerClass -> {
                log.warn("consumerClass: " + consumerClass.getClsSig());
            });
        }
    }
    @Override
    public String getWrapperPath(){
        return rootDir.getPath() + File.separator + ".mvn" + File.separator + "wrapper";
    }

    @Override
    public String getBuildCp() {
        return outputDir.getAbsolutePath();
    }

    @Override
    public String getBuildTestCp() {
        return testOutputDir.getAbsolutePath();
    }
    public void setCompileSrcPaths(List<String> paths) {
            this.compileSrcDirs = paths.stream().map(String::trim).collect(Collectors.toSet());
            this.compileSrcFiles = compileSrcDirs.stream()
                                .map(File::new)
                                .collect(Collectors.toSet());

    }
    @Override
    public String getBuildTool() {
        return "maven";
    }


}
