package nju.lab.DSchecker.model;

import javassist.ClassPool;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import neu.lab.conflict.container.AllCls;
import neu.lab.conflict.container.AllRefedCls;
import neu.lab.conflict.container.DepJars;
import neu.lab.conflict.util.GradleUtil;
import neu.lab.conflict.util.MyLogger;
import neu.lab.conflict.util.SootUtil;
import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DSchecker.util.soot.JarAna;
import neu.lab.conflict.vo.GlobalVar;
import neu.lab.conflict.vo.MethodVO;


import java.io.File;
import java.util.*;

@Data
@Getter
@Setter
public class DepJar implements IDepJar {
    private final int depth;
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

    

    public DepJar(String groupId, String artifactId, String version, String classifier, List<String> jarFilePaths,int priority,int depth) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.jarFilePaths = jarFilePaths;
        this.priority = priority;
        this.depth = depth;
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
    public int getDepth() { return depth; }
    public Map<String, ClassVO> getClsTb() {
        if(clsTb==null){
            clsTb = initClsTbRealTime();
        }
        return clsTb;
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
//        TODO
        return jarFilePaths;
    }

    public boolean isSelected() {
//        TODO
        return nodeAdapters.iterator().next().isNodeSelected();
    }

    synchronized public Set<String> getAllCls() {
        if (allCls == null) {
            if (!GlobalVar.i().useAllClsBuffer) {
                allCls =  getAllClsRealTime(true);
            } else {
                allCls =  getAllClsWithBuffer(true);
            }
        }
        return allCls;
    }

    public Map<String, ClassVO> initClsTbRealTime() {
        if (clsTb == null) {
            if (null == this.getJarFilePaths(true)) {
                // no file
                clsTb = new HashMap<String, ClassVO>(0);
                GradleUtil.i().getLogger().warn("can't find jarFile for:" + toString());
            } else {
                if (GlobalVar.i().sootProcess) {
//                    TODO()
//                    clsTb = neu.lab.conflict.sootprocess.JarAna.i().deconstruct(this.getJarFilePaths(true));
                }
                else {
                    clsTb = JarAna.i().deconstruct(this.getJarFilePaths(true));
                }
                if (clsTb.size() == 0) {
                    MyLogger.i().warn("get empty clsTb for " + getDisplayName());
//                    GradleUtil.i().getLogger().warn("get empty clsTb for " + toString());
                }
                for (ClassVO clsVO : clsTb.values()) {
                    clsVO.setDepJar(this);
                }
            }
        }
        return clsTb;
    }

    private Set<String> getAllClsWithBuffer(boolean useTarget) {
//        TODO
        return null;
    }

