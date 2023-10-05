package nju.lab.DScheckerMaven.model;

import lombok.extern.slf4j.Slf4j;

import org.apache.maven.shared.dependency.tree.DependencyNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author asus
 * 依赖树节点适配器
 */
@Slf4j
public class NodeAdapters {
    private static NodeAdapters instance;
    /**
     * 返回所有依赖树节点
     *
     * @return
     */
    public static NodeAdapters i() {
        return instance;
    }
    /**
     * 初始化
     * 依赖树扁平化
     * 并且将经过dependencymanagement的节点manage之前的那个版本虚构出来（一般被manage的都是依赖的依赖，也就是说不是项目直接依赖，所以被manage了就有些难受）。
     * （代码细节）在manageNds（一个临时变量）中找，如果找到了一样的构件，说明已经虚构过，不再虚构，否则虚构，并且加入manageNds中。
     * 将manageNds中这些虚构出来的节点放进NodeAdapters的容器中去。
     *
     * @param root
     */
    public static void init(DependencyNode root) {
        instance = new NodeAdapters();
        // add node in dependency tree
        NodeAdapterCollector visitor = new NodeAdapterCollector(instance);
        System.out.println("visit start");
        root.accept(visitor);
        System.out.println("visit end");
        // add management node
        List<NodeAdapter> manageNds = new ArrayList<NodeAdapter>();
        for (NodeAdapter nodeAdapter : instance.container) {
            // this node have management
            if (nodeAdapter.isVersionChanged()) {
                NodeAdapter maybeExistNode = instance.getNodeAdapter(nodeAdapter.getGroupId(), nodeAdapter.getArtifactId(),
                        nodeAdapter.getManagedVersion(), nodeAdapter.getClassifier(), nodeAdapter.getManagedScope());
                //2022.8.10. big bug! null doesn't mean good.maybe duplicated
                if (null == maybeExistNode || (!(maybeExistNode instanceof ManageNodeAdapter) && maybeExistNode.getPriority() == -1)) {
                    // this managed-version doesnt have used node,we should new a virtual node to
                    // find conflict
                    NodeAdapter manageNd = null;
                    // find if manageNd exists
                    for (NodeAdapter existManageNd : manageNds) {
                        if (existManageNd.isSelf(nodeAdapter.getGroupId(), nodeAdapter.getArtifactId(),
                                nodeAdapter.getManagedVersion(), nodeAdapter.getClassifier(), nodeAdapter.getManagedScope())) {
                            manageNd = existManageNd;
                            break;
                        }
                    }
                    //dont exist manageNd,should new and add
                    if (null == manageNd) {
                        manageNd = new ManageNodeAdapter(nodeAdapter);
                        manageNds.add(manageNd);
                    } else {
                        if (manageNd.getPriority() == -1) {
                            if (nodeAdapter.getPriority() >= 0) {
                                //TODO modify to new priority
                                manageNd.setPriority(0);
                            }
                        }
                    }
                } else {
                    if (maybeExistNode instanceof ManageNodeAdapter) {
                        if (maybeExistNode.getPriority() == -1) {
                            if (nodeAdapter.getPriority() >= 0) {
                                maybeExistNode.setPriority(0);
                            }
                        }
                    }
                }
            }
        }
        for (NodeAdapter manageNd : manageNds) {
            instance.addNodeAapter(manageNd);
        }
    }
    private List<NodeAdapter> container;
    private NodeAdapters() {
        container = new ArrayList<NodeAdapter>();
    }
    /**
     * 添加一个依赖树节点适配器
     *
     * @param nodeAdapter
     */
    public void addNodeAapter(NodeAdapter nodeAdapter) {
        container.add(nodeAdapter);
    }
    /**
     * 根据node获得对应的adapter
     * 这里如何判断node相等呢？只是简单根据引用相等来判断，并不参考artifactid之类的。
     *
     * @param node
     */
    public NodeAdapter getNodeAdapter(DependencyNode node) {
        for (NodeAdapter nodeAdapter : container) {
            if (nodeAdapter.isSelf(node)) {
                return nodeAdapter;
            }
        }
        if (node != null) {
            if (!NodeAdapterCollector.getLongTimeLib().contains(
                    node.getArtifact().getGroupId() + ":" + node.getArtifact().getArtifactId())) {
                log.warn("cant find nodeAdapter for node:" + node.toNodeString());
            }
        } else {
            log.warn("cant find nodeAdapter for node:" + "null");
        }
        return null;
    }
    /**
     * 返回所有node成员是指定node的适配器
     *
     * @param node
     * @return
     */
    public NodeAdapter getNodeAdapters(DependencyNode node) {
        for (NodeAdapter nodeAdapter : container) {
            if (nodeAdapter.isSelf(node)) {
                return nodeAdapter;
            }
        }
        if (node != null) {
            if (!NodeAdapterCollector.getLongTimeLib().contains(
                    node.getArtifact().getGroupId() + ":" + node.getArtifact().getArtifactId())) {
                log.warn("cant find nodeAdapter for node:" + node.toNodeString());
            }
        } else {
            log.warn("cant find nodeAdapter for node:" + "null");
        }
        return null;
    }
    /**
     * 根据groupId，artifactId，version和classifier获得对应的adapter
     *
     * @param groupId2    : 目标groupId
     * @param artifactId2 : 目标artifactId
     * @param version2    : 目标version
     * @param classifier2 : 目标classifier
     * @return nodeAdapter
     */
    public NodeAdapter getNodeAdapter(String groupId2, String artifactId2, String version2, String classifier2) {
        for (NodeAdapter nodeAdapter : container) {
            if (nodeAdapter.isSelf(groupId2, artifactId2, version2, classifier2)) {
                return nodeAdapter;
            }
        }
        // TODO delete the log
        log.warn("cant find nodeAdapter for management node:" + groupId2 + ":" + artifactId2 + ":"
                + version2 + ":" + classifier2);
        return null;
    }
    /**
     * Find NodeAdapter for management with the given groupId, artifactId, version, classifier and scope.
     */
    public NodeAdapter getNodeAdapter(String groupId, String artifactId, String version, String classifier, String scope) {
        for (NodeAdapter nodeAdapter : container) {
            if (nodeAdapter.isSelf(groupId, artifactId, version, classifier, scope)) {
                return nodeAdapter;
            }
        }
        return null;
    }
    /**
     * 根据depJar获得adapter
     *
     * @param depJar
     * @return
     */
    public Set<NodeAdapter> getNodeAdapters(DepJar depJar) {
        Set<NodeAdapter> result = new HashSet<NodeAdapter>();
        for (NodeAdapter nodeAdapter : container) {
            if (nodeAdapter.getDepJar() == depJar) {
                result.add(nodeAdapter);
            }
        }
        if (result.size() == 0) {
            log.warn("cant find nodeAdapter for depJar:" + depJar.toString());
        }
        return result;
    }

    public Set<NodeAdapter> getNodeAdaptersWithSameGA(String groupId, String artifactId) {
        Set<NodeAdapter> result = new HashSet<NodeAdapter>();
        for (NodeAdapter nodeAdapter : container) {
            if (nodeAdapter.getGroupId().equals(groupId) && nodeAdapter.getArtifactId().equals(artifactId)) {
                result.add(nodeAdapter);
            }
        }
        return result;
    }

    /**
     * 获取所有依赖树节点适配器
     *
     * @return
     */
    public List<NodeAdapter> getAllNodeAdapter() {
        return container;
    }
}
