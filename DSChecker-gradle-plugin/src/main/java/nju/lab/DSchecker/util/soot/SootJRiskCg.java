//package neu.lab.conflict.util.soot;
//
//import abandon.neu.lab.conflict.statics.DupClsJarPair;
//import neu.lab.conflict.util.JVMUtil;
//import neu.lab.conflict.util.MyLogger;
//import neu.lab.conflict.vo.GlobalVar;
//import neu.lab.conflict.container.DepJars;
//import neu.lab.conflict.graph.*;
//import neu.lab.conflict.graph.reverse.ReverseGraph4Reachability;
//import neu.lab.conflict.util.risk.DepJarJRisk;
//import neu.lab.conflict.util.soot.tf.JRiskCgTf;
//import neu.lab.conflict.util.SootUtil;
//import nju.lab.DSchecker.model.DepJar;
//import neu.lab.conflict.vo.GsonEdgeVO;
//import neu.lab.conflict.vo.MethodCall;
//import org.jgrapht.graph.DefaultEdge;
//import org.jgrapht.graph.concurrent.AsSynchronizedGraph;
//import soot.PackManager;
//import soot.SootMethod;
//import soot.Transform;
//import soot.jimple.toolkits.callgraph.CallGraph;
//import soot.jimple.toolkits.callgraph.Edge;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.Writer;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicLong;
//
///**
// * use soot to get call graph of the host project and its dependencies，then use IGraph data structure to process call graph
// */
//public class SootJRiskCg extends AbstractSootAna {
//    private static SootJRiskCg instance = new SootJRiskCg();
//    private SootJRiskCg() {
//    }
//    public static SootJRiskCg i() {
//        return instance;
//    }
//    /**
//     * get Call graph with user specified call graph generation algorithm!!!
//     * by grj
//     */
//    public IGraph getGraph4distanceChooseCg(DepJarJRisk depJarJRisk) {
//        IGraph graph = null;
//        CallGraph cg = null;
//        long start = System.currentTimeMillis();
//        try {
//            //Collection<String> prcDirPaths = depJarJRisk.getPrcDirPaths();
//            List<DepJar> prcDirDeps = new ArrayList<>(depJarJRisk.getPrcDepJars());
////            DepJars.i().sortDepJarsLetter(prcDirDeps);
//            DepJars.i().sortDepJars(prcDirDeps);
//            List<String> tmpPrcDirPaths = new ArrayList<>();
//            prcDirDeps.forEach(prcDirDep -> {
//                if (prcDirDep.getPriority() >= 0 || prcDirDep.getPriority() == -2) {
//                    tmpPrcDirPaths.add(prcDirDep.getJarFilePath());
//                } else{
//                    if (prcDirDep.getName().equals(depJarJRisk.getDepJar().getName())) {
//                        tmpPrcDirPaths.add(prcDirDep.getJarFilePath());
//                    }
//                }
//            });
//            if (GlobalVar.i().isTest) {
//                tmpPrcDirPaths.add(DepJars.i().getHostDepJar().getJarFilePath().replace("test-classes", "classes"));
//            }
//            List<String> prcDirPaths = SootUtil.getInstance().invalidClassPreprocess(tmpPrcDirPaths);
//            if (prcDirPaths.isEmpty()) {
//                return new Graph4distance(new HashMap<>(), new ArrayList<>());
//            }
//            // TODO test exclude for risk
//            if (!GlobalVar.i().sootExcludePkg) {
//                cg = SootCgAna.i().getCallGraphNoCpExcludeForRisk(depJarJRisk, GlobalVar.i().cgAlgStr);
//            } else {
//                cg = SootCgAna.i().getCallGraphForRisk(depJarJRisk.getConflictJar(), prcDirPaths, GlobalVar.i().cgAlgStr);
//            }
//            MyLogger.i().info(JVMUtil.getInstance().toMemoryInfo());
//            System.out.println("no Cg Fast cg edges size" + cg.size());
//            //System.out.println(cg);
//            long timeStamp2 = System.currentTimeMillis();
//            graph = generateGraph4distanceWithCg(depJarJRisk, cg);
//            if (GlobalVar.i().writeCgToFile) {
//                int debugCgFileName = System.identityHashCode(cg);
//                File debugCgFile = new File(UserConf.getInstance().getOutDir() + File.separator + String.valueOf(debugCgFileName));
//                Writer debugCgWriter = new BufferedWriter(new FileWriter(debugCgFile));
//                MyLogger.i().info("callgraphfor " + depJarJRisk.getConflictJar().getSig() + " :" + debugCgFileName);
//                for (Edge edge : cg) {
//                    debugCgWriter.write(edge.toString() + '\n');
//                }
//                debugCgWriter.close();
//            }
//        }
//        catch (Exception e) { System.err.println("Caught Exception!");
//            MyLogger.i().error("SootJRiskCg.getGraph4distanceWithSpark");
//            e.printStackTrace();
//        }
//        soot.G.reset();
//        long runtime = (System.currentTimeMillis() - start) / 1000;
//        GlobalVar.i().time2cg += runtime;
//        return graph;
//    }
//    /**
//     * get Call graph fast by song xiaohu's plugin
//     * by grj
//     */
//    public IGraph getGraph4distanceChooseCgFast(DepJarJRisk depJarJRisk) {
//        IGraph graph = null;
//        AsSynchronizedGraph<String, DefaultEdge> cg = null;
//        long start = System.currentTimeMillis();
//        try {
//            List<DepJar> prcDirDeps = new ArrayList<>(depJarJRisk.getPrcDepJars());
//            //DepJars.i().sortDepJarsLetter(prcDirDeps);
//            DepJars.i().sortDepJars(prcDirDeps);
//            List<String> tmpPrcDirPaths = new ArrayList<>();
//            prcDirDeps.forEach(prcDirDep -> {
//                if (prcDirDep.getPriority() >= 0 || prcDirDep.getPriority() == -2) {
//                    tmpPrcDirPaths.add(prcDirDep.getJarFilePath());
//                } else{
//                    if (prcDirDep.getName().equals(depJarJRisk.getDepJar().getName())) {
//                        tmpPrcDirPaths.add(prcDirDep.getJarFilePath());
//                    }
//                }
//            });
//            List<String> prcDirPaths = SootUtil.getInstance().invalidClassPreprocess(tmpPrcDirPaths);
//            //Collection<String> prcDirPaths = depJarJRisk.getPrcDirPaths();
//            if (GlobalVar.i().isTest) {
//                prcDirPaths.add(1, DepJars.i().getHostDepJar().getJarFilePath().replace("test-classes", "classes"));
//            }
//            if (GlobalVar.i().addJdk) {
//                prcDirPaths.add(MavenUtil.getInstance().getMvnRep()
//                        + File.separator + "neu"
//                        + File.separator + "lab"
//                        + File.separator + "rt"
//                        + File.separator + "1.0.0"
//                        + File.separator + "rt-1.0.0.jar");
//            }
//            if (prcDirPaths.isEmpty()) {
//                return new Graph4distance(new HashMap<>(), new ArrayList<>());
//            }
//            MyLogger.i().info(JVMUtil.getInstance().toMemoryInfo());
//            // TODO test exclude for risk
//            if (GlobalVar.i().sootExcludePkg) {
//                MyLogger.i().info("use xxx exclude get call graph for " + depJarJRisk.getConflictJar() + prcDirPaths);
//               /* List<String> excludePkgs = SootCgAna.i().getExcludeList(new ArrayList<>(depJarJRisk.getPrcDepJars()));*/
//                cg = GlobalVar.i().cm.build(new ArrayList<>(prcDirPaths), DepJars.i().getHostDepJar().getAllMthd(),GlobalVar.i().extendThreadNum, GlobalVar.i().callGraphThreadNum/*, GlobalVar.i().sceneInclude*/);
//            }
//            else {
//                MyLogger.i().info("use xxx get call graph for " + depJarJRisk.getConflictJar() + prcDirPaths);
//                cg = GlobalVar.i().cm.build(new ArrayList<>(prcDirPaths), DepJars.i().getHostDepJar().getAllMthd(), GlobalVar.i().extendThreadNum, GlobalVar.i().callGraphThreadNum/*, GlobalVar.i().sceneInclude*/);
//            }
//            System.out.println("cg Fast cg edges size" + cg.edgeSet().size());
//            long timeStamp2 = System.currentTimeMillis();
//            graph = generateGraph4distanceWithCg(depJarJRisk, cg);
//            if (GlobalVar.i().writeCgToFile) {
//                int debugCgFileName = System.identityHashCode(cg);
//                File debugCgFile = new File(UserConf.getInstance().getOutDir() + File.separator + String.valueOf(debugCgFileName));
//                Writer debugCgWriter = new BufferedWriter(new FileWriter(debugCgFile));
//                MyLogger.i().info("callgraphcgfastfor " + depJarJRisk.getConflictJar().getSig() + " :" + debugCgFileName);
//                for (DefaultEdge edge : cg.edgeSet()) {
//                    debugCgWriter.write(edge.toString() + '\n');
//                }
//                debugCgWriter.close();
//            }
//        }
//        catch (Exception e) { System.err.println("Caught Exception!");
//            MyLogger.i().error("SootJRiskCg.getGraph4distanceWithSpark");
//            e.printStackTrace();
//        }
//        long runtime = (System.currentTimeMillis() - start) / 1000;
//        GlobalVar.i().time2cg += runtime;
//        return graph;
//    }
//    /**
//     * get Call graph fast by song xiaohu's plugin
//     * by grj
//     */
//    public IGraph getGraph4distanceChooseCgFast4Scene6(DepJarJRisk depJarJRisk) {
//        IGraph graph = null;
//        AsSynchronizedGraph<String, DefaultEdge> cg = null;
//        long start = System.currentTimeMillis();
//        try {
//            Collection<String> prcDirPaths = depJarJRisk.getPrcDirPaths4Scene6();
//            if (prcDirPaths.isEmpty()) {
//                return new Graph4distance(new HashMap<>(), new ArrayList<>());
//            }
//            // TODO test exclude for risk
//            if (GlobalVar.i().sootExcludePkg) {
//                MyLogger.i().info("use xxx exclude get call graph for " + depJarJRisk.getConflictJar() + prcDirPaths);
//                /* List<String> excludePkgs = SootCgAna.i().getExcludeList(new ArrayList<>(depJarJRisk.getPrcDepJars()));*/
//                cg = GlobalVar.i().cm.build(new ArrayList<>(prcDirPaths), DepJars.i().getHostDepJar().getAllMthd(),GlobalVar.i().extendThreadNum, GlobalVar.i().callGraphThreadNum/*, GlobalVar.i().sceneInclude*/);
//            }
//            else {
//                MyLogger.i().info("use xxx get call graph for " + depJarJRisk.getConflictJar() + prcDirPaths);
//                cg = GlobalVar.i().cm.build(new ArrayList<>(prcDirPaths), DepJars.i().getHostDepJar().getAllMthd(), GlobalVar.i().extendThreadNum, GlobalVar.i().callGraphThreadNum/*, GlobalVar.i().sceneInclude*/);
//            }
//            long timeStamp2 = System.currentTimeMillis();
//            graph = generateGraph4distanceWithCg(depJarJRisk, cg);
//        }
//        catch (Exception e) { System.err.println("Caught Exception!");
//            MyLogger.i().error("SootJRiskCg.getGraph4distanceWithSpark");
//            e.printStackTrace();
//        }
//        long runtime = (System.currentTimeMillis() - start) / 1000;
//        GlobalVar.i().time2cg += runtime;
//        return graph;
//    }
//    /**
//     * cycle cg detect
//     * @param depJarJRisk
//     * @return
//     */
//    public IGraph getGraph4distance4Scene6ChooseCg(DepJarJRisk depJarJRisk) {
//        IGraph graph = null;
//        CallGraph cg = null;
//        long start = System.currentTimeMillis();
//        try {
//            /**
//             * By grj. Concat callgraph
//             */
//            Collection<String> prcDirPaths = depJarJRisk.getPrcDirPaths4Scene6();
//            if (prcDirPaths.isEmpty()) {
//                return new Graph4distance(new HashMap<>(), new ArrayList<>());
//            }
//            Collection<DepJar> prcDirDepJars = depJarJRisk.getPrcDepJars4Scene6();
//            List<DepJar> seqDepJars = new ArrayList<>(prcDirDepJars);
//            DepJars.i().sortDepJars(seqDepJars);
//            //host ... lastdepjar
//            List<DepJar> part1 = seqDepJars.subList(0, seqDepJars.size() - 1);
//            List<DepJar> part2 = seqDepJars.subList(1, seqDepJars.size());
//            List<String> part1Paths = new ArrayList<>();
//            List<String> part2Paths = new ArrayList<>();
//            part1.forEach(depJar -> part1Paths.add(depJar.getJarFilePath()));
//            part2.forEach(depJar -> part2Paths.add(depJar.getJarFilePath()));
//            CallGraph cg1 = SootCgAna.i().getCallGraphNoCp(part1Paths, SootCgAna.CGAlgorithm.SPARK);
//            CallGraph cg2 = SootCgAna.i().getCallGraphNoCp(part2Paths, SootCgAna.CGAlgorithm.CHA);
//            ReverseGraph4Reachability reverseGraph = new ReverseGraph4Reachability(cg2);
//            Set<String> cg1ReachableMethods = new HashSet<>();
//            for (Edge edge1 : cg1) {
//                cg1ReachableMethods.add(edge1.tgt().getSignature());
//            }
//            Set<String> cg2ReverseReachableMethods = reverseGraph
//                    .getReverseReachableMethods(depJarJRisk.newGetThrownMthds());
//            if (cg2ReverseReachableMethods
//                    .stream()
//                    .anyMatch(cg2RsvRchbMthd -> cg1ReachableMethods.contains(cg2RsvRchbMthd))) {
//                cg1.forEach(edge -> cg2.addEdge(edge));
//                long timeStamp2 = System.currentTimeMillis();
//                graph = generateGraph4distanceWithCg(depJarJRisk, cg2);
//            }
//        }
//        catch (Exception e) { System.err.println("Caught Exception!");
//            MyLogger.i().error("SootJRiskCg.getGraph4distanceWithSpark");
//            e.printStackTrace();
//        }
//        soot.G.reset();
//        long runtime = (System.currentTimeMillis() - start) / 1000;
//        GlobalVar.i().time2cg += runtime;
//        return graph;
//    }
//    private int getBranchNum(Map<String, Integer> mthd2branch, String mthd) {
//        Integer branchNum = mthd2branch.get(mthd);
//        if (null != branchNum) {
//            return branchNum;
//        }
//        return 0;
//    }
//    /**
//     * By grj. Use Buffer to get Graph4distance.
//     *
//     * @param bufCg
//     * @param depJarJRisk
//     * @return
//     */
//    public IGraph generateGraphWithBuffer(Collection<GsonEdgeVO> bufCg, DepJarJRisk depJarJRisk) {
//        long start = System.currentTimeMillis();
//        MyLogger.i().info("start form graph with buffer");
//        Set<String> entryClses;    //入口类集合
//        Set<String> conflictJarClses;        //冲突jar类集合
//        Set<String> riskMthds = depJarJRisk.newGetThrownMthds();    //风险方法集合
//        Map<String, Integer> mthd2branch = new HashMap<>();
//        entryClses = depJarJRisk.getEntryJar().getAllCls(true);
//        conflictJarClses = depJarJRisk.getConflictJar().getAllCls(true);
//        IGraph graph = null;
//        Map<String, Node4distance> name2node = new HashMap<String, Node4distance>(16384);
//        // 初始化20000大小
//        List<MethodCall> mthdRlts = new ArrayList<MethodCall>(16384);
//        for (GsonEdgeVO gsonEdge : bufCg) {
//            /*if (gsonEdge.getSrcLibrary() || gsonEdge.getTgtLibrary()) {
//            } else {*/
//            //if (gsonEdge.getSrcConcrete() && gsonEdge.getTgtConcrete()) {
//            String srcMthdSig = gsonEdge.getSrcMthdSig();// 源方法名
//            String tgtMthdSig = gsonEdge.getTgtMthdSig();// 目标方法名
//            String srcClsName = gsonEdge.getSrcClsName();// 源方法的类名
//            String tgtClsName = gsonEdge.getTgtClsName();// 目标方法的类名
//            if (conflictJarClses.contains(SootUtil.getInstance().mthdSig2cls(srcMthdSig))
//                    && conflictJarClses.contains(SootUtil.getInstance().mthdSig2cls(tgtMthdSig))) {
//                // filter relation inside conflictJar 过滤掉conflictJar中的类
//            } else {
//                if (!name2node.containsKey(srcMthdSig)) {
//                    name2node.put(srcMthdSig, new Node4distance(srcMthdSig,
//                            (entryClses.contains(srcClsName) && !LibCopyInfo.isLibCopy(MavenUtil.getInstance().getProjectCor(), srcClsName)) && !gsonEdge.getSrcPrivate(),
//                            riskMthds.contains(srcMthdSig), getBranchNum(mthd2branch, srcMthdSig)));
//                }
//                if (!name2node.containsKey(tgtMthdSig)) {
//                    name2node.put(tgtMthdSig, new Node4distance(tgtMthdSig,
//                            (entryClses.contains(tgtClsName) && !LibCopyInfo.isLibCopy(MavenUtil.getInstance().getProjectCor(), tgtClsName)) && !gsonEdge.getTgtPrivate(),
//                            riskMthds.contains(tgtMthdSig), getBranchNum(mthd2branch, tgtMthdSig)));
//                }
//                mthdRlts.add(new MethodCall(srcMthdSig, tgtMthdSig));
//            }
//            //}
//            //}
//        }
//        graph = new Graph4distance(name2node, mthdRlts);
//        System.gc();
//        MyLogger.i().info("end form graph with buffer.");
//        MyLogger.i().info("form graph with buffer time:" + (System.currentTimeMillis() - start));
//        return graph;
//    }
//    public Map<String, Object> func(CallGraph cg) {
//        for (Edge e : cg) {
//            SootMethod src = e.src();
//            SootMethod tgt = e.tgt();
//            //System.out.println(src.);
//        }
//        return new HashMap<>();
//    }
//    public IGraph getGraph(DupClsJarPair dupClsJarPair, JRiskCgTf transformer) {
//        MyLogger.i().info("use soot to compute reach methods for " + dupClsJarPair.getSig());
//        IGraph graph = null;
//        long start = System.currentTimeMillis();
//        try {
//            SootUtil.getInstance().modifyLogOut();
//            PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", transformer));
//            soot.Main.main(getArgs(dupClsJarPair.getPrcDirPaths().toArray(new String[0])).toArray(new String[0]));
//            graph = transformer.getGraph();
//        } catch (Exception e) { System.err.println("Caught Exception!");
//            MyLogger.i().warn("cg error: ", e);
//        }
//        soot.G.reset();
//        long runtime = (System.currentTimeMillis() - start) / 1000;
//        GlobalVar.i().time2cg += runtime;
//        return graph;
//    }
//    /**
//     * get graph for scene2
//     * @param dupClsJarPair
//     * @param riskMethods
//     * @param cgAlgStr
//     * @return
//     */
//    public IGraph getGraphChooseCg(DupClsJarPair dupClsJarPair, Set<String> riskMethods, String cgAlgStr) {
//        MyLogger.i().info("SootJRiskCg.java: use soot to compute reach methods for " + dupClsJarPair.getSig());
//        IGraph graph = null;
//        CallGraph cg = null;
//        long start = System.currentTimeMillis();
//        try {
//            Collection<String> prcDirPaths = dupClsJarPair.getPrcDirPaths();
//            if (GlobalVar.i().isTest) {
//                prcDirPaths.add(DepJars.i().getHostDepJar().getJarFilePath().replace("test-classes", "classes"));
//            }
//            if (GlobalVar.i().sootExcludePkg) {
//                cg = SootCgAna.i().getCallGraphNoCpExcludeForClsDup(dupClsJarPair, riskMethods, GlobalVar.i().cgAlgStr);
//            } else {
//                cg = SootCgAna.i().getCallGraphNoCp(prcDirPaths, cgAlgStr);
//            }
//            //cg = SootCgAna.i().getCallGraphNoCp(prcDirPaths, cgAlgStr);
//            //System.out.println(cg);
//            graph = generateGraph4pathWithCg(riskMethods, cg);
//        }
//        catch (Exception e) { System.err.println("Caught Exception!");
//            MyLogger.i().error("SootJRiskCg.getGraph4distanceWithSpark");
//            e.printStackTrace();
//        }
//        soot.G.reset();
//        long runtime = (System.currentTimeMillis() - start) / 1000;
//        GlobalVar.i().time2cg += runtime;
//        return graph;
//    }
//    /**
//     * get graph for scene2
//     * @param dupClsJarPair
//     * @param riskMethods
//     * @return
//     */
//    public IGraph getGraphChooseCgFast(DupClsJarPair dupClsJarPair, Set<String> riskMethods) {
//        MyLogger.i().info("SootJRiskCg.java: use soot to compute reach methods for " + dupClsJarPair.getSig());
//        IGraph graph = null;
//        AsSynchronizedGraph cg = null;
//        long start = System.currentTimeMillis();
//        try {
//            Collection<String> prcDirPaths = dupClsJarPair.getPrcDirPaths();
//            MyLogger.i().info("prcDirPaths: " + prcDirPaths);
//            Collection<DepJar> prcDepJars = dupClsJarPair.getPrcDepJars();
//            if (GlobalVar.i().isTest) {
//                prcDirPaths.add(DepJars.i().getHostDepJar().getJarFilePath().replace("test-classes", "classes"));
//            }
//            if (GlobalVar.i().sootExcludePkg) {
//                cg = GlobalVar.i().cm.build(new ArrayList<>(prcDirPaths), DepJars.i().getHostDepJar().getAllMthd(), GlobalVar.i().extendThreadNum, GlobalVar.i().callGraphThreadNum/*, GlobalVar.i().sceneInclude*/);
//            }
//            else {
//                cg = GlobalVar.i().cm.build(new ArrayList<>(prcDirPaths), DepJars.i().getHostDepJar().getAllMthd(), GlobalVar.i().extendThreadNum, GlobalVar.i().callGraphThreadNum/*, GlobalVar.i().sceneInclude*/);
//            }
//            long timeStamp2 = System.currentTimeMillis();
//            graph = generateGraph4pathWithCg(riskMethods, cg);
//        }
//        catch (Exception e) { System.err.println("Caught Exception!");
//            MyLogger.i().error("SootJRiskCg.getGraph4distanceWithSpark");
//            e.printStackTrace();
//        }
//        long runtime = (System.currentTimeMillis() - start) / 1000;
//        GlobalVar.i().time2cg += runtime;
//        return graph;
//    }
//    @Override
//    protected void addCgArgs(List<String> argsList) {
//        argsList.addAll(Arrays.asList(new String[]{"-p", "cg", "off",}));
//    }
//    private IGraph generateGraph4pathWithCg(Set<String> riskMthds, CallGraph cg) {
//        IGraph graph = null;
//        Set<String> entryClses = DepJars.i().getHostDepJar().getAllCls(true);
//        if (graph == null) {
//            MyLogger.i().info("start form graph...");
//            // get call-graph.
//            Map<String, Node4path> name2node = new HashMap<String, Node4path>();
//            List<MethodCall> mthdRlts = new ArrayList<MethodCall>();
//            Iterator<Edge> ite = cg.iterator();
//            while (ite.hasNext()) {
//                Edge edge = ite.next();
//                String srcMthdSig = edge.src().getSignature();
//                String tgtMthdSig = edge.tgt().getSignature();
//                // //TODO1
//                // if("<com.fasterxml.jackson.core.JsonFactory: boolean
//                // requiresPropertyOrdering()>".equals(tgtMthdSig)) {
//                // MyLogger.i().info("srcMthdSig:"+srcMthdSig);
//                // }
//                String srcClsName = edge.src().getDeclaringClass().getName();
//                String tgtClsName = edge.tgt().getDeclaringClass().getName();
//                if (edge.src().isJavaLibraryMethod() || edge.tgt().isJavaLibraryMethod()) {
//                    // filter relation contains javaLibClass
//                } else {
//                    if (!name2node.containsKey(srcMthdSig)) {
//                        name2node.put(srcMthdSig, new Node4path(srcMthdSig,
//                                (entryClses.contains(srcClsName) && !LibCopyInfo.isLibCopy(MavenUtil.getInstance().getProjectCor(), srcClsName))
//                                        && !edge.src().isPrivate(),
//                                riskMthds.contains(srcMthdSig)));
//                    }
//                    if (!name2node.containsKey(tgtMthdSig)) {
//                        name2node.put(tgtMthdSig, new Node4path(tgtMthdSig,
//                                (entryClses.contains(tgtClsName) && !LibCopyInfo.isLibCopy(MavenUtil.getInstance().getProjectCor(), tgtClsName))
//                                        && !edge.tgt().isPrivate(),
//                                riskMthds.contains(tgtMthdSig)));
//                    }
//                    mthdRlts.add(new MethodCall(srcMthdSig, tgtMthdSig));
//                }
//            }
//            graph = new Graph4path(name2node, mthdRlts);
//            MyLogger.i().info("end form graph.");
//        }
//        return graph;
//    }
//    /**
//     * for class dup risk, generate Graph4path with Xiaohu's AsSynchronizedGraph
//     * @param riskMthds
//     * @param cg
//     * @return
//     */
//    private IGraph generateGraph4pathWithCg(Set<String> riskMthds, AsSynchronizedGraph<String, DefaultEdge> cg) {
//        IGraph graph = null;
//        Set<String> entryClses = DepJars.i().getHostDepJar().getAllCls(true);
//        if (graph == null) {
//            MyLogger.i().info("start form graph...");
//            // get call-graph.
//            Map<String, Node4path> name2node = new HashMap<String, Node4path>();
//            List<MethodCall> mthdRlts = new ArrayList<MethodCall>();
//            Iterator<DefaultEdge> ite = cg.edgeSet().iterator();
//            while (ite.hasNext()) {
//                DefaultEdge edge = ite.next();
//                String srcMthdSig = cg.getEdgeSource(edge);
//                String tgtMthdSig = cg.getEdgeTarget(edge);
//                // //TODO1
//                // if("<com.fasterxml.jackson.core.JsonFactory: boolean
//                // requiresPropertyOrdering()>".equals(tgtMthdSig)) {
//                // MyLogger.i().info("srcMthdSig:"+srcMthdSig);
//                // }
//                String srcClsName = SootUtil.getInstance().mthdSig2cls(srcMthdSig);
//                String tgtClsName = SootUtil.getInstance().mthdSig2cls(tgtMthdSig);
//                // TODO maybe no need to judge Java Lib Classes
//                if (SootUtil.getInstance().isJavaLibraryClass(srcClsName) || SootUtil.getInstance().isJavaLibraryClass(tgtClsName)) {
//                    // filter relation contains javaLibClass
//                } else {
//                    if (!name2node.containsKey(srcMthdSig)) {
//                        name2node.put(srcMthdSig, new Node4path(srcMthdSig,
//                                (entryClses.contains(srcClsName) && !LibCopyInfo.isLibCopy(MavenUtil.getInstance().getProjectCor(), srcClsName))
//                                       ,
//                                riskMthds.contains(srcMthdSig)));
//                    }
//                    if (!name2node.containsKey(tgtMthdSig)) {
//                        name2node.put(tgtMthdSig, new Node4path(tgtMthdSig,
//                                (entryClses.contains(tgtClsName) && !LibCopyInfo.isLibCopy(MavenUtil.getInstance().getProjectCor(), tgtClsName))
//                                        ,
//                                // ? delete is Private Judge
//                                riskMthds.contains(tgtMthdSig)));
//                    }
//                    mthdRlts.add(new MethodCall(srcMthdSig, tgtMthdSig));
//                }
//            }
//            graph = new Graph4path(name2node, mthdRlts);
//            MyLogger.i().info("end form graph.");
//        }
//        return graph;
//    }
//    private IGraph generateGraph4distanceWithCg(DepJarJRisk depJarJRisk, CallGraph cg) {
//        long start = System.currentTimeMillis();
//        MyLogger.i().info("start form graph with cg");
//        Set<String> entryClses;    //入口类集合
//        Set<String> conflictJarClses;        //冲突jar类集合
//        Set<String> riskMthds = depJarJRisk.newGetThrownMthds();    //风险方法集合
//        Map<String, Integer> mthd2branch = new HashMap<>();
//        entryClses = depJarJRisk.getEntryJar().getAllCls(true);
//        conflictJarClses = depJarJRisk.getConflictJar().getAllCls(true);
//        IGraph graph = null;
//        Map<String, Node4distance> name2node = new HashMap<String, Node4distance>(16384);
//        // 初始化20000大小
//        List<MethodCall> mthdRlts = new ArrayList<MethodCall>(16384);
//        Iterator<Edge> ite = cg.iterator();
//        System.out.println("==============================================");
//        System.out.println("before iter " + (System.currentTimeMillis() - start));
//        System.out.println("==============================================");
//        AtomicLong sig2ClsTime = new AtomicLong(0);
//        long cgOpTime = System.currentTimeMillis();
//        while (ite.hasNext()) {
//            Edge edge = ite.next();
//            SootMethod source = edge.src();
//            SootMethod target = edge.tgt();
//            if (source.isJavaLibraryMethod() || target.isJavaLibraryMethod()) {
//                // filter relation contains javaLibClass 过滤掉JavaLib的类
//            } else {
//                if (source.isConcrete() && target.isConcrete()) {
//                    String srcMthdSig = source.getSignature();// 源方法名
//                    String tgtMthdSig = target.getSignature();// 目标方法名
//                    String srcClsName = source.getDeclaringClass().getName();// 源方法的类名
//                    String tgtClsName = target.getDeclaringClass().getName();// 目标方法的类名
//                    if (conflictJarClses.contains(SootUtil.getInstance().mthdSig2cls(srcMthdSig))
//                            && conflictJarClses.contains(SootUtil.getInstance().mthdSig2cls(tgtMthdSig))) {
//                        // filter relation inside conflictJar 过滤掉conflictJar中的类
//                    } else {
//                        if (!name2node.containsKey(srcMthdSig)) {
//                            name2node.put(srcMthdSig, new Node4distance(srcMthdSig,
//                                    (entryClses.contains(srcClsName) && !LibCopyInfo.isLibCopy(MavenUtil.getInstance().getProjectCor(), srcClsName)) && !source.isPrivate(),
//                                    riskMthds.contains(srcMthdSig), getBranchNum(mthd2branch, srcMthdSig)));
//                        }
//                        if (!name2node.containsKey(tgtMthdSig)) {
//                            name2node.put(tgtMthdSig, new Node4distance(tgtMthdSig,
//                                    (entryClses.contains(tgtClsName) && !LibCopyInfo.isLibCopy(MavenUtil.getInstance().getProjectCor(), tgtClsName)) && !target.isPrivate(),
//                                    riskMthds.contains(tgtMthdSig), getBranchNum(mthd2branch, tgtMthdSig)));
//                        }
//                        mthdRlts.add(new MethodCall(srcMthdSig, tgtMthdSig));
//                    }
//                }
//            }
//        }
//        System.out.println("cycle time " + (System.currentTimeMillis() - cgOpTime));
//        System.out.println("==============================================");
//        System.out.println("cls2sig time " + sig2ClsTime);
//        System.out.println("==============================================");
//        System.out.println(name2node.size() + mthdRlts.size());
//        long timeStamp4 = System.currentTimeMillis();
//        graph = new Graph4distance(name2node, mthdRlts);
//        System.out.println("newGraph4distance time:" + (System.currentTimeMillis() - timeStamp4));
//        long timeStamp3 = System.currentTimeMillis();
//        //System.gc();
//        System.out.println("gc time:" + (System.currentTimeMillis() - timeStamp3));
//        MyLogger.i().info("end form graph with cg.");
//        MyLogger.i().info("form graph with cg time:" + (System.currentTimeMillis() - start));
//        return graph;
//    }
//    /**
//     * generate graph4distance for Xiaohu's AsSynchronizedGraph
//     * @param depJarJRisk
//     * @param cg
//     * @return
//     */
//    private IGraph generateGraph4distanceWithCg(DepJarJRisk depJarJRisk, AsSynchronizedGraph<String, DefaultEdge> cg) {
//        long start = System.currentTimeMillis();
//        MyLogger.i().info("start form graph with cg");
//        Set<String> entryClses;    //入口类集合
//        Set<String> conflictJarClses;        //冲突jar类集合
//        Set<String> riskMthds = depJarJRisk.newGetThrownMthds();    //风险方法集合
//        Map<String, Integer> mthd2branch = new HashMap<>();
//        entryClses = depJarJRisk.getEntryJar().getAllCls(true);
//        conflictJarClses = depJarJRisk.getConflictJar().getAllCls(true);
//        IGraph graph = null;
//        Map<String, Node4distance> name2node = new HashMap<String, Node4distance>(16384);
//        // 初始化20000大小
//        List<MethodCall> mthdRlts = new ArrayList<MethodCall>(16384);
//        System.out.println("==============================================");
//        System.out.println("before iter " + (System.currentTimeMillis() - start));
//        System.out.println("==============================================");
//        Iterator<String> vertexIt = cg.vertexSet().iterator();
//        AtomicLong sig2ClsTime = new AtomicLong(0);
//        long cgOpTime = System.currentTimeMillis();
//        while (vertexIt.hasNext()) {
//            String vertex = vertexIt.next();
//            String srcMthdSig = vertex;
//            long timeStamp = System.currentTimeMillis();
//            String srcClsName = SootUtil.getInstance().mthdSig2cls(srcMthdSig);// 源方法的类名
//            sig2ClsTime.addAndGet(System.currentTimeMillis() - timeStamp);
//            cg.outgoingEdgesOf(vertex).forEach(
//                    outEdge -> {
//                        String tgtMthdSig = cg.getEdgeTarget(outEdge);
//                        long timeStamp2 = System.currentTimeMillis();
//                        String tgtClsName = SootUtil.getInstance().mthdSig2cls(tgtMthdSig);
//                        sig2ClsTime.addAndGet(System.currentTimeMillis() - timeStamp2);
//                        if (conflictJarClses.contains(srcClsName)
//                                && conflictJarClses.contains(tgtClsName)) {
//                            // filter relation inside conflictJar 过滤掉conflictJar中的类
//                        } else {
//                            if (!name2node.containsKey(srcMthdSig)) {
//                                name2node.put(srcMthdSig, new Node4distance(srcMthdSig,
//                                        (entryClses.contains(srcClsName) && !LibCopyInfo.isLibCopy(MavenUtil.getInstance().getProjectCor(), srcClsName)),
//                                        riskMthds.contains(srcMthdSig), getBranchNum(mthd2branch, srcMthdSig)));
//                            }
//                            if (!name2node.containsKey(tgtMthdSig)) {
//                                name2node.put(tgtMthdSig, new Node4distance(tgtMthdSig,
//                                        (entryClses.contains(tgtClsName) && !LibCopyInfo.isLibCopy(MavenUtil.getInstance().getProjectCor(), tgtClsName)),
//                                        riskMthds.contains(tgtMthdSig), getBranchNum(mthd2branch, tgtMthdSig)));
//                            }
//                            mthdRlts.add(new MethodCall(srcMthdSig, tgtMthdSig));
//                        }
//                    }
//            );
//        }
//        System.out.println("cycle time " + (System.currentTimeMillis() - cgOpTime));
//        System.out.println("==============================================");
//        System.out.println("cls2sig time " + sig2ClsTime);
//        System.out.println("==============================================");
//        System.out.println(name2node.size() + mthdRlts.size());
//        long timeStamp4 = System.currentTimeMillis();
//        graph = new Graph4distance(name2node, mthdRlts);
//        System.out.println("newGraph4distance time:" + (System.currentTimeMillis() - timeStamp4));
//        System.out.println("==============================================");
//        long timeStamp3 = System.currentTimeMillis();
//        //System.gc();
//        System.out.println("gc time:" + (System.currentTimeMillis() - timeStamp3));
//        MyLogger.i().info("end form graph with cg.");
//        MyLogger.i().info("form graph with cg time:" + (System.currentTimeMillis() - start));
//        return graph;
//    }
//    /**
//     * soot process zizhu diaoyonggtu
//     * @param entryClses
//     * @param conflictJarClses
//     * @param riskMthds
//     * @param at_aplit_edges
//     * @return
//     */
//    public IGraph generateGraph4distanceWithCg(Set<String> entryClses, Set<String> conflictJarClses, Set<String> riskMthds, List<String> at_aplit_edges) {
//        long start = System.currentTimeMillis();
//        MyLogger.i().info("start form graph with cg");
//        Map<String, Integer> mthd2branch = new HashMap<>();
//        IGraph graph = null;
//        Map<String, Node4distance> name2node = new HashMap<String, Node4distance>(16384);
//        // 初始化20000大小
//        List<MethodCall> mthdRlts = new ArrayList<MethodCall>(16384);
//        System.out.println("==============================================");
//        System.out.println("before iter " + (System.currentTimeMillis() - start));
//        System.out.println("==============================================");
//        Iterator<String> edgeIt = at_aplit_edges.iterator();
//        AtomicLong sig2ClsTime = new AtomicLong(0);
//        long cgOpTime = System.currentTimeMillis();
//        while (edgeIt.hasNext()) {
//            String at_split_edge = edgeIt.next();
//            String[] src_tgt = at_split_edge.split("@");
//            String srcMthdSig = src_tgt[0];
//            String tgtMthdSig = src_tgt[1];
//            String srcClsName = SootUtil.getInstance().mthdSig2cls(srcMthdSig);// 源方法的类名
//            String tgtClsName = SootUtil.getInstance().mthdSig2cls(tgtMthdSig);
//            if (conflictJarClses.contains(srcClsName)
//                    && conflictJarClses.contains(tgtClsName)) {
//                // filter relation inside conflictJar 过滤掉conflictJar中的类
//            } else {
//                if (!name2node.containsKey(srcMthdSig)) {
//                    name2node.put(srcMthdSig, new Node4distance(srcMthdSig,
//                            (entryClses.contains(srcClsName) && !LibCopyInfo.isLibCopy(MavenUtil.getInstance().getProjectCor(), srcClsName)),
//                            riskMthds.contains(srcMthdSig), getBranchNum(mthd2branch, srcMthdSig)));
//                }
//                if (!name2node.containsKey(tgtMthdSig)) {
//                    name2node.put(tgtMthdSig, new Node4distance(tgtMthdSig,
//                            (entryClses.contains(tgtClsName) && !LibCopyInfo.isLibCopy(MavenUtil.getInstance().getProjectCor(), tgtClsName)),
//                            riskMthds.contains(tgtMthdSig), getBranchNum(mthd2branch, tgtMthdSig)));
//                }
//                mthdRlts.add(new MethodCall(srcMthdSig, tgtMthdSig));
//            }
//        }
//        System.out.println("cycle time " + (System.currentTimeMillis() - cgOpTime));
//        System.out.println("==============================================");
//        System.out.println("cls2sig time " + sig2ClsTime);
//        System.out.println("==============================================");
//        System.out.println(name2node.size() + mthdRlts.size());
//        long timeStamp4 = System.currentTimeMillis();
//        graph = new Graph4distance(name2node, mthdRlts);
//        System.out.println("newGraph4distance time:" + (System.currentTimeMillis() - timeStamp4));
//        System.out.println("==============================================");
//        long timeStamp3 = System.currentTimeMillis();
//        //System.gc();
//        System.out.println("gc time:" + (System.currentTimeMillis() - timeStamp3));
//        MyLogger.i().info("end form graph with cg.");
//        MyLogger.i().info("form graph with cg time:" + (System.currentTimeMillis() - start));
//        return graph;
//    }
//    public IGraph generateGraph4pathWithCg(Set<String> entryClses, Set<String> riskMthds, List<String> at_aplit_edges) {
//        IGraph graph = null;
//        MyLogger.i().info("start form graph...");
//        // get call-graph.
//        Map<String, Node4path> name2node = new HashMap<String, Node4path>();
//        List<MethodCall> mthdRlts = new ArrayList<MethodCall>();
//        Iterator<String> ite = at_aplit_edges.iterator();
//        while (ite.hasNext()) {
//            String at_split_edge = ite.next();
//            String[] src_tgt = at_split_edge.split("@");
//            String srcMthdSig = src_tgt[0];
//            String tgtMthdSig = src_tgt[1];
//            // //TODO1
//            // if("<com.fasterxml.jackson.core.JsonFactory: boolean
//            // requiresPropertyOrdering()>".equals(tgtMthdSig)) {
//            // MyLogger.i().info("srcMthdSig:"+srcMthdSig);
//            // }
//            String srcClsName = SootUtil.getInstance().mthdSig2cls(srcMthdSig);
//            String tgtClsName = SootUtil.getInstance().mthdSig2cls(tgtMthdSig);
//            if (SootUtil.getInstance().isJavaLibraryClass(srcClsName) || SootUtil.getInstance().isJavaLibraryClass(tgtClsName)) {
//                // filter relation contains javaLibClass
//            } else {
//                if (!name2node.containsKey(srcMthdSig)) {
//                    name2node.put(srcMthdSig, new Node4path(srcMthdSig,
//                            (entryClses.contains(srcClsName) && !LibCopyInfo.isLibCopy(MavenUtil.getInstance().getProjectCor(), srcClsName))
//                            ,
//                            riskMthds.contains(srcMthdSig)));
//                }
//                if (!name2node.containsKey(tgtMthdSig)) {
//                    name2node.put(tgtMthdSig, new Node4path(tgtMthdSig,
//                            (entryClses.contains(tgtClsName) && !LibCopyInfo.isLibCopy(MavenUtil.getInstance().getProjectCor(), tgtClsName))
//                            ,
//                            // ? delete is Private Judge
//                            riskMthds.contains(tgtMthdSig)));
//                }
//                mthdRlts.add(new MethodCall(srcMthdSig, tgtMthdSig));
//            }
//        }
//        graph = new Graph4path(name2node, mthdRlts);
//        MyLogger.i().info("end form graph.");
//        return graph;
//    }
//}
