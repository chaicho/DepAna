//package neu.lab.conflict.util.soot;
//
//import abandon.neu.lab.conflict.statics.DupClsJarPair;
//import neu.lab.conflict.GlobalVar;
//import neu.lab.conflict.container.AllRefedCls;
//import neu.lab.conflict.container.DepJars;
//import neu.lab.conflict.risk.jar.DepJarJRisk;
//import neu.lab.conflict.soot.tf.AddEntryTransformer;
//import neu.lab.conflict.util.ClassPackageTreeUtil;
//import neu.lab.conflict.util.MavenUtil;
//import neu.lab.conflict.util.MyLogger;
//import neu.lab.conflict.util.OSinfo;
//import neu.lab.conflict.util.SootUtil;
//import neu.lab.conflict.util.risk.DepJarJRisk;
//import neu.lab.conflict.util.soot.tf.AddEntryTransformer;
//import nju.lab.DSchecker.model.DepJar;
//import neu.lab.conflict.vo.GlobalVar;
//import soot.PackManager;
//import soot.Scene;
//import soot.Transform;
//import soot.Transformer;
//import soot.jimple.toolkits.callgraph.CallGraph;
//
//import java.io.File;
//import java.util.*;
//
//public class SootCgAna extends AbstractSootAna {
//    private static SootCgAna instance = new SootCgAna();
//    public enum CGAlgorithm{
//        CHA("cha", 0), SPARK("spark", 1), PADDLE("paddle", 2);
//        private String name;
//        private int index;
//        CGAlgorithm(String name, int i) {
//            this.name = name;
//            this.index = i;
//        }
//        public void setIndex(int index) {
//            this.index = index;
//        }
//        public void setName(String name) {
//            this.name = name;
//        }
//        public int getIndex() {
//            return index;
//        }
//        public String getName() {
//            return name;
//        }
//    }
//    public static SootCgAna i() {
//        return instance;
//    }
//    public CallGraph getCallGraph(Collection<String> jarFilePaths, CGAlgorithm alg) {
//       MyLogger.i().info("use soot to get CallGraph with " +alg.getName() + jarFilePaths);
//        CallGraph graph = null;
//        long start = System.currentTimeMillis();
//        try {
//            SootUtil.getInstance().modifyLogOut();
//            long startTime = System.currentTimeMillis();
//            List<String> args = new ArrayList<>();
//            switch (alg) {
//                case SPARK: {
//                    Transformer addEntryTransformer = new AddEntryTransformer(DepJars.i().getHostDepJar().getAllMthd());
//                    PackManager.v().getPack("wjpp").add(new Transform("wjpp.myTrans", addEntryTransformer));
//                    args = getSparkArgs(jarFilePaths);
//                    break;
//                }
//                case PADDLE: {
//                    Transformer addEntryTransformer = new AddEntryTransformer(DepJars.i().getHostDepJar().getAllMthd());
//                    PackManager.v().getPack("wjpp").add(new Transform("wjpp.myTrans", addEntryTransformer));
//                    args = getPaddleArgs(jarFilePaths);
//                    break;
//                }
//                default: args = getCHAArgs(jarFilePaths);
//                    break;
//            }
//            soot.Main.main(args.toArray(new String[0]));
//            System.out.println("获取调用图时间" + (System.currentTimeMillis() - startTime) + "ms\n");
//            System.out.println("调用路径jar包个数: " + jarFilePaths.size());
//        } catch (Exception e) { System.err.println("Caught Exception!");
//           MyLogger.i().warn("cg error: ", e);
//        }
//        graph = Scene.v().getCallGraph();
//        soot.G.reset();
//        long runtime = (System.currentTimeMillis() - start) / 1000;
//        GlobalVar.i().time2cg += runtime;
//        return graph;
//    }
//    /**
//     * do not add -cp arg into soot
//     * @param jarFilePaths
//     * @param alg
//     * @return
//     */
//    public CallGraph getCallGraphNoCp(Collection<String> jarFilePaths, CGAlgorithm alg) {
//       MyLogger.i().info("use soot to get CallGraph with " + alg.getName() + jarFilePaths);
//        CallGraph graph = null;
//        long start = System.currentTimeMillis();
//        try {
//            SootUtil.getInstance().modifyLogOut();
//            long startTime = System.currentTimeMillis();
//            List<String> args = new ArrayList<>();
//            switch (alg) {
//                case SPARK: {
//                    Transformer addEntryTransformer = new AddEntryTransformer(DepJars.i().getHostDepJar().getAllMthd());
//                    PackManager.v().getPack("wjpp").add(new Transform("wjpp.myTrans", addEntryTransformer));
//                    args = getSparkArgsNoCp(jarFilePaths);
//                    break;
//                }
//                case PADDLE: {
//                    Transformer addEntryTransformer = new AddEntryTransformer(DepJars.i().getHostDepJar().getAllMthd());
//                    PackManager.v().getPack("wjpp").add(new Transform("wjpp.myTrans", addEntryTransformer));
//                    args = getPaddleArgs(jarFilePaths);
//                    break;
//                }
//                default: args = getCHAArgsNoCp(jarFilePaths);
//                    break;
//            }
//            soot.Main.main(args.toArray(new String[0]));
//            System.out.println("获取调用图时间" + (System.currentTimeMillis() - startTime) + "ms\n");
//            System.out.println("调用路径jar包个数: " + jarFilePaths.size());
//        } catch (Exception e) { System.err.println("Caught Exception!");
//           MyLogger.i().warn("cg error: ", e);
//        }
//        graph = Scene.v().getCallGraph();
//        soot.G.reset();
//        long runtime = (System.currentTimeMillis() - start) / 1000;
//        GlobalVar.i().time2cg += runtime;
//        return graph;
//    }
//    /**
//     * do not add -cp arg into soot
//     * @param jarFilePaths
//     * @param algStr
//     * @return
//     */
//    public CallGraph getCallGraphNoCp(Collection<String> jarFilePaths, String algStr) {
//       MyLogger.i().info("use soot to get CallGraph with " + algStr + jarFilePaths);
//        jarFilePaths = SootUtil.getInstance().invalidClassPreprocess(jarFilePaths);
//        CallGraph graph = null;
//        long start = System.currentTimeMillis();
//        try {
//            SootUtil.getInstance().modifyLogOut();
//            long startTime = System.currentTimeMillis();
//            List<String> args = new ArrayList<>();
//            switch (algStr) {
//                case "spark": {
//                    Transformer addEntryTransformer = new AddEntryTransformer(DepJars.i().getHostDepJar().getAllMthd());
//                    PackManager.v().getPack("wjpp").add(new Transform("wjpp.myTrans", addEntryTransformer));
//                    args = getSparkArgsNoCp(jarFilePaths);
//                    break;
//                }
//                case "paddle": {
//                    Transformer addEntryTransformer = new AddEntryTransformer(DepJars.i().getHostDepJar().getAllMthd());
//                    PackManager.v().getPack("wjpp").add(new Transform("wjpp.myTrans", addEntryTransformer));
//                    args = getPaddleArgs(jarFilePaths);
//                    break;
//                }
//                default: args = getCHAArgsNoCp(jarFilePaths);
//                    break;
//            }
//            soot.Main.main(args.toArray(new String[0]));
//            System.out.println("获取调用图时间" + (System.currentTimeMillis() - startTime) + "ms\n");
//            System.out.println("调用路径jar包个数: " + jarFilePaths.size());
//        } catch (Exception e) { System.err.println("Caught Exception!");
//           MyLogger.i().warn("cg error: ", e);
//        }
//        graph = Scene.v().getCallGraph();
//        soot.G.reset();
//        long runtime = (System.currentTimeMillis() - start) / 1000;
//        GlobalVar.i().time2cg += runtime;
//        return graph;
//    }
//    public List<String> getExcludeList(List<DepJar> depJarList) {
//        List<String> excludePkgs = new ArrayList<>();
//        Set<String> allPathClses = new HashSet<>();
//        for (DepJar depJar : depJarList) {
//            allPathClses.addAll(depJar.getAllCls(true));
//        }
//        excludePkgs = new ArrayList<>(ClassPackageTreeUtil.getInstance().getMinimumExcludePkgs(allPathClses, AllRefedCls.iReachable().getRefedClses()));
//        return excludePkgs;
//    }
//    /**
//     * do not add -cp arg into soot
//     * @param depJarJRisk
//     * @param algStr
//     * @return
//     */
//    public CallGraph getCallGraphNoCpExcludeForRisk(DepJarJRisk depJarJRisk, String algStr) {
//       MyLogger.i().info("use soot to get CallGraph with " + algStr + depJarJRisk.getConflictJar().getSig());
//        CallGraph graph = null;
//        long start = System.currentTimeMillis();
//        try {
//            Collection<DepJar> depJarList = depJarJRisk.getPrcDepJars();
//            Collection<String> jarFilePaths = depJarJRisk.getPrcDirPaths();
//            if (jarFilePaths.isEmpty()) {
//                return null;
//            }
//            Collection<String> excludePkgs = new ArrayList<>();
//            if (GlobalVar.i().sootExcludeLittle) {
//                Set<String> allPathClses = new HashSet<>();
//                for (DepJar depJar : depJarList) {
//                    allPathClses.addAll(depJar.getAllCls(true));
//                }
//                excludePkgs = ClassPackageTreeUtil.getInstance().getMinimumExcludePkgs(allPathClses, AllRefedCls.iReachable().getRefedClses());
//            }
//            else {
//                Set<String> riskMthds = depJarJRisk.newGetThrownMthdsWithoutFilter();
//                Set<String> riskClses = new HashSet<>();
//                for (String riskMthd : riskMthds) {
//                    riskClses.add(SootUtil.getInstance().mthdSig2cls(riskMthd));
//                }
//                Collection<String> reverseReachableClses = AllRefedCls.iReachable().getReverseReachableClass(riskClses);
//                //System.out.println("reverseReachableClses: " + reverseReachableClses);
//                Set<String> riskPkgs = new HashSet<>();
//                Set<String> allPathClses = new HashSet<>();
//                for (DepJar depJar : depJarList) {
//                    allPathClses.addAll(depJar.getAllCls(true));
//                }
//                excludePkgs = ClassPackageTreeUtil.getInstance().getMinimumExcludePkgs(allPathClses, reverseReachableClses);
//            }
//            //System.out.println("excludePkgs: " + excludePkgs);
//            SootUtil.getInstance().modifyLogOut();
//            long startTime = System.currentTimeMillis();
//            List<String> args = new ArrayList<>();
//            switch (algStr) {
//                case "spark": {
//                    Transformer addEntryTransformer = new AddEntryTransformer(DepJars.i().getHostDepJar().getAllMthd());
//                    PackManager.v().getPack("wjpp").add(new Transform("wjpp.myTrans", addEntryTransformer));
//                    args = getSparkArgsNoCp(jarFilePaths);
//                    break;
//                }
//                case "paddle": {
//                    Transformer addEntryTransformer = new AddEntryTransformer(DepJars.i().getHostDepJar().getAllMthd());
//                    PackManager.v().getPack("wjpp").add(new Transform("wjpp.myTrans", addEntryTransformer));
//                    args = getPaddleArgs(jarFilePaths);
//                    break;
//                }
//                default: args = getCHAArgsNoCp(jarFilePaths);
//                    break;
//            }
//            addExcludeArgs(args, excludePkgs);
//            if (GlobalVar.i().addJdk) {
//                args.add("-app");
//            }
//            //System.out.println(args);
//            soot.Main.main(args.toArray(new String[0]));
//            System.out.println("获取调用图时间" + (System.currentTimeMillis() - startTime) + "ms\n");
//            System.out.println("调用路径jar包个数: " + jarFilePaths.size());
//        } catch (Exception e) { System.err.println("Caught Exception!");
//           MyLogger.i().warn("cg error: ", e);
//        }
//        graph = Scene.v().getCallGraph();
//        soot.G.reset();
//        long runtime = (System.currentTimeMillis() - start) / 1000;
//        GlobalVar.i().time2cg += runtime;
//        return graph;
//    }
//    /**
//     * do not add -cp arg into soot
//     * @param dupClsJarPair
//     * @param algStr
//     * @return
//     */
//    public CallGraph getCallGraphNoCpExcludeForClsDup(DupClsJarPair dupClsJarPair, Set<String> riskMthds, String algStr) {
//       MyLogger.i().info("use soot to get CallGraph with " + algStr + dupClsJarPair.getSig());
//        CallGraph graph = null;
//        long start = System.currentTimeMillis();
//        try {
//            Collection<DepJar> depJarList = dupClsJarPair.getPrcDepJars();
//            Collection<String> jarFilePaths = dupClsJarPair.getPrcDirPaths();
//            Collection<String> excludePkgs = new ArrayList<>();
//            if (GlobalVar.i().sootExcludeLittle) {
//                Set<String> allPathClses = new HashSet<>();
//                for (DepJar depJar : depJarList) {
//                    allPathClses.addAll(depJar.getAllCls(true));
//                }
//                excludePkgs = ClassPackageTreeUtil.getInstance().getMinimumExcludePkgs(allPathClses, AllRefedCls.iReachable().getRefedClses());
//            }
//            else {
//                Set<String> riskClses = new HashSet<>();
//                for (String riskMthd : riskMthds) {
//                    riskClses.add(SootUtil.getInstance().mthdSig2cls(riskMthd));
//                }
//                Collection<String> reverseReachableClses = AllRefedCls.iReachable().getReverseReachableClass(riskClses);
//                //System.out.println("reverseReachableClses: " + reverseReachableClses);
//                Set<String> riskPkgs = new HashSet<>();
//            /*for (String riskCls : reverseReachableClses) {
//                riskPkgs.add(SootUtil.getInstance().cls2Pkg_K(riskCls,GlobalVar.i().sootExcludePkgK));
//            }*/
//            /*Set<String> allPkgs = new HashSet<>();
//            for (DepJar depJar : depJarList) {
//                for (String cls : depJar.getAllCls(true)) {
//                    allPkgs.add(SootUtil.getInstance().cls2Pkg_K(cls, GlobalVar.i().sootExcludePkgK));
//                }
//            }*/
//                //DoubleArray da =
//                //System.out.println("allPkgs: " + allPkgs);
//           /* List<String> excludePkgs = new ArrayList();
//            for (String allPkg : allPkgs) {
//                if (!riskPkgs.contains(allPkg)) {
//                    excludePkgs.add(allPkg);
//                }
//            }*/
//                Set<String> allPathClses = new HashSet<>();
//                for (DepJar depJar : depJarList) {
//                    allPathClses.addAll(depJar.getAllCls(true));
//                }
//                excludePkgs = ClassPackageTreeUtil.getInstance().getMinimumExcludePkgs(allPathClses, reverseReachableClses);
//            }
//            //System.out.println("excludePkgs: " + excludePkgs);
//            SootUtil.getInstance().modifyLogOut();
//            long startTime = System.currentTimeMillis();
//            List<String> args = new ArrayList<>();
//            switch (algStr) {
//                case "spark": {
//                    Transformer addEntryTransformer = new AddEntryTransformer(DepJars.i().getHostDepJar().getAllMthd());
//                    PackManager.v().getPack("wjpp").add(new Transform("wjpp.myTrans", addEntryTransformer));
//                    args = getSparkArgsNoCp(jarFilePaths);
//                    break;
//                }
//                case "paddle": {
//                    Transformer addEntryTransformer = new AddEntryTransformer(DepJars.i().getHostDepJar().getAllMthd());
//                    PackManager.v().getPack("wjpp").add(new Transform("wjpp.myTrans", addEntryTransformer));
//                    args = getPaddleArgs(jarFilePaths);
//                    break;
//                }
//                default: args = getCHAArgsNoCp(jarFilePaths);
//                    break;
//            }
//            addExcludeArgs(args, excludePkgs);
//            //System.out.println(args);
//            soot.Main.main(args.toArray(new String[0]));
//            System.out.println("获取调用图时间" + (System.currentTimeMillis() - startTime) + "ms\n");
//            System.out.println("调用路径jar包个数: " + jarFilePaths.size());
//        } catch (Exception e) { System.err.println("Caught Exception!");
//           MyLogger.i().warn("cg error: ", e);
//        }
//        graph = Scene.v().getCallGraph();
//        soot.G.reset();
//        long runtime = (System.currentTimeMillis() - start) / 1000;
//        GlobalVar.i().time2cg += runtime;
//        return graph;
//    }
//    /**
//     * do not add -cp arg into soot
//     * @param jarFilePaths
//     * @param algStr
//     * @return
//     */
//    public CallGraph getCallGraphNoCpForTest(Collection<String> jarFilePaths, Collection<String> entryMethods, String algStr) {
//        CallGraph graph = null;
//        long start = System.currentTimeMillis();
//        try {
//            ///SootUtil.getInstance().modifyLogOut();
//            long startTime = System.currentTimeMillis();
//            List<String> args = new ArrayList<>();
//            switch (algStr) {
//                case "spark": {
//                    Transformer addEntryTransformer = new AddEntryTransformer(entryMethods);
//                    PackManager.v().getPack("wjpp").add(new Transform("wjpp.myTrans", addEntryTransformer));
//                    args = getSparkArgsNoCp(jarFilePaths);
//                    break;
//                }
//                case "paddle": {
//                    Transformer addEntryTransformer = new AddEntryTransformer(entryMethods);
//                    PackManager.v().getPack("wjpp").add(new Transform("wjpp.myTrans", addEntryTransformer));
//                    args = getPaddleArgs(jarFilePaths);
//                    break;
//                }
//                default: args = getCHAArgsNoCp(jarFilePaths);
//                    break;
//            }
//            soot.Main.main(args.toArray(new String[0]));
//            System.out.println("获取调用图时间" + (System.currentTimeMillis() - startTime) + "ms\n");
//            System.out.println("调用路径jar包个数: " + jarFilePaths.size());
//        } catch (Exception e) { System.err.println("Caught Exception!");
//            //MavenUtil.getInstance().getLog().warn("cg error: ", e);
//            e.printStackTrace();
//        }
//        graph = Scene.v().getCallGraph();
//        soot.G.reset();
//        long runtime = (System.currentTimeMillis() - start) / 1000;
//        GlobalVar.i().time2cg += runtime;
//        return graph;
//    }
//    public CallGraph getCallGraphForRisk(DepJar riskJar, Collection<String> jarFilePaths, String cgAlg) {
//       MyLogger.i().info("use soot to get CallGraph with " + cgAlg + jarFilePaths);
//        CallGraph graph = null;
//        long start = System.currentTimeMillis();
//        try {
//            SootUtil.getInstance().modifyLogOut();
//            long startTime = System.currentTimeMillis();
//            List<String> args = new ArrayList<>();
//            switch (cgAlg) {
//                case "spark": {
//                    Transformer addEntryTransformer = new AddEntryTransformer(DepJars.i().getHostDepJar().getAllMthd());
//                    PackManager.v().getPack("wjpp").add(new Transform("wjpp.myTrans", addEntryTransformer));
//                    if (GlobalVar.i().useCp) {
//                        args = getSparkArgs(jarFilePaths, DepJars.i().getUsedJarPathsSeqForRisk(riskJar));
//                    } else {
//                        args = getSparkArgsNoCp(jarFilePaths);
//                    }
//                    break;
//                }
//                case "paddle": {
//                    Transformer addEntryTransformer = new AddEntryTransformer(DepJars.i().getHostDepJar().getAllMthd());
//                    PackManager.v().getPack("wjpp").add(new Transform("wjpp.myTrans", addEntryTransformer));
//                    args = getPaddleArgs(jarFilePaths);
//                    // TODO modify this.
//                    break;
//                }
//                default:
//                    if(GlobalVar.i().useCp) {
//                        args = getCHAArgsNoCp(jarFilePaths);
//                    } else {
//                        args = getCHAArgsNoCp(jarFilePaths);
//                    }
//                    break;
//            }
//            soot.Main.main(args.toArray(new String[0]));
//            System.out.println("获取调用图时间" + (System.currentTimeMillis() - startTime) + "ms\n");
//            System.out.println("调用路径jar包个数: " + jarFilePaths.size());
//        } catch (Exception e) { System.err.println("Caught Exception!");
//           MyLogger.i().warn("cg error: ", e);
//        }
//        graph = Scene.v().getCallGraph();
//        soot.G.reset();
//        long runtime = (System.currentTimeMillis() - start) / 1000;
//        GlobalVar.i().time2cg += runtime;
//        return graph;
//    }
//    public CallGraph getCallGraphForClsDup(DupClsJarPair riskJar, Collection<String> jarFilePaths, CGAlgorithm alg) {
//       MyLogger.i().info("use soot to get CallGraph with " + alg.getName() + jarFilePaths);
//        CallGraph graph = null;
//        long start = System.currentTimeMillis();
//        try {
//            SootUtil.getInstance().modifyLogOut();
//            long startTime = System.currentTimeMillis();
//            List<String> args = new ArrayList<>();
//            switch (alg) {
//                case SPARK: {
//                    Transformer addEntryTransformer = new AddEntryTransformer(DepJars.i().getHostDepJar().getAllMthd());
//                    PackManager.v().getPack("wjpp").add(new Transform("wjpp.myTrans", addEntryTransformer));
//                    //if (GlobalVar.i().useCp)
//                      //  args = getSparkArgs(jarFilePaths, DepJars.i().getUsedJarPathsSeqForRisk(riskJar));
//                    //else args = getSparkArgsNoCp(jarFilePaths);
//                    break;
//                }
//                case PADDLE: {
//                    Transformer addEntryTransformer = new AddEntryTransformer(DepJars.i().getHostDepJar().getAllMthd());
//                    PackManager.v().getPack("wjpp").add(new Transform("wjpp.myTrans", addEntryTransformer));
//                    args = getPaddleArgs(jarFilePaths);
//                    // TODO modify this.
//                    break;
//                }
//                default:
//                    if(GlobalVar.i().useCp) {
//                        args = getCHAArgsNoCp(jarFilePaths);
//                    } else {
//                        args = getCHAArgsNoCp(jarFilePaths);
//                    }
//                    break;
//            }
//            soot.Main.main(args.toArray(new String[0]));
//            System.out.println("获取调用图时间" + (System.currentTimeMillis() - startTime) + "ms\n");
//            System.out.println("调用路径jar包个数: " + jarFilePaths.size());
//        } catch (Exception e) { System.err.println("Caught Exception!");
//           MyLogger.i().warn("cg error: ", e);
//        }
//        graph = Scene.v().getCallGraph();
//        soot.G.reset();
//        long runtime = (System.currentTimeMillis() - start) / 1000;
//        GlobalVar.i().time2cg += runtime;
//        return graph;
//    }
//    @Override
//    protected List<String> getArgs(String[] jarFilePaths) {
//        return super.getArgs(jarFilePaths);
//    }
//    @Override
//    protected void addGenArg(List<String> argsList) {
//        argsList.add("-ire");
//        //argsList.add("-app");
//        argsList.add("-allow-phantom-refs");
//        //argsList.add("-allow-phantom-elms");
//        argsList.add("-ignore-resolving-levels");
//        argsList.add("-w");
//        argsList.add("-ignore-resolution-errors");
//    }
//    @Override
//    protected void addIgrArgs(List<String> argsList) {
//        argsList.addAll(Arrays.asList(new String[] { "-p", "wjop", "off", }));
//        argsList.addAll(Arrays.asList(new String[] { "-p", "wjap", "off", }));
//        argsList.addAll(Arrays.asList(new String[] { "-p", "jtp", "off", }));
//        argsList.addAll(Arrays.asList(new String[] { "-p", "jop", "off", }));
//        argsList.addAll(Arrays.asList(new String[] { "-p", "jap", "off", }));
//        argsList.addAll(Arrays.asList(new String[] { "-p", "bb", "off", }));
//        argsList.addAll(Arrays.asList(new String[] { "-p", "tag", "off", }));
//        argsList.addAll(Arrays.asList(new String[] { "-f", "n", }));//no output
//    }
//    @Override
//    protected void addCgArgs(List<String> argsList) {
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg", "on", }));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg", "all-reachable:false"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg", "implicit-entry:false"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg.cha", "enabled:false"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg.spark", "enabled:true"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg.spark", "apponly:true"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "wjpp.myTrans", "on"}));
//        // CallGraphPack
//    }
//    protected static void addCHACgArgs(List<String> argsList) {
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg", "on", }));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg", "all-reachable:false"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg", "implicit-entry:false"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg.cha", "enabled:true"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg.cha", "apponly:true"}));
//        // CallGraphPack
//    }
//    protected static void addSparkCgArgs(List<String> argsList) {
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg", "on", }));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg", "all-reachable:false"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg", "implicit-entry:false"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg.cha", "enabled:false"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg.spark", "enabled:true"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg.spark", "apponly:true"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "wjpp.myTrans", "on"}));
//        // CallGraphPack
//    }
//    @Deprecated
//    protected static void addPaddleCgArgs(List<String> argsList) {
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg", "on", }));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg", "all-reachable:false"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg", "implicit-entry:false"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg.cha", "enabled:false"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg.paddle", "enabled:true"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "cg.paddle", "apponly:true"}));
//        argsList.addAll(Arrays.asList(new String[] {"-p", "wjpp.myTrans", "on"}));
//    }
//    protected List<String> getCHAArgs(Collection<String> jarFilePaths) {
//        List<String> argsList = new ArrayList<String>();
//        addGenArg(argsList);
//        addIgrArgs(argsList);
//        addCHACgArgs(argsList);
//        addClassPath(argsList, jarFilePaths);
//        if (GlobalVar.i().useCp) {
//            addClasspaths(argsList, DepJars.i().getUsedJarPaths());
//        }
//        if(argsList.size()==0) {//this class can't analysis
//            return argsList;
//        }
//        return argsList;
//    }
//    protected List<String> getCHAArgsNoCp(Collection<String> jarFilePaths) {
//        List<String> argsList = new ArrayList<String>();
//        addGenArg(argsList);
//        addIgrArgs(argsList);
//        addCHACgArgs(argsList);
//        addClassPath(argsList, jarFilePaths);
//        if(argsList.size()==0) {//this class can't analysis
//            return argsList;
//        }
//        return argsList;
//    }
//    protected List<String> getSparkArgs(Collection<String> jarFilePaths) {
//        List<String> argsList = new ArrayList<String>();
//        addGenArg(argsList);
//        addIgrArgs(argsList);
//        addSparkCgArgs(argsList);
//        addClassPath(argsList, jarFilePaths);
//        if (GlobalVar.i().useCp) {
//            addClasspaths(argsList, DepJars.i().getUsedJarPaths());
//        }
//        if(argsList.size()==0) {//this class can't analysis
//            return argsList;
//        }
//        return argsList;
//    }
//    protected List<String> getSparkArgsNoCp(Collection<String> jarFilePaths) {
//        List<String> argsList = new ArrayList<String>();
//        addGenArg(argsList);
//        addIgrArgs(argsList);
//        addSparkCgArgs(argsList);
//        addClassPath(argsList, jarFilePaths);
//        if(argsList.size()==0) {//this class can't analysis
//            return argsList;
//        }
//        return argsList;
//    }
//    protected List<String> getSparkArgs(Collection<String> jarFilePaths, Collection<String> classpaths) {
//        List<String> argsList = new ArrayList<String>();
//        addGenArg(argsList);
//        addIgrArgs(argsList);
//        addSparkCgArgs(argsList);
//        addClassPath(argsList, jarFilePaths);
//        if (GlobalVar.i().useCp) {
//            addClasspaths(argsList, classpaths);
//        }
//        if(argsList.size()==0) {//this class can't analysis
//            return argsList;
//        }
//        return argsList;
//    }
//    protected List<String> getPaddleArgs(Collection<String> jarFilePaths) {
//        List<String> argsList = new ArrayList<String>();
//        addGenArg(argsList);
//        addIgrArgs(argsList);
//        addPaddleCgArgs(argsList);
//        addClassPath(argsList, jarFilePaths);
//        if (GlobalVar.i().useCp) {
//            addClasspaths(argsList, DepJars.i().getUsedJarPaths());
//        }
//        if(argsList.size()==0) {//this class can't analysis
//            return argsList;
//        }
//        return argsList;
//    }
//    @Override
//    protected void addClassPath(List<String> argsList, String[] jarFilePaths) {
//        super.addClassPath(argsList, jarFilePaths);
//    }
//    protected void addClassPath(List<String> argsList, Collection<String> jarFilePaths) {
//        for (String jarFilePath : jarFilePaths) {
//            if (new File(jarFilePath).exists()) {
//                if (canAna(jarFilePath)) {
//                    argsList.add("-process-dir");
//                    argsList.add(jarFilePath);
//                }else {
//                   MyLogger.i().warn("add classpath error:can't analysis file " + jarFilePath);
//                }
//            } else {
//               MyLogger.i().warn("add classpath error:doesn't exist file " + jarFilePath);
//            }
//        }
//    }
//    protected void addClasspaths(List<String> argsList, String[] jarFilePaths) {
//        argsList.add("-pp");
//        String cpSplit = OSinfo.getInstance().isWindows() ? ";" : ":";
//        StringBuilder classpath = new StringBuilder();
//        for (String jarFilePath : jarFilePaths) {
//            if (new File(jarFilePath).exists()) {
//                if (canAna(jarFilePath)) {
//                    classpath.append(jarFilePath);
//                    classpath.append(cpSplit);
//                }else {
//                   MyLogger.i().warn("add classpath error:can't analysis file " + jarFilePath);
//                }
//            } else {
//               MyLogger.i().warn("add classpath error:doesn't exist file " + jarFilePath);
//            }
//        }
//        argsList.add("-cp");
//        argsList.add(classpath.toString());
//    }
//    protected void addClasspaths(List<String> argsList, Collection<String> classpaths) {
//        argsList.add("-pp");
//        String cpSplit = OSinfo.getInstance().isWindows() ? ";" : ":";
//        StringBuilder ret = new StringBuilder();
//        for (String classpath : classpaths) {
//            if (new File(classpath).exists()) {
//                if (canAna(classpath)) {
//                    ret.append(classpath);
//                    ret.append(cpSplit);
//                }else {
//                   MyLogger.i().warn("add classpath error:can't analysis file " + classpath);
//                }
//            } else {
//               MyLogger.i().warn("add classpath error:doesn't exist file " + classpath);
//            }
//        }
//        argsList.add("-cp");
//        argsList.add(ret.toString());
//    }
//    protected void addClasspaths(List<String> argsList, Collection<String> classpaths, int maxNum) {
//        argsList.add("-pp");
//        String cpSplit = OSinfo.getInstance().isWindows() ? ";" : ":";
//        StringBuilder ret = new StringBuilder();
//        int i = 0;
//        for (String classpath : classpaths) {
//            if (new File(classpath).exists()) {
//                if (canAna(classpath)) {
//                    i++;
//                    if (i > maxNum) {
//                        break;
//                    }
//                    ret.append(classpath);
//                    ret.append(cpSplit);
//                }else {
//                   MyLogger.i().warn("add classpath error:can't analysis file " + classpath);
//                }
//            } else {
//               MyLogger.i().warn("add classpath error:doesn't exist file " + classpath);
//            }
//        }
//        argsList.add("-cp");
//        argsList.add(ret.toString());
//    }
//}
