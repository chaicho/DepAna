package nju.lab.DScheckerMaven.model;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.ClassVO;
import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DSchecker.core.model.MethodVO;
import nju.lab.DSchecker.util.SootUtil;
import nju.lab.DScheckerMaven.util.GlobalVar;
import nju.lab.DScheckerMaven.util.MavenUtil;
import org.apache.commons.io.FileUtils;
import soot.jimple.toolkits.callgraph.CallGraph;

import java.io.*;
import java.util.*;

;

/**
 * @author asus
 *
 */
@Slf4j
public class DepJar implements IDepJar {
    private String groupId;
    private String artifactId;// artifactId
    private String version;// version
    private String classifier;
    private List<String> jarFilePaths;// host project may have multiple source.
    private Map<String, ClassVO> clsTb;// all class in jar
    // private Set<String> clsSigs;// only all class signatures defined in jars
    private Set<String> phantomClsSet;// all phantom classes in jar

    private Map<String, Set<String>> classesUsedScopes;
    /**
     * Nodes in Dependency tree that this jar is connected to.
     */
    private Set<NodeAdapter> nodeAdapters;
    private Set<NodeAdapter> nodeAdaptersWithSameGA;
    private Set<String> allMthd;
    private Set<String> allCls;
    private Map<String, Collection<String>> allRefedCls;
    private boolean canUseFilterBuffer = false;
    private Map<String, ClassVO> allClass;// all class in jar
    private int priority;

    /**
     * 初始化
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @param classifier
     * @param priority
     * @param jarFilePaths
     */
    public DepJar(String groupId, String artifactId, String version, String classifier, int priority,
            List<String> jarFilePaths) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.priority = priority;
        this.jarFilePaths = jarFilePaths;

