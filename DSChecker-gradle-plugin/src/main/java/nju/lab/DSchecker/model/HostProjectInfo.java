package nju.lab.DSchecker.model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import nju.lab.DSchecker.core.model.*;
import soot.SourceLocator;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public class HostProjectInfo extends IHostProjectInfo {
    private static HostProjectInfo instance;
    private Set<String> apiDepJars = new HashSet<>();

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
            System.out.println(compileSrcDir);
            consumerClasses.addAll(SourceLocator.v().getClassesUnder(compileSrcDir)
                    .stream()
                    .map(ClassVO::new)
                    .collect(Collectors.toSet()));
            consumerClasses.forEach(consumerClass -> {
                System.out.println("consumerClass: " + consumerClass.getClsSig());
            });
        }

    }




}
