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
    String getName();
    Set<String> getAllCls();


    int getDepth();
    List<String> getJarFilePaths();

    boolean isSelected();

    boolean isHost();

    String getDisplayName();

    String getUsedDepTrail();

    String getDepTrail();

    String getScope();

    Map<String, Set<String> > getUsedClasses();
    Set<String> getUsedClassesAtScene(String scene);
    void addClassToScene(String scene, String cls);
    public String getUsedClassesAsString();
}
