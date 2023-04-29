package nju.lab.DSchecker.core.model;

import java.util.List;
import java.util.Set;

public interface IDepJars {

    /**
     * Get all the jar file paths used by the host project.
     */
    public List<String> getUsedJarPaths();
    /**
     * Get all the jar files used by the host project.
     */
    public Set<IDepJar> getUsedDepJars();

    /**
    * Get the jar file of the given name;
    */
    public IDepJar getSelectedDepJarById(String componentId);

    /**
        Get all the jar files of the given project.
     */
    public Set<IDepJar> getAllDepJar();

}