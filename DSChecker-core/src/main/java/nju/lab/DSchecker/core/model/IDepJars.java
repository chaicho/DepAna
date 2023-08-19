package nju.lab.DSchecker.core.model;

import java.util.List;
import java.util.Set;

public interface IDepJars<T extends IDepJar> {
    /**
     * Get all the jar file paths used by the host project.
     */
    public List<String> getUsedJarPaths();
    /**
     * Get all the jar files used by the host project.
     */
    public Set<T> getUsedDepJars();

    /**
     * Get the jar file of the given groupId and the artifactId;
     */
    public T getSelectedDepJarById(String componentId);

    /**
     Get all the jar files of the given project.
     */
    public Set<T> getAllDepJar();

    public Set<T> getDepJarsWithScope(String scope);

    public Set<T> getDepJarsWithScene(String scene);
}