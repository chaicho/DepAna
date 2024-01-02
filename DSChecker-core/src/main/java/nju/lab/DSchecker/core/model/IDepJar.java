package nju.lab.DSchecker.core.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IDepJar {

    /**
     * Get the signature of the jar file (groupId:artifactId:version:classifier)
     * @return
     */
    String getSig();

    String getGroupId();
    String getArtifactId();
    /**
     * @return groupId:artifactId
     */
    String getName();
    String getVersion();
    Set<String> getAllCls();


    int getDepth();
    List<String> getJarFilePaths();

    boolean isSelected();

    boolean isHost();

    String getDisplayName();

    String getUsedDepTrail();

//    Get the first import path in the dependency tree
    String getDepTrail();
//  Get all the import paths in the dependency tree
    Set<String> getDepTrails();

    String getScope();

    Set<String> getScopes();

    Map<String, Set<String> > getUsedClasses();
    Set<String> getUsedClassesAtScene(String scene);
    void addClassToScene(String scene, String cls);
    public String getUsedClassesAsString();

    public String getGA();
}
