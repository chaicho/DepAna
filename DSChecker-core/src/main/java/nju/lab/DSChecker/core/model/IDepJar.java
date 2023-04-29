package nju.lab.DSchecker.core.model;

import java.util.List;
import java.util.Set;

public interface IDepJar {

    String getName();

    Set<String> getAllCls();

    int getDepth();
    List<String> getJarFilePaths();
}
