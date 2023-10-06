package nju.lab.DScheckerMaven.model;

import org.apache.maven.model.Dependency;

public class GroupArtifact {
    public String groupId;
    public String artifactId;

    public GroupArtifact(String groupId, String artifactId){
        this.groupId = groupId;
        this.artifactId = artifactId;
    }
    public GroupArtifact(Dependency dependency){
        this.groupId = dependency.getGroupId();
        this.artifactId = dependency.getArtifactId();
    }

}
