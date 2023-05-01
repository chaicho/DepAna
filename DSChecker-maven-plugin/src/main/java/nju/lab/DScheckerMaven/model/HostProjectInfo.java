package nju.lab.DScheckerMaven.model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import nju.lab.DSchecker.core.model.*;
import soot.SourceLocator;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
                System.out.println("consumerClass: " + consumerClass.getClsSig());
            });
        }
    }

    public void setCompileSrcPaths(List<String> paths) {
            this.compileSrcDirs = paths.stream().map(String::trim).collect(Collectors.toSet());
            this.compileSrcFiles = compileSrcDirs.stream()
                                .map(File::new)
                                .collect(Collectors.toSet());

    }

}
