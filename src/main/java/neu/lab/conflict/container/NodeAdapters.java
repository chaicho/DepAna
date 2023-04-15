package neu.lab.conflict.container;

import neu.lab.conflict.util.GradleUtil;
import neu.lab.conflict.util.MyLogger;
import neu.lab.conflict.vo.NodeAdapter;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.component.*;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;

import java.io.File;
import java.util.*;

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
            walk(root,newArtifactMap,seen,0);
        }

    }
    public void addNodeAdapter(NodeAdapter nodeAdapter) {
        container.add(nodeAdapter);
    }
    private static void walk(ResolvedComponentResult component, Map<ComponentIdentifier, Set<ResolvedArtifact>> newArtifactMap, Set<ResolvedComponentResult> seen, int dep) {

        if (seen.add(component)) {
            for (DependencyResult dependency : component.getDependencies()) {
                if (dependency instanceof ResolvedDependencyResult) {
                    ResolvedDependencyResult resolvedDependency = (ResolvedDependencyResult) dependency;
                    ResolvedComponentResult selectdCompoment = resolvedDependency.getSelected();
                    Set<ResolvedArtifact> artifacts = newArtifactMap.get(selectdCompoment.getId());

                    NodeAdapter nodeAdapter =  new NodeAdapter(resolvedDependency.getSelected(),artifacts, resolvedDependency, dep, resolvedDependency.getRequested());
                    i().container.add(nodeAdapter);
                    if(nodeAdapter.isNodeSelected()) {
                        walk(selectdCompoment, newArtifactMap, seen, dep + 1);
                    }
                    else{
//                        Artifact Unresolved, the node is not selected.
                        ComponentSelector selector = resolvedDependency.getRequested();
                        if(selector instanceof ModuleComponentSelector){
                            ModuleComponentSelector moduleComponentSelector = (ModuleComponentSelector) selector;
                            GradleUtil.i().resolveArtifact(selector.getDisplayName(),nodeAdapter);
                        } else if (selector instanceof ProjectComponentSelector) {

                        }
                        
//                        System.out.println("Not Selected : " + nodeAdapter );
                    }
                }
                else {
                    MyLogger.i().warn("Unresolved artifact ");
                }
            }
        }
        else{
            System.out.println("cycle");
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

}
