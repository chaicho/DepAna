package nju.lab.DScheckerGradle.model;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.artifacts.ResolvedArtifact;
//import org.gradle.api.artifacts.result.Resol;
import lombok.Data;
import org.gradle.api.artifacts.component.ComponentSelector;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.gradle.api.artifacts.result.ComponentSelectionCause.COMPOSITE_BUILD;
import static org.gradle.api.artifacts.result.ComponentSelectionCause.CONFLICT_RESOLUTION;

@Data
@Setter
@Slf4j
public class NodeAdapter {

    private static int counter = 0;



    public static int incCnt() {
        return  counter++;
    }
    private final int depth;
    private int nodeId;
    /*
        * 依赖树节点的显示名,如果存在多个版本的依赖,那么requested和selected的显示名不一样，但是resolvedComponentResult可能是一样的
     */
    private final String displayName;
    private final ComponentSelector selector;
    /**
     * 依赖树节
     */
    protected Set<ResolvedArtifact> artifacts;
    protected ResolvedComponentResult componentResult;
    protected ResolvedDependencyResult dependencyResult;
    /**
     * 关联的Jar包数据结
     */
    protected DepJar depJar;
    /**
     * jar包本地路径（其实这个list大小
     */
    protected List<String> filePaths = new ArrayList<>();
    private String version;
    private String group;
    private String name;


    public NodeAdapter(ResolvedComponentResult component, Set<ResolvedArtifact> artifact, ResolvedDependencyResult dep, int depth, ComponentSelector selector) {
        this.artifacts = artifact;
        this.dependencyResult = dep;
        this.componentResult = component;
        this.depth = depth;
        this.selector = selector;
        this.displayName = selector.getDisplayName();
        this.nodeId = incCnt();
        if(isNodeSelected()){
            this.group = componentResult.getModuleVersion().getGroup();
            this.name = componentResult.getModuleVersion().getName();
            this.version = componentResult.getModuleVersion().getVersion();
            if (version == "unspecified" && displayName.split(":").length == 3) {
                this.version = displayName.split(":")[2];
            }
        }
        else {
            if (displayName.split(":").length != 3) {
                log.warn("Unstandard Display name");
                this.group = displayName.split(":")[0];
                this.name = displayName.split(":")[1];
                this.version = Objects.requireNonNull(componentResult.getModuleVersion()).getVersion();
            } else {
                this.group = displayName.split(":")[0];
                this.name = displayName.split(":")[1];
                this.version = displayName.split(":")[2];
            }
        }

    }
    public NodeAdapter(ResolvedComponentResult selected, Set<ResolvedArtifact> artifacts, ResolvedDependencyResult resolvedDependency, int dep, ComponentSelector requested, NodeAdapter parent) {
        this(selected, artifacts, resolvedDependency, dep, requested);
        this.parent = parent;
    }
    public NodeAdapter parent;

    public String getGroupId() {
        return group;
    }
    public String getArtifactId() {

        return name;
    }
    public String getVersion() {
        return version;
    }
    public String getClassifier() {
        String classifier = "";
        if(artifacts == null){
            return  classifier;
        }
        try {
             classifier = artifacts.iterator().next().getClassifier();
        }
        catch (Exception e){
            log.warn("No classifier");
        };
        return classifier != null ? classifier : "";
    }
    public void setArtifacts(Set<ResolvedArtifact> artifacts) {
        this.artifacts = artifacts;
    }
    public List<String> getFilePath() {
        if(artifacts == null){
            return filePaths;
        }
        if(filePaths.isEmpty()) {
           artifacts.forEach(artifact -> {
                try {
                    filePaths.add(artifact.getFile().getCanonicalPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return filePaths;
    }
    public void addFilePath(String path){
        filePaths.add(path);
        return;
    }

    public int getDepth() {
        return depth;
    }
    public String getValidDepPath() {
        StringBuilder sb = new StringBuilder();


        return toString();
    }
    /**
     * @return groupId:artifactId:version:classifier
     */
    @Override
    public String toString() {
      return displayName;
//        return getGroupId() + ":" + getArtifactId() + ":" + getVersion() + ":" + getClassifier();
    }
    /**
     * @return groupId:artifactId:version
     */

    public void setDepJar(DepJar depJar) {
        this.depJar = depJar;
    }

    public boolean isSameLib(NodeAdapter nodeAdapter) {
        return getGroupId().equals(nodeAdapter.getGroupId()) && getArtifactId().equals(nodeAdapter.getArtifactId());
    }

    public boolean equals(Object obj) {
        if (obj instanceof NodeAdapter) {
            NodeAdapter nodeAdapter = (NodeAdapter) obj;
            return displayName.equals(nodeAdapter.displayName) && depth == nodeAdapter.depth && nodeId == nodeAdapter.nodeId;
        }
        return false;
    }
    public int hashCode() {
        return Objects.hash(displayName,depth,nodeId);
    }

    public boolean isNodeSelected() {
        boolean containsConflictResolution = dependencyResult
                .getSelected()
                .getSelectionReason()
                .getDescriptions()
                .stream()
                .filter(desc -> desc.getCause() != null)
                .anyMatch(desc -> desc.getCause().equals(CONFLICT_RESOLUTION));
        boolean containsCompositeBuildResolution = dependencyResult
                .getSelected()
                .getSelectionReason()
                .getDescriptions()
                .stream()
                .filter(desc -> desc.getCause() != null)
                .anyMatch(desc -> desc.getCause().equals(COMPOSITE_BUILD));
        boolean isVersionRange = dependencyResult.getRequested().getDisplayName().contains("{");
        if (isVersionRange) {
            return true;
        }
        if (containsCompositeBuildResolution) {
            return true;
        }
        if(containsConflictResolution) {
            return dependencyResult.getRequested().matchesStrictly(dependencyResult.getSelected().getId());
        }
        return dependencyResult.getRequested().matchesStrictly(dependencyResult.getSelected().getId());
    }

    public String getWholePath() {
        StringBuilder sb = new StringBuilder(getDisplayName());
        NodeAdapter father = getParent();
        while (null != father) {
            sb.insert(0, father.getDisplayName() + " -> ");
            father = father.getParent();
        }
        return sb.toString();
    }

    public boolean sameGAV(NodeAdapter nodeAdapter) {
        return getGroupId().equals(nodeAdapter.getGroupId()) && getArtifactId().equals(nodeAdapter.getArtifactId()) && getVersion().equals(nodeAdapter.getVersion());
    }
    public int getPriority() {
        if (isNodeSelected()) {
            return 1;
        }
        return  0;
    }
    public DepJar getDepJar() {
        return depJar;
    }


}
