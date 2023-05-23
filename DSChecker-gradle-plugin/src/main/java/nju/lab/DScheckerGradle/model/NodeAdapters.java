package nju.lab.DScheckerGradle.model;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DScheckerGradle.util.GradleUtil;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ComponentSelector;
import org.gradle.api.artifacts.component.ModuleComponentSelector;
import org.gradle.api.artifacts.component.ProjectComponentSelector;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;

import java.util.*;

@Slf4j
public class NodeAdapters {
    private static NodeAdapters instance;

    public static NodeAdapters i() {
        return instance;
    }
    private List<NodeAdapter> container;
    public Map<ComponentIdentifier, ResolvedArtifact> artifactMap;
    public NodeAdapter rootNodeAdapter;
    private int NodeAdapterNum = 0;
    private NodeAdapters() {
        container = new ArrayList<NodeAdapter>();
    }

    public static void init(ResolvedComponentResult root, Map<ComponentIdentifier, Set<ResolvedArtifact>> newArtifactMap) {
        if(instance == null) {
            instance = new NodeAdapters();
            Set<ResolvedComponentResult> seen = new HashSet<>();
            walk(root,newArtifactMap,seen,1,null);
        }

    }
    public void addNodeAdapter(NodeAdapter nodeAdapter) {
        container.add(nodeAdapter);
    }
    private static void walk(ResolvedComponentResult component, Map<ComponentIdentifier, Set<ResolvedArtifact>> newArtifactMap, Set<ResolvedComponentResult> seen, int dep,NodeAdapter parent) {

        if (seen.add(component)) {
            for (DependencyResult dependency : component.getDependencies()) {
                if (dependency instanceof ResolvedDependencyResult) {
                    ResolvedDependencyResult resolvedDependency = (ResolvedDependencyResult) dependency;
                    ResolvedComponentResult selectedComponent = resolvedDependency.getSelected();
                    Set<ResolvedArtifact> artifacts = newArtifactMap.get(selectedComponent.getId());
                    NodeAdapter nodeAdapter =  new NodeAdapter(resolvedDependency.getSelected(),artifacts, resolvedDependency, dep, resolvedDependency.getRequested(),parent);
                    i().container.add(nodeAdapter);
                    if(nodeAdapter.isNodeSelected()) {
                        walk(selectedComponent, newArtifactMap, seen, dep + 1, nodeAdapter);
                    }
                    else{
                        //                        Artifact Unresolved, the node is not selected.
                        ComponentSelector selector = resolvedDependency.getRequested();
                        if(selector instanceof ModuleComponentSelector){
                            ModuleComponentSelector moduleComponentSelector = (ModuleComponentSelector) selector;
                            GradleUtil.i().resolveArtifact(selector.getDisplayName(),nodeAdapter);
                        } else if (selector instanceof ProjectComponentSelector) {
//                            TODO
                        }

                    }
                }
                else {
                    log.warn("Unresolved artifact ");
                }
            }
        }
        else{
            log.debug("cycle");
        }
    }


    public List<NodeAdapter> getAllNodeAdapter() {
        return container;
    }

    public void printAllNodeAdapter() {
        for (NodeAdapter nodeAdapter : container) {
            System.out.println(nodeAdapter);
        }
    }
    public NodeAdapter getConflictNodeAdapter(String groupId, String artifactId, String version) {
        for (NodeAdapter nodeAdapter : container) {
            if(nodeAdapter.getGroupId().equals(groupId) && nodeAdapter.getArtifactId().equals(artifactId) && nodeAdapter.getVersion().equals(version)){
                return nodeAdapter;
            }
        }
        return null;
    }
}