        classesUsedScopes = new HashMap<>();
        classesUsedScopes.put("compile", new HashSet<>());
        classesUsedScopes.put("runtime", new HashSet<>());
        classesUsedScopes.put("test", new HashSet<>());
    }

    /**
     * 初始化
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @param classifier
     * @param jarFilePaths
     */
    public DepJar(String groupId, String artifactId, String version, String classifier, List<String> jarFilePaths) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.jarFilePaths = jarFilePaths;
    }

    public void setCanUseFilterBuffer(boolean canUseFilterBuffer) {
        this.canUseFilterBuffer = canUseFilterBuffer;
    }

    /**
     * get jar may have risk thinking same class in different dependency,selected
     * jar may have risk;
     * Not thinking same class in different dependency,selected jar is safe
     *
     * @return
     */
    public boolean isRisk() {
        return !this.isSelected();
    }

    /**
     * all class in jar中是不是包含某一class
     *
     */
    public boolean containsCls(String clsSig) {
        return this.getAllCls().contains(clsSig);
    }

    synchronized public Set<String> getAllCls() {
        if (allCls == null) {
            allCls = getAllClsRealTime(true);
        }
        return allCls;
    }

    private Set<String> getAllClsRealTime(boolean b) {
        if (allCls == null) {
            allCls = SootUtil.getJarsClasses(this.getJarFilePaths(b));
        }
        return allCls;
    }

    public Set<NodeAdapter> getNodeAdapters() {
        if (nodeAdapters == null) {
            nodeAdapters = NodeAdapters.i().getNodeAdapters(this);
        }
        return nodeAdapters;
    }

    public Set<NodeAdapter> getNodeAdaptersWithSameGA() {
        if (nodeAdaptersWithSameGA == null) {
            nodeAdaptersWithSameGA = NodeAdapters.i().getNodeAdaptersWithSameGA(getGroupId(),getArtifactId());
        }
        return nodeAdaptersWithSameGA;
    }
    /**
     * maybe useful
     * 
     * @return
     */
    public NodeAdapter getSelectedNode() {
        for (NodeAdapter node : getNodeAdapters()) {
            if (node.isNodeSelected()) {
                return node;
            }
        }
        return null;
    }

    /**
     * @return whether the scope is provided, but this node must be selected
     */
    public boolean isProvided() {
        for (NodeAdapter node : getNodeAdapters()) {
            if (node.isNodeSelected()) {
                return "provided".equals(node.getScope());
            }
        }
        return false;
    }

    /**
     * @return whether is selected
     *         只要
     */
    public boolean isSelected() {
        for (NodeAdapter nodeAdapter : getNodeAdapters()) {
            if (nodeAdapter.isNodeSelected()) {
                // if manageNodeAdapter, must not version changed. but sometimes not loaded
                if (nodeAdapter instanceof ManageNodeAdapter && nodeAdapter.getPriority() == -1) {
                    continue;
                }
                return true;
            }
            // 2022.8.10. modify bug. maybe can do more, reconstruct NodeAdapter code.
            if (nodeAdapter instanceof ManageNodeAdapter && nodeAdapter.getPriority() == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 得到这个jar所有类的集合 polished by grj
     * 
     * @return
     */

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DepJar) {
            return isSelf((DepJar) obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return groupId.hashCode() * 31 * 31 + artifactId.hashCode() * 31 + version.hashCode()
                + classifier.hashCode() * 31 * 31 * 31;
    }

    /**
     * @return groupId:artifactId:version:classifier
     */
    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version + ":" + classifier;
    }

    /**
     * @return groupId:artifactId:version
     */
    public String getSig() {
        return groupId + ":" + artifactId + ":" + version + ":" + classifier;
    }

    /**
     * @return groupId:artifactId
     */
    public String getName() {
        return groupId + ":" + artifactId;
    }

    /**
     * @return groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @return artifactId
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @return version
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return classifier
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * whether is the same deoJar
     * 
     * @param groupId2    : 目标groupId
     * @param artifactId2 : 目标artifactId
     * @param version2    : 目标version
     * @param classifier2 : 目标classifier
     * @return boolean
     */
    public boolean isSame(String groupId2, String artifactId2, String version2, String classifier2) {
        return groupId.equals(groupId2) && artifactId.equals(artifactId2) && version.equals(version2)
                && classifier.equals(classifier2);
    }

    /**
     * 是否为同一个
     * 
     * @param dep
     * @return
     */
    public boolean isSelf(DepJar dep) {
        return isSame(dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), dep.getClassifier());
    }

    /**
     * 没有比较版本
     * 
     * @param depJar
     * @return
     */
    public boolean isSameLib(DepJar depJar) {
        return getGroupId().equals(depJar.getGroupId()) && getArtifactId().equals(depJar.getArtifactId());
    }

    /**
     * 没有比较Classifier, 比较groupId, artifactId, version
     * 
     * @param depJar : 待比较的jar
     * @return boolean
     */
    public boolean isSameJarIgnoreClassifier(DepJar depJar) {
        return getGroupId().equals(depJar.getGroupId()) && getArtifactId().equals(depJar.getArtifactId())
                && getVersion().equals(depJar.getVersion());
    }

    public void setClsTb(Map<String, ClassVO> clsTb) {
        this.clsTb = clsTb;
    }

    public List<String> getJarFilePaths(boolean useTarget) {
        if (!useTarget) {// use source directory
            // if node is inner project,will return source directory(using source directory
            // can get classes before maven-package)
            if (isHost()) {
                return MavenUtil.getInstance().getSrcPaths();
            }
        }
        /*
         * 如果是test，那么默认路径为testclasses目录
         */
        if (GlobalVar.i().isTest && this.isHost() && !jarFilePaths.get(0).endsWith("test-classes")) {
            List<String> ret = new ArrayList<>();
            ret.add("");
            ret.set(0, jarFilePaths.get(0).substring(0, jarFilePaths.get(0).lastIndexOf("classes")) + "test-classes");
            log.info("ret depjargets" + ret);
            return ret;
        }
        return jarFilePaths;
    }

    @Override
    public List<String> getJarFilePaths() {
        return getJarFilePaths(true);
    }

    public String getJarFilePath() {
        if (GlobalVar.i().isTest && this.isHost() && !jarFilePaths.get(0).endsWith("test-classes")) {
            List<String> ret = new ArrayList<>(1);
            ret.add("");
            ret.set(0, jarFilePaths.get(0).substring(0, jarFilePaths.get(0).lastIndexOf("classes")) + "test-classes");
            log.info("ret depjarget" + ret);
            return ret.get(0);
        }
        return jarFilePaths.get(0);
    }

    public boolean isHost() {
        if (getNodeAdapters().size() == 1) {
            NodeAdapter node = getNodeAdapters().iterator().next();
            if (MavenUtil.getInstance().isInner(node)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The scope of a jar is the largest scope of all scopes of nodes in the dependency tree.
     * 
     * @return the scope after the scopes handling
     */
    public String getScope() {
        Set<String> scopes = new HashSet<String>();
        log.error(getArtifactId());
        log.error(getNodeAdapters().toString());
        for (NodeAdapter node : getNodeAdaptersWithSameGA()) {
            if (node.getNodeDepth() == 1) {
                if (getArtifactId().equals("snappy-java")) {
                    log.error("snappy-java direct scope: " + node.getScope());
                }
                return node.getScope();
            } else {
                scopes.add(node.getScope());
            }
        }
        if (getArtifactId().equals("snappy-java")) {
            log.error(nodeAdapters.toString());
            log.error("snappy-java scopes: " + scopes);
        }
        if (scopes.contains("compile")) {
            return "compile";
        } else if (scopes.contains("runtime")) {
            return "runtime";
        } else if (scopes.contains("provided")) {
            return "provided";
        } else if (scopes.contains("test")) {
            return "test";
        } else {
            return "N/A";
        }
    }

    @Override
    public Set<String> getUsedClassesAtScene(String scene) {
        if (classesUsedScopes.containsKey(scene)) {
            return classesUsedScopes.get(scene);
        } else {
            return new HashSet<>();
        }
    }

    @Override
    public Map<String, Set<String>> getUsedClasses() {
        return classesUsedScopes;
    }

    public String getUsedClassesAtSceneAsString(String scene) {
        if (classesUsedScopes.containsKey(scene)) {
            return classesUsedScopes.get(scene).toString();
        } else {
            return "";
        }
    }

    @Override
    public String getUsedClassesAsString() {
        StringBuilder sb = new StringBuilder();
        for (String scene : classesUsedScopes.keySet()) {
            sb.append("		scene " + scene + " : ");
            sb.append(classesUsedScopes.get(scene).toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public void addClassToScene(String scene, String cls) {
        if (classesUsedScopes.containsKey(scene)) {
            classesUsedScopes.get(scene).add(cls);
        } else {
            Set<String> clsSet = new HashSet<>();
            clsSet.add(cls);
            classesUsedScopes.put(scene, clsSet);
        }
    }

    /**
     * @return priority
     */
    public int getPriority() {
        return priority;
    }

    public String getDisplayName() {
        return getGroupId() + ":" + getArtifactId() + ":" + getVersion() + ":" + getScope();
    }

    /**
     *
     * @return the depth of the node in the dependency tree. Note that the root
     *         project is not in the tree.
     */
    @Override
    public int getDepth() {
        return getNodeAdapters().stream().map(NodeAdapter::getNodeDepth).min(Integer::compareTo).get();
    }

    /**
     * Return the trail in dependency tree that a jar file is selected;
     * 
     * @return path in dependency tree
     */
    @Override
    public String getUsedDepTrail() {
        StringBuilder sb = new StringBuilder();
        for (NodeAdapter node : getNodeAdapters()) {
            if (!node.isNodeSelected()) {
                continue;
            }
            sb.append("  [");
            sb.append(node.getWholePath());
            sb.append("]");
            break;
        }
        return sb.toString();
    }

    @Override
    public String getDepTrail() {
        StringBuilder sb = new StringBuilder();
        for (NodeAdapter node : getNodeAdapters()) {
            sb.append("  [");
            sb.append(node.getWholePath());
            sb.append("]");
            break;
        }
        return sb.toString();
    }

    @Override
    public Set<String> getDepTrails() {
        Set<String> trails = new HashSet<>();
        for (NodeAdapter node : getNodeAdapters()) {
            trails.add(node.getWholePath());
        }
        return trails;
    }

    @Override
    public Set<String> getScopes() {
        Set<String> scopes = new HashSet<String>();
        for (NodeAdapter node : getNodeAdapters()) {
            scopes.add(node.getScope());
        }
        return scopes;
    }

}
