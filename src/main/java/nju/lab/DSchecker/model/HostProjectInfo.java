package nju.lab.DSchecker.model;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class HostProjectInfo {
    private static HostProjectInfo instance;
    private File buildDir;
    private String buildPath;

    private HostProjectInfo() {
    }

    public static HostProjectInfo i() {
        if (instance == null) {
            instance = new HostProjectInfo();
        }
        return instance;
    }

    Set<File> compileSrcFiles ;


    List<String> compileSrcDirs = null;

    public Set<File> getCompileSrcFiles() {
        return compileSrcFiles;
    }
    public void setCompileSrcFiles(Set<File> compileSrcFiles) {
        this.compileSrcFiles = compileSrcFiles;
    }
    public List<String> getCompileSrcDirs() {
        if (compileSrcDirs != null)
            return compileSrcDirs;
        compileSrcDirs = compileSrcFiles.stream().map(File::getAbsolutePath).collect(Collectors.toList());
        return compileSrcDirs;
    }
    public String getCompileSrcCp(){
        return String.join(";", getCompileSrcDirs());
    }

    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
        this.buildPath = buildDir.getAbsolutePath();
    }
    public String getBuildCp() {
        return buildPath + File.separator + "classes" + File.separator + "java" + File.separator + "main";
    }
}
