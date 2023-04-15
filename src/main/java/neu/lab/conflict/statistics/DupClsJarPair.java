//package neu.lab.conflict.statistics;
//
//import neu.lab.conflict.container.AllRefedCls;
//import neu.lab.conflict.container.Conflicts;
//import neu.lab.conflict.graph.Graph4path;
//import neu.lab.conflict.graph.IGraph;
//import neu.lab.conflict.graph.Node4path;
//import neu.lab.conflict.util.soot.SootJRiskCg;
//import neu.lab.conflict.util.soot.tf.JRiskMthdPathCgTf;
//import neu.lab.conflict.util.ClassDupRiskMemoryUnit;
//import neu.lab.conflict.util.Conf;
//import neu.lab.conflict.util.MavenUtil;
//import neu.lab.conflict.util.SootUtil;
//import neu.lab.conflict.vo.*;
//
//import java.util.*;
//
///**
// * two jar that have different name and same class.
// *
// * @author asus
// */
//public class DupClsJarPair {
//    private DepJar jar1;
//    private DepJar jar2;
//    private Set<String> clsSigs;
//    private Set<String> riskMethods;
//    private IGraph iGraph;// for soot process, reconstruct Code
//
//    private String needDetect = null; // for jar changed
//
//    public String getNeedDetect() {
//        return needDetect;
//    }
//
//    public void setNeedDetect(String needDetect) {
//        this.needDetect = needDetect;
//    }
//// TODO add a variable that mark if the pair need to be detected while jar changed
//
//    public DupClsJarPair(DepJar jarA, DepJar jarB) {
//        jar1 = jarA;
//        jar2 = jarB;
//        clsSigs = new HashSet<String>();
//    }
//
//    public String getSig() {
//        return jar1.toString() + "-" + jar2.toString();
//    }
//
//    public Set<String> getClsSigs() {
//        return clsSigs;
//    }
//
//    public boolean isInDupCls(String rhcedMthd) {
//        return clsSigs.contains(SootUtil.getInstance().mthdSig2cls(rhcedMthd));
//    }
//
//    public void addClass(String clsSig) {
//        clsSigs.add(clsSig);
//    }
//
//    public Set<String> getRiskMethods() {
//        if (riskMethods == null) {
//            /**
//             * ref Reachable
//             * TODO allCls -> methodSig's class
//             * now judge allCls reachable, not perfect?
//             */
//            if (GlobalVar.i().refReachable) {
//                boolean refed = false;
//                for (String sameCls : this.getClsSigs()) {
//                    if (AllRefedCls.iReachable().contains(sameCls)) {
//                        refed = true;
//                        break;
//                    }
//                }
//                if (!refed) {
//                    this.riskMethods = new HashSet<>();
//                    return riskMethods;
//                }
//            }
//            if (jar1.getPriority() < jar2.getPriority() && jar1.getPriority() != -1) {
//                riskMethods = this.getOnlyMethod(jar2, jar1);
//            } else {
//                riskMethods = this.getOnlyMethod(jar1, jar2);
//            }
//            if (riskMethods == null) {
//                riskMethods = new HashSet<>();
//            }
//        }
//        return riskMethods;
//    }
//
//    /**
//     * judge jarA whether is the same jar with jarB
//     * @param jarA DepJar
//     * @param jarB DepJar
//     * @return boolean
//     */
//    public boolean isSelf(DepJar jarA, DepJar jarB) {
//        return (jar1.equals(jarA) && jar2.equals(jarB)) || (jar1.equals(jarB) && jar2.equals(jarA));
//    }
//
//    public DepJar getJar1() {
//        return jar1;
//    }
//
//    public DepJar getJar2() {
//        return jar2;
//    }
//
//    public DepJar getPriorJar() {
//        if(jar1.getPriority() < jar2.getPriority() && jar1.getPriority() != -1){
//            return jar1;
//        }
//        else if(jar2.getPriority() < jar1.getPriority() && jar2.getPriority() != -1){
//            return jar2;
//        }
//        else {
//            return null;
//        }
//    }
//    public DepJar getNonPriorJar() {
//        if(jar1.getPriority() < jar2.getPriority() && jar1.getPriority() != -1){
//            return jar2;
//        }
//        else if(jar2.getPriority() < jar1.getPriority() && jar2.getPriority() != -1){
//            return jar1;
//        }
//        else {
//            return null;
//        }
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("    jar1 : ").append(getJar1().getSig()).append(" " + getJar1().getAllDepPath()).append("\n");
//        sb.append("    jar2 : ").append(getJar2().getSig()).append(" " + getJar2().getAllDepPath()).append("\n");
//        sb.append("classes : \n");
//        for (String clsSig : getClsSigs()) {
//            sb.append("    ").append(clsSig).append("\n");
//        }
//        return sb.toString();
//    }
//
//    /**
//     * get jar1 and jar2 risk method
//     * @return string
//     */
//    public String getRiskString() {
//        StringBuilder sb = new StringBuilder("classConflict:");
//        sb.append("<" + jar1.toString() + ">");
//        sb.append("<" + jar2.toString() + ">\n");
//        sb.append(getJarString(jar1, jar2));
//        sb.append(getJarString(jar2, jar1));
//        return sb.toString();
//    }
//
//    /**
//     * get total depJar only methods
//     * @param total
//     * @param some
//     * @return string
//     */
//    private String getJarString(DepJar total, DepJar some) {
//        StringBuilder sb = new StringBuilder();
//        Set<String> onlyMthds = getOnlyMethod(total, some);
//        sb.append("   methods that only exist in " + total.getValidDepPath() + "\n");
//        if (onlyMthds.size() > 0) {
//            for (String onlyMthd : onlyMthds) {
//                sb.append(onlyMthd + "\n");
//            }
//        }
//        return sb.toString();
//    }
//
//    /**
//     * get total depJar only methods
//     * @param total
//     * @param some
//     * @return Set<String> total depJar onlyMthds
//     */
//    public Set<String> getOnlyMethod(DepJar total, DepJar some) {
//        Set<String> onlyMthds = new HashSet<>();
//        for (String clsSig : clsSigs) {
//            ClassVO classVO = total.getClassVO(clsSig);
//            if (classVO != null) {
//                for (MethodVO mthd : classVO.getMthds()) {
//                    ClassVO someClassVO = some.getClassVO(clsSig);
//                    if (someClassVO != null) {
//                        if (!someClassVO.hasMethod(mthd.getMthdSig())) {
//                            onlyMthds.add(mthd.getMthdSig());
//                        }
//                    }
//
//                }
//            }
//        }
//        return onlyMthds;
//    }
//
//    /**
//     * if has riskMethods, get graph
//     * @param classDupRiskMemoryUnit for record
//     */
//    public Graph4path getGraph4MethodPath(ClassDupRiskMemoryUnit classDupRiskMemoryUnit) {
//        Set<String> riskMethods; // low priority jar risk methods
//        Set<String> noUsedJarRiskMethods; // high priority jar risk methods
//        if(jar1.getPriority() < jar2.getPriority() && jar1.getPriority() != -1){
//            riskMethods = getOnlyMethod(jar2, jar1);
//            noUsedJarRiskMethods = getOnlyMethod(jar1, jar2);
//            classDupRiskMemoryUnit.setLowPriorityJar(jar1);
//            classDupRiskMemoryUnit.setHighPriorityJar(jar2);
//        }else {
//            riskMethods = getOnlyMethod(jar1, jar2);
//            noUsedJarRiskMethods = getOnlyMethod(jar2, jar1);
//            classDupRiskMemoryUnit.setLowPriorityJar(jar2);
//            classDupRiskMemoryUnit.setHighPriorityJar(jar1);
//        }
//        classDupRiskMemoryUnit.setRiskMethods(riskMethods);
//        classDupRiskMemoryUnit.setNoUsedJarRiskMethods(noUsedJarRiskMethods);
//        if (riskMethods.size() > 0) {
//            IGraph iGraph = SootJRiskCg.i().getGraph(this, new JRiskMthdPathCgTf(this, riskMethods));
//            if (iGraph != null) {
//                return (Graph4path) iGraph;
//            }
//        }
//        return new Graph4path(new HashMap<String, Node4path>(), new ArrayList<MethodCall>());
//    }
//    /**
//     * if has riskMethods, get graph
//     * @param classDupRiskMemoryUnit for record
//     */
//    public Graph4path getGraph4MethodPathChooseCg(ClassDupRiskMemoryUnit classDupRiskMemoryUnit) {
//        Set<String> riskMethods; // low priority jar risk methods
//        Set<String> noUsedJarRiskMethods; // high priority jar risk methods
//        // TODO modify getGraph4MethodPathChooseCg to "getRiskMethods"
//        if(jar1.getPriority() < jar2.getPriority() && jar1.getPriority() != -1){
//            riskMethods = getOnlyMethod(jar2, jar1);
//            noUsedJarRiskMethods = getOnlyMethod(jar1, jar2);
//            classDupRiskMemoryUnit.setLowPriorityJar(jar1);
//            classDupRiskMemoryUnit.setHighPriorityJar(jar2);
//        }else {
//            riskMethods = getOnlyMethod(jar1, jar2);
//            noUsedJarRiskMethods = getOnlyMethod(jar2, jar1);
//            classDupRiskMemoryUnit.setLowPriorityJar(jar2);
//            classDupRiskMemoryUnit.setHighPriorityJar(jar1);
//        }
//        classDupRiskMemoryUnit.setRiskMethods(riskMethods);
//        classDupRiskMemoryUnit.setNoUsedJarRiskMethods(noUsedJarRiskMethods);
//        if (riskMethods.size() > 0) {
//            if (iGraph != null) {
//                return (Graph4path) iGraph;
//            }
//            IGraph iGraph = null;
//            if (GlobalVar.i().sootProcess) {
//                iGraph = neu.lab.conflict.sootprocess.SootJRiskCg.i().getGraphChooseCg(this, riskMethods, GlobalVar.i().cgAlgStr);
//            }
//            else {
//                if (GlobalVar.i().cgFast) {
//                    // TODO same name class also need cg fast
//                    // iGraph = SootJRiskCg.i().getGraphChooseCgFast(this, riskMethods);
//                    iGraph = SootJRiskCg.i().getGraphChooseCgFast(this, riskMethods);
//                }
//                else {
//                    iGraph = SootJRiskCg.i().getGraphChooseCg(this, riskMethods, GlobalVar.i().cgAlgStr);
//                }
//
//            }
//            if (iGraph != null) {
//                this.iGraph = iGraph;
//                return (Graph4path) iGraph;
//            }
//
//        }
//        return new Graph4path(new HashMap<String, Node4path>(), new ArrayList<MethodCall>());
//    }
//
//    public void setJar2(DepJar jar2) {
//        this.jar2 = jar2;
//    }
//
//    public void setRiskMethods(Set<String> riskMethods) {
//        this.riskMethods = riskMethods;
//    }
//
//    public IGraph getiGraph() {
//        return iGraph;
//    }
//
//    public void setiGraph(IGraph iGraph) {
//        this.iGraph = iGraph;
//    }
//
//
//    /**
//     * if has riskMethods, get graph, it's for scene4, has class missing, the priority is different from previous
//     * @param classDupRiskMemoryUnit for record
//     */
//    public Graph4path getGraph4MethodPath4classMissing(ClassDupRiskMemoryUnit classDupRiskMemoryUnit) {
//        Set<String> riskMethods; // high priority jar risk methods
//        Set<String> noUsedJarRiskMethods; // low priority jar risk methods
//        if (jar1.getPriority() < jar2.getPriority() && jar1.getPriority() != -1) {
//            riskMethods = getOnlyMethod(jar1, jar2);
//            noUsedJarRiskMethods = getOnlyMethod(jar2, jar1);
//            classDupRiskMemoryUnit.setLowPriorityJar(jar1);
//            classDupRiskMemoryUnit.setHighPriorityJar(jar2);
//        } else {
//            riskMethods = getOnlyMethod(jar2, jar1);
//            noUsedJarRiskMethods = getOnlyMethod(jar1, jar2);
//            classDupRiskMemoryUnit.setLowPriorityJar(jar2);
//            classDupRiskMemoryUnit.setHighPriorityJar(jar1);
//        }
//        classDupRiskMemoryUnit.setRiskMethods(riskMethods);
//        classDupRiskMemoryUnit.setNoUsedJarRiskMethods(noUsedJarRiskMethods);
//        if (riskMethods.size() > 0) {
//            try {
//                IGraph iGraph = null;
//                if (GlobalVar.i().sootProcess) {
//                    iGraph = neu.lab.conflict.sootprocess.SootJRiskCg.i().getGraphChooseCg(this, riskMethods, GlobalVar.i().cgAlgStr);
//                }
//                else {
//                    iGraph = SootJRiskCg.i().getGraphChooseCg(this, riskMethods, GlobalVar.i().cgAlgStr);
//                }
//                if (iGraph != null) {
//                    return (Graph4path) iGraph;
//                }
//            }
//            catch (Exception e) { System.err.println("Caught Exception!");
//                e.printStackTrace();
//            }
//        }
//        return new Graph4path(new HashMap<String, Node4path>(), new ArrayList<MethodCall>());
//    }
//
//    /**
//     * get jar classPaths to get call graph with soot
//     * @return List<String> classpaths
//     * @throws Exception
//     */
//    public Collection<String> getPrcDirPaths() throws Exception {
////        printDependencyMap();
//        List<String> classpaths;
//        if (!Conf.getInstance().CLASS_MISSING) {
//            if (jar1.getPriority() < jar2.getPriority() && jar1.getPriority() != -1) {
//                if (GlobalVar.i().useAllJarTwoThree) {
//                    classpaths = jar2.getRepalceCp();
//                } else {
//                    classpaths = getJarClassPathPruning(this.jar2);
//                }
//            } else {
//                if (GlobalVar.i().useAllJarTwoThree) {
//                    classpaths = jar1.getRepalceCp();
//                } else {
//                    classpaths = getJarClassPathPruning(this.jar1);
//                }
//            }
//        } else {
//            classpaths = getClassMissingPaths();
//        }
//        classpaths = SootUtil.getInstance().invalidClassPreprocess(classpaths);
//        return classpaths;
//    }
//    public Collection<DepJar> getPrcDirDepJars() throws Exception {
////        printDependencyMap();
//        List<DepJar> classpaths;
//        if (!Conf.getInstance().CLASS_MISSING) {
//            if (jar1.getPriority() < jar2.getPriority() && jar1.getPriority() != -1) {
//                if (GlobalVar.i().useAllJarTwoThree) {
//                    classpaths = jar2.getRepalceJarList();
//                } else {
//                    classpaths = getJarDepJarPruning(this.jar2);
//                }
//            } else {
//                if (GlobalVar.i().useAllJarTwoThree) {
//                    classpaths = jar1.getRepalceJarList();
//                } else {
//                    classpaths = getJarDepJarPruning(this.jar1);
//                }
//            }
//        } else {
//            classpaths = getClassMissingDepJars();
//        }
//        return classpaths;
//    }
//
//    public List<String> getClassMissingPaths() {
//        List<String> classpath = new ArrayList<>();
//        Set<String> paths = new HashSet<>();
//        if (jar1.getPriority() < jar2.getPriority() && jar1.getPriority() != -1) {
//            paths.addAll(getJarClassPathPruning(jar1));
//            Conflict conflict = null;
//            for (Conflict conflict1 : Conflicts.i().getConflicts()) {
//                if (conflict1.getSig().equals(jar2.getName())) {
//                    conflict = conflict1;
//                    break;
//                }
//            }
//            if (conflict != null) {
//                for (DepJar depJar : conflict.getDepJars()) {
//                    paths.addAll(getJarClassPathPruning(depJar));
//                    paths.removeAll(depJar.getJarFilePaths(true));
//                }
//            }
//        } else {
//            paths.addAll(getJarClassPathPruning(jar2));
//            Conflict conflict = null;
//            for (Conflict conflict1 : Conflicts.i().getConflicts()) {
//                if (conflict1.getSig().equals(jar1.getName())) {
//                    conflict = conflict1;
//                    break;
//                }
//            }
//            if (conflict != null) {
//                for (DepJar depJar : conflict.getDepJars()) {
//                    paths.addAll(getJarClassPathPruning(depJar));
//                    paths.removeAll(depJar.getJarFilePaths(true));
//                }
//            }
//        }
//        classpath.addAll(paths);
//        return classpath;
//    }
//    public List<DepJar> getClassMissingDepJars() {
//        List<DepJar> classpath = new ArrayList<>();
//        Set<DepJar> paths = new HashSet<>();
//        if (jar1.getPriority() < jar2.getPriority() && jar1.getPriority() != -1) {
//            paths.addAll(getJarDepJarPruning(jar1));
//            Conflict conflict = null;
//            for (Conflict conflict1 : Conflicts.i().getConflicts()) {
//                if (conflict1.getSig().equals(jar2.getName())) {
//                    conflict = conflict1;
//                    break;
//                }
//            }
//            if (conflict != null) {
//                for (DepJar depJar : conflict.getDepJars()) {
//                    paths.addAll(getJarDepJarPruning(depJar));
//                    paths.remove(depJar);
//                }
//            }
//        } else {
//            paths.addAll(getJarDepJarPruning(jar2));
//            Conflict conflict = null;
//            for (Conflict conflict1 : Conflicts.i().getConflicts()) {
//                if (conflict1.getSig().equals(jar1.getName())) {
//                    conflict = conflict1;
//                    break;
//                }
//            }
//            if (conflict != null) {
//                for (DepJar depJar : conflict.getDepJars()) {
//                    paths.addAll(getJarDepJarPruning(depJar));
//                    paths.remove(depJar);
//                }
//            }
//        }
//        classpath.addAll(paths);
//        return classpath;
//    }
//    /**
//     * get jar classPaths to get call graph with soot
//     * @return List<String> classpaths
//     * @throws Exception
//     */
//    public Collection<DepJar> getPrcDepJars() throws Exception {
////        printDependencyMap();
//        List<DepJar> classpaths;
//        if (!Conf.getInstance().CLASS_MISSING) {
//            if (jar1.getPriority() < jar2.getPriority() && jar1.getPriority() != -1) {
//                if (GlobalVar.i().useAllJarTwoThree) {
//                    classpaths = jar2.getRepalceJarList();
//                } else {
//                    classpaths = getJarPruning(this.jar2);
//                }
//            } else {
//                if (GlobalVar.i().useAllJarTwoThree) {
//                    classpaths = jar1.getRepalceJarList();
//                } else {
//                    classpaths = getJarPruning(this.jar1);
//                }
//            }
//        } else {
//            classpaths = getClassMissingJars();
//        }
//        return classpaths;
//    }
//
//    public List<DepJar> getClassMissingJars() {
//        List<DepJar> classpath = new ArrayList<>();
//        Set<DepJar> paths = new HashSet<>();
//        if (jar1.getPriority() < jar2.getPriority() && jar1.getPriority() != -1) {
//            paths.addAll(getJarPruning(jar1));
//            Conflict conflict = null;
//            for (Conflict conflict1 : Conflicts.i().getConflicts()) {
//                if (conflict1.getSig().equals(jar2.getName())) {
//                    conflict = conflict1;
//                    break;
//                }
//            }
//            if (conflict != null) {
//                for (DepJar depJar : conflict.getDepJars()) {
//                    paths.addAll(getJarPruning(depJar));
//                    paths.remove(depJar);
//                }
//            }
//        } else {
//            paths.addAll(getJarPruning(jar2));
//            Conflict conflict = null;
//            for (Conflict conflict1 : Conflicts.i().getConflicts()) {
//                if (conflict1.getSig().equals(jar1.getName())) {
//                    conflict = conflict1;
//                    break;
//                }
//            }
//            if (conflict != null) {
//                for (DepJar depJar : conflict.getDepJars()) {
//                    paths.addAll(getJarPruning(depJar));
//                    paths.remove(depJar);
//                }
//            }
//        }
//        classpath.addAll(paths);
//        return classpath;
//    }
//
//    private List<String> getJarClassPathPruning(DepJar depJar) {
//        List<String> classpaths = new ArrayList<>();
//        MyLogger.i().info("not add all jar to process");
//        try{
//            classpaths.addAll(depJar.getJarFilePaths(true));
//            classpaths.addAll(depJar.getOnlyFatherJarCps(true));
//        } catch (NullPointerException e) { System.err.println("Caught Exception!");
//            classpaths = new ArrayList<>();
//        }
//        return classpaths;
//    }
//    private List<DepJar> getJarDepJarPruning(DepJar depJar) {
//        List<DepJar> classpaths = new ArrayList<>();
//        MyLogger.i().info("not add all jar to process");
//        try{
//            classpaths.add(depJar);
//            classpaths.addAll(depJar.getOnlyFatherJars(true));
//        } catch (NullPointerException e) { System.err.println("Caught Exception!");
//            classpaths = new ArrayList<>();
//        }
//        return classpaths;
//    }
//    private List<DepJar> getJarPruning(DepJar depJar) {
//        List<DepJar> classpaths = new ArrayList<>();
//        MyLogger.i().info("not add all jar to process");
//        try{
//            classpaths.add(depJar);
//            classpaths.addAll(depJar.getOnlyFatherJars(true));
//        } catch (NullPointerException e) { System.err.println("Caught Exception!");
//            classpaths = new ArrayList<>();
//        }
//        return classpaths;
//    }
//
//
//    // @Override
//    // public int hashCode() {
//    // return jar1.hashCode() + jar2.hashCode();
//    // }
//    //
//    // @Override
//    // public boolean equals(Object obj) {
//    // if (obj instanceof JarCmp) {
//    // JarCmp other = (JarCmp) obj;
//    // return (jar1.equals(other.getJar1()) && jar2.equals(other.getJar2()))
//    // || (jar1.equals(other.getJar2()) && jar2.equals(other.getJar1()));
//    // }
//    // return false;
//    // }
//}