    synchronized public Set<String> getAllClsRealTime(boolean useTarget) {
        if (allCls == null) {
            allCls = SootUtil.getInstance().getJarsClasses(this.getJarFilePaths(useTarget));
        }
        //System.out.println("allCls:" + allCls);
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
    public String getDisplayName(){
        return getGroupId() +":" + getArtifactId() + ":" + getVersion() + ":" + getClassifier();
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
        return groupId + ":" + artifactId + ":" + version + ":" + classifier;
    }
    /**
     * @return groupId:artifactId:version
     */
    public String getSig() {
        return groupId + ":" + artifactId + ":" + version;
    }
    /**
     * @return groupId:artifactId
     */
    public String getName() {
        return groupId + ":" + artifactId;
    }

    public ClassVO getClassVO(String clsSig) {
        return getClsTb().get(clsSig);
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
    public String getValidDepPath() {
        StringBuilder sb = new StringBuilder(toString() + ":");
        for (NodeAdapter node : getNodeAdapters()) {
            if (node.isNodeSelected()) {
                sb.append("  [");
                sb.append(node.getWholePath());
                sb.append("]");
            }
        }
        return sb.toString();
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
    public String getScope(){
       return "implementation";
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
//        if (getNodeAdapters().size() == 1) {
//            NodeAdapter node = getNodeAdapters().iterator().next();
//            if (GradleUtil.i().isHost(node)) {
//                return true;
//            }
//        }
        return false;
    }

    public String getJarFilePath() {
        if (GlobalVar.i().isTest && this.isHost() && !jarFilePaths.get(0).endsWith("test-classes")) {
            List<String> ret= new ArrayList<>(1);
            ret.add("");
            ret.set(0, jarFilePaths.get(0).substring(0, jarFilePaths.get(0).lastIndexOf("classes")) + "test-classes");
            MyLogger.i().info("ret depjarget" + ret);
            return ret.get(0);
        }
        return jarFilePaths.get(0);
    }

    public Map<String, Collection<String>> getRefedCls() {
        if (allRefedCls == null) {
            if (!GlobalVar.i().useRefedClsBuffer) {
                return getRefedClsRealTime();
            }
            else {
//                TODO
                return getRefedClsWithBuffer();
            }
        }
        return allRefedCls;
    }

    private Map<String, Collection<String>> getRefedClsWithBuffer() {
//        TODO
        return null;
    }

    private Map<String, Collection<String>> getRefedClsRealTime(){
        Map<String, Collection<String>> ret = new HashMap<>();
        try {
            ClassPool pool = new ClassPool();
            pool.appendClassPath(this.getJarFilePath());
            for (String cls : this.getAllCls()) {
                if (pool.getOrNull(cls) != null) {
                    if (!ret.containsKey(cls)) {
                        ret.put(cls, new LinkedList<>());
                    }
                    for (String refedCls : pool.get(cls).getRefClasses()) {
                        if (!SootUtil.getInstance().isJavaLibraryClass(refedCls)) {
                            ret.get(cls).add(refedCls);
                        }
                    }
                } else {
                    MyLogger.i().warn("can't find " + cls + " in pool when form reference.");
                }
            }
        }
        catch (Exception e) { System.err.println("Caught Exception!");
            MyLogger.i().error("get refed classes error for jar:" + this.getSig() + e);
        }
        return ret;
    }

    public Map<String, Collection<String>> getClassesRefedClsWhenTest() {
        Map<String, Collection<String>> ret = new HashMap<>();
        try {
            ClassPool pool = new ClassPool();
            pool.appendClassPath(this.getJarFilePath().replace("test-classes", "classes"));
            List<String> tmpclasspaths = new ArrayList<>();
            tmpclasspaths.add(this.getJarFilePath().replace("test-classes", "classes"));
            for (String cls : SootUtil.getInstance().getJarsClasses(tmpclasspaths)) {
                if (pool.getOrNull(cls) != null) {
//					System.out.println();
                    //ret.addAll(pool.get(cls).getRefClasses());
                    if (!ret.containsKey(cls)) {
                        ret.put(cls, new LinkedList<>());
                    }
                    for (String refedCls : pool.get(cls).getRefClasses()) {
                        if (!SootUtil.getInstance().isJavaLibraryClass(refedCls)) {
                            ret.get(cls).add(refedCls);
                        }
                    }
                    //pool.get(cls).getM
                } else {
                    MyLogger.i().warn("can't find " + cls + " in pool when form reference.");
                }
            }
        }
        catch (Exception e) { System.err.println("Caught Exception!");
            MyLogger.i().error("get refed classes error for jar:" + this.getSig() + e);
            //e.printStackTrace();
        }
        return ret;
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
    public Set<String> getAllMthd() {
        if (allMthd == null) {
            allMthd = new HashSet<String>();
            /**
             * add this synchronize for multiprocess get callgraph
             */
            synchronized (this){
                for (ClassVO cls : getClsTb().values()) {
                    for (MethodVO mthd : cls.getMthds()) {
                        allMthd.add(mthd.getMthdSig());
                    }
                }
            }
        }
        return allMthd;
    }

    public Set<String> getRiskMthds(Collection<String> testMthds) {
        Set<String> riskMthds = new HashSet<String>();
        System.out.println("DepJar.getRiskMthds begin");
        for (String testMthd : testMthds) {
            if (!this.containsMthd(testMthd) && (
                    (GlobalVar.i().refReachable && AllRefedCls.iReachable().contains(SootUtil.getInstance().mthdSig2cls(testMthd))) ||
                            (!GlobalVar.i().refReachable && AllRefedCls.i().contains(SootUtil.getInstance().mthdSig2cls(testMthd)))
            )
            ) {
                // don't have method,and class is used. 使用这个类，但是没有方法
                if (this.containsCls(SootUtil.getInstance().mthdSig2cls(testMthd))) {
                    // has class.don't have method.	有这个类，没有方法
                    riskMthds.add(testMthd);
                } else if (!AllCls.i().contains(SootUtil.getInstance().mthdSig2cls(testMthd))) {
                    // This jar don't have class,and all jar don't have class.	这个jar没有这个class，所有加载的jar都没有
                    riskMthds.add(testMthd);
                }
            }
        }
        System.out.println("DepJar.getRiskMthds end");
        return riskMthds;
    }
    public boolean containsMthd(String mthd) {
        return getAllMthd().contains(mthd);
    }
    public Set<String> getRiskRefedClasses(Collection<String> testClasses) {
        Set<String> riskClasses = new HashSet<String>();
        System.out.println("DepJar.getRiskClasses begin, class detect ref reachable:" + GlobalVar.i().classDetectRefReachable);
        for (String testClass : testClasses) {
            /**
             * never open refReachable(has bug)
             */
            if (!this.containsCls(testClass) &&
                    ((!GlobalVar.i().classDetectRefReachable && AllRefedCls.iNotReachable().contains(testClass)) ||
                            (GlobalVar.i().classDetectRefReachable && AllRefedCls.iReachable().contains(testClass))
                    )) {
                if (!AllCls.i().contains(testClass)) {
                    riskClasses.add(testClass);
                }
            }
        }
        System.out.println("DepJar.getRiskClasses end");
        return riskClasses;
    }

    public List<DepJar> getReplaceJarList() throws Exception {
        List<DepJar> depJars = new ArrayList<>();
        depJars.add(this);
        boolean hasRepalce = false;
        for (DepJar usedDepJar : DepJars.i().getUsedDepJars()) {
            if (this.isSameLib(usedDepJar)) {// used depJar instead of usedDepJar.
                if (hasRepalce) {
                    MyLogger.i().warn("when cg, find multiple usedLib for " + toString());	//有重复的使用路径
                    throw new Exception("when cg, find multiple usedLib for " + toString());
                }
                hasRepalce = true;
            } else {
                depJars.add(usedDepJar);
            }
        }
        if (!hasRepalce) {
            MyLogger.i().warn("when cg,can't find mutiple usedLib for " + toString());
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
                    MyLogger.i().warn("when cg, find multiple usedLib for " + toString());	//有重复的使用路径
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
            MyLogger.i().warn("when cg,can't find mutiple usedLib for " + toString());
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
    /**
     * get only father jar class paths, used in pruning
     * 只获取父节点，剪枝时使用
     * @param includeSelf : include self
     * @return Set<String> fatherJarCps
     */
    public Set<String> getOnlyFatherJarCps4Scene6(boolean includeSelf) {
        Set<String> fatherJarCps = new HashSet<String>();
        for (NodeAdapter node : this.nodeAdapters) {
//            fatherJarCps.addAll(node.getImmediateAncestorJarCps4Scene6(includeSelf));
        }
        return fatherJarCps;
    }
    public Set<DepJar> getOnlyFatherJars4Scene6(boolean includeSelf) {
        Set<DepJar> fatherJars = new HashSet<>();
        for (NodeAdapter node : this.nodeAdapters) {
//            fatherJars.addAll(node.getImmediateAncestorJars4Scene6(includeSelf));
        }
        return fatherJars;
    }

    public Set<String> getUsedMthds() {
        Set<String> allMethods = getAllMthd();
        Set<String> usedMethods = new HashSet<>();
        for (String method : allMethods) {
            if (AllRefedCls.i().contains(SootUtil.getInstance().mthdSig2cls(method))) {
                usedMethods.add(method);
            }
        }
        return usedMethods;
    }
}

