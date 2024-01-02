package nju.lab.DScheckerGradle.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.ClassVO;
import nju.lab.DSchecker.util.SootUtil;
import nju.lab.DSchecker.core.model.IDepJar;


import java.io.File;
import java.util.*;

@Slf4j
@Data
@Getter
@Setter
public class DepJar implements IDepJar {
    private int depth;
    private String groupId;
    private String artifactId;// artifactId
    private String version;// version
    private String classifier;
    private List<String> jarFilePaths;
    private File file;
    private Set<String> allCls;
    private Map<String, ClassVO> clsTb = null;// all class in jar
    private Set<NodeAdapter> nodeAdapters;// all
    private Map<String, Collection<String>> allRefedCls;
    private int priority;
    private HashSet<String> allMthd;
    private Map<String, Set<String>> classesUsedScopes;
    public String scope;

    public DepJar(String groupId, String artifactId, String version, String classifier, List<String> jarFilePaths,int priority,int depth) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.jarFilePaths = jarFilePaths;
        this.priority = priority;
        this.depth = depth;

        classesUsedScopes = new HashMap<>();
        classesUsedScopes.put("compile", new HashSet<>());
        classesUsedScopes.put("runtime", new HashSet<>());
        classesUsedScopes.put("test", new HashSet<>());
        classesUsedScopes.put("abi", new HashSet<>());
    }

    public String getGroupId() {
        return groupId;
    }
    public String getArtifactId() {
        return artifactId;
    }
    public String getVersion() {
        return version;
    }
    public String getClassifier() {
        return classifier;
    }
    public String getGA() {
        return groupId + ":" + artifactId;
    }
    public int getDepth() {
        return depth;
    }

    /**
     * @param useTarget:
     *            host-class-name can get from source directory(false) or target
     *            directory(true). using source directory: advantage: get class
     *            before maven-package disadvantage:class can't deconstruct by
     *            soot;miss class that generated.
     * @return
     */
    public List<String> getJarFilePaths(boolean useTarget ) {
//        TODO
        return jarFilePaths;
    }

    public List<String> getJarFilePaths( ) {
        return jarFilePaths;
    }

    public boolean isSelected() {
        for (NodeAdapter nodeAdapter : nodeAdapters) {
            if (nodeAdapter.isNodeSelected()) {
                return true;
            }
        }
        return nodeAdapters.iterator().next().isNodeSelected();
    }

    synchronized public Set<String> getAllCls() {
        if (allCls == null) {
            allCls =  getAllClsRealTime();
        }
        return allCls;
    }

    synchronized public Set<String> getAllClsRealTime() {
        if (allCls == null) {
            allCls = SootUtil.getJarsClasses(this.getJarFilePaths());
        }
        return allCls;
    }

    public boolean isSameLib(DepJar depJar) {
        return getGroupId().equals(depJar.getGroupId()) && getArtifactId().equals(depJar.getArtifactId());
    }
    public boolean isSame(String groupId2, String artifactId2, String version2, String classifier2) {
        return groupId.equals(groupId2) && artifactId.equals(artifactId2) && version.equals(version2)
                && classifier.equals(classifier2);
    }

    public boolean isSelf(DepJar dep) {
        return isSame(dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), dep.getClassifier());
    }

    /**
     * @return groupId:artifactId:version
     */
    public String getDisplayName(){
        return getGroupId() +":" + getArtifactId() + ":" + getVersion();
    }

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
    public boolean equals(Object obj) {
        if (obj instanceof DepJar) {
            return isSelf((DepJar) obj);
        }
        return false;
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    @Override
    public String toString() {
        return getGroupId() + ":" + getArtifactId() + ":" + getVersion() + ":" + getClassifier();
    }
    /**
     * @return groupId:artifactId:version
     */
    public String getSig() {
        return getGroupId() + ":" + getArtifactId() + ":" + getVersion() + ":" + getClassifier();
    }
    /**
     * @return groupId:artifactId
     */
    public String getName() {
        return groupId + ":" + artifactId;
    }


    public String getAllDepPaths(){
        StringBuilder sb = new StringBuilder(toString() + ":");
        for (NodeAdapter node : getNodeAdapters()) {
            sb.append(node.getWholePath());
            sb.append(";");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    /**
     * @return the import path of depJar.
     */
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
        return null;
    }


    /**
     * maybe useful
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
    public String getScope() {
       return scope;
    }
    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public Set<String> getScopes() {
        return null;
    }

    @Override
    public Map<String, Set<String>> getUsedClasses() {
        return null;
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
    public void addClassToScene(String scene, String cls) {
        if (classesUsedScopes.containsKey(scene)) {
            classesUsedScopes.get(scene).add(cls);
        } else {
            Set<String> clsSet = new HashSet<>();
            clsSet.add(cls);
            classesUsedScopes.put(scene, clsSet);
        }
    }

    public String getUsedClassesAsString() {
        StringBuilder sb = new StringBuilder();
        for (String scene : classesUsedScopes.keySet()) {
            sb.append("		scene " + scene + " : ");
            sb.append(classesUsedScopes.get(scene).toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public Set<NodeAdapter> getNodeAdapters() {
        return nodeAdapters;
    }

    public void addNodeAdapter(NodeAdapter nodeAdapter) {
        if (this.nodeAdapters == null) {
            this.nodeAdapters = new HashSet<NodeAdapter>();
        }
        this.nodeAdapters.add(nodeAdapter);
    }

    public boolean isHost() {
        System.out.println("getArtifactId(): " + getArtifactId());
        System.out.println("HostProjectInfo.i().getName(): " + HostProjectInfo.i().getName());
        return getArtifactId().equals(HostProjectInfo.i().getName());
    }


    /**
     * 没有比较Classifier, 比较groupId, artifactId, version
     * @param depJar : 待比较的jar
     * @return boolean
     */
    public boolean isSameJarIgnoreClassifier(DepJar depJar) {
        return getGroupId().equals(depJar.getGroupId()) && getArtifactId().equals(depJar.getArtifactId())
                && getVersion().equals(depJar.getVersion());
    }

    public int getPriority() {
        return priority;
    }

    public boolean containsCls(String clsSig) {
        return this.getAllCls().contains(clsSig);
    }

    public Set<String> getRiskClasses(Collection<String> entryClasses) {
        Set<String> riskClasses = new HashSet<String>();
        for (String cls : entryClasses) {
            if (!this.containsCls(cls)) {
                riskClasses.add(cls);
            }
        }
        return riskClasses;
    }


    public List<DepJar> getReplaceJarList() throws Exception {
        List<DepJar> depJars = new ArrayList<>();
        depJars.add(this);
        boolean hasRepalce = false;
        for (DepJar usedDepJar : DepJars.i().getUsedDepJars()) {
            if (this.isSameLib(usedDepJar)) {// used depJar instead of usedDepJar.
                if (hasRepalce) {
                    log.warn("when cg, find multiple usedLib for " + toString());	//有重复的使用路径
                    throw new Exception("when cg, find multiple usedLib for " + toString());
                }
                hasRepalce = true;
            } else {
                depJars.add(usedDepJar);
            }
        }
        if (!hasRepalce) {
            log.warn("when cg,can't find mutiple usedLib for " + toString());
            throw new Exception("when cg,can't find mutiple usedLib for " + toString());
        }
        return depJars;
    }
    /**
     * use this jar replace version of used-version ,then return path of
     * all-used-jar
     * 使用这个jar替代了旧版本，然后返回所有的旧jar的路径
     * 说人话：就是把所有被使用jar（除去和本jar相同artifactId的），以及本jar返回
     * @return
     * @throws Exception
     */
    public List<String> getReplaceCp() throws Exception {
        List<String> paths = new ArrayList<String>();
        paths.addAll(this.getJarFilePaths(true));
        boolean hasRepalce = false;
        for (DepJar usedDepJar : DepJars.i().getUsedDepJars()) {
            if (this.isSameLib(usedDepJar)) {// used depJar instead of usedDepJar.
                if (hasRepalce) {
                    log.warn("when cg, find multiple usedLib for " + toString());	//有重复的使用路径
                    throw new Exception("when cg, find multiple usedLib for " + toString());
                }
                hasRepalce = true;
            } else {
                for (String path : usedDepJar.getJarFilePaths(true)) {
                    paths.add(path);
                }
            }
        }
        if (!hasRepalce) {
            log.warn("when cg,can't find mutiple usedLib for " + toString());
            throw new Exception("when cg,can't find mutiple usedLib for " + toString());
        }
        return paths;
    }

    /**
     * get only father jar class paths, used in pruning
     * 只获取父节点，剪枝时使用
     * @param includeSelf : include self
     * @return Set<String> fatherJarCps
     */
    public Set<String> getOnlyFatherJarCps(boolean includeSelf) {
        Set<String> fatherJarCps = new HashSet<String>();
        for (NodeAdapter node : this.nodeAdapters) {
//            fatherJarCps.addAll(node.getImmediateAncestorJarCps(includeSelf));
        }
        return fatherJarCps;
    }
    /**
     * get only father jar class paths, used in pruning
     * 只获取父节点，剪枝时使用
     * @param includeSelf : include self
     * @return Set<String> fatherJarCps
     */
    public Set<DepJar> getOnlyFatherJars(boolean includeSelf) {
        Set<DepJar> fatherJars = new HashSet<>();
        for (NodeAdapter node : this.nodeAdapters) {
//            fatherJars.addAll(node.getImmediateAncestorJars(includeSelf));
        }
        return fatherJars;
    }

    public boolean isSame(String groupId, String artifactId, String version) {
        return this.groupId.equals(groupId) && this.artifactId.equals(artifactId) && this.version.equals(version);
    }
}

