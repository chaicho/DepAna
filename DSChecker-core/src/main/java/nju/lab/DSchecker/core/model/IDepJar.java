package nju.lab.DSchecker.core.model;

import java.util.List;
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

    String getDisplayName();

    String getDepTrail();
}
