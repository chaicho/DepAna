package nju.lab.DScheckerMaven.model;

import nju.lab.DScheckerMaven.util.ClassifierUtil;
import nju.lab.DScheckerMaven.util.Conf;
import nju.lab.DScheckerMaven.util.MavenUtil;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * 访问者模式，遍历依赖树生成NodeAdapters的类
 */
public class NodeAdapterCollector implements DependencyNodeVisitor {
    private static Set<String> longTimeLib;// lib that takes a long time to get call-graph.
    static {
        longTimeLib = new HashSet<String>();
        longTimeLib.add("org.scala-lang:scala-library");
        longTimeLib.add("org.clojure:clojure");
    }
    /**
     * 一些分析时间过长的jar包
     * @return
     */
    public static Set<String> getLongTimeLib() {
        return longTimeLib;
    }
    private NodeAdapters nodeAdapters;
    private static int priority = 0;
    /**
     * 构造函数
     * @param nodeAdapters
     */
    public NodeAdapterCollector(NodeAdapters nodeAdapters) {
        this.nodeAdapters = nodeAdapters;
    }
    /**
     * 访问者模式visit函数，用于前序遍历Maven依赖树，做一些处理，生成NodeAdapters
     * return true表示继续遍历子节点
     *   return false表示当前节点子节点不再遍历
     * @param node
     * @return
     */
    @Override
    public boolean visit(DependencyNode node) {
        MavenUtil.getInstance().getLog().debug(node.toNodeString() + " type:" + node.getArtifact().getType() + " version"
                + node.getArtifact().getVersionRange() + " selected:" + (node.getState() == DependencyNode.INCLUDED));
        if (Conf.getInstance().DEL_LONGTIME) {
            if (longTimeLib.contains(node.getArtifact().getGroupId() + ":" + node.getArtifact().getArtifactId())) {
                return false;
            }
        }
        if (Conf.getInstance().DEL_OPTIONAL) {
            if (node.getArtifact().isOptional()) {
                return false;
            }
        }
        // 是否过滤掉scope为provided的
        if (MavenUtil.getInstance().getMojo().ignoreProvidedScope) {
            if ("provided".equals(node.getArtifact().getScope())) {
                return false;
            }
        }
        // 是否过滤掉scope为test的
        if (MavenUtil.getInstance().getMojo().ignoreTestScope) {
            if ("test".equals(node.getArtifact().getScope())) {
                return false;
            }
        }
        // 是否过滤掉classifier为test的
        if (MavenUtil.getInstance().getMojo().ignoreTestClassifier) {
            if (ClassifierUtil.transformClf(node.getArtifact().getClassifier()).contains("test")){
                return false;
            }
        }
        // 是否过滤掉scope为runtime的
        if (MavenUtil.getInstance().getMojo().ignoreRuntimeScope) {
            if ("runtime".equals(node.getArtifact().getScope())) {
                return false;
            }
        }
        if (node.getState() == 0) {
//            0 Represents Included.
            /*这里的优先级是加载类的优先级，将会在检测完全限定名相同时用到*/
            nodeAdapters.addNodeAapter(new NodeAdapter(node, priority));
            priority++;
        }
        else {
            nodeAdapters.addNodeAapter(new NodeAdapter(node, -1));
        }
        return true;
    }
    /**
     * unknown
     * @param node
     * @return
     */
    @Override
    public boolean endVisit(DependencyNode node) {
        return true;
    }
}
