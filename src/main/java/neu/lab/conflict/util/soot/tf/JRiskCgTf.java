//package neu.lab.conflict.util.soot.tf;
//
//import abandon.neu.lab.conflict.statics.DupClsJarPair;
//import neu.lab.conflict.container.DepJars;
//import neu.lab.conflict.graph.IGraph;
//import neu.lab.conflict.util.LibCopyInfo;
//import neu.lab.conflict.util.MyLogger;
//import neu.lab.conflict.util.risk.DepJarJRisk;
//import neu.lab.conflict.vo.DepJar;
//import soot.SceneTransformer;
//import soot.jimple.toolkits.callgraph.CHATransformer;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
///**
// * to get call-graph.
// * 得到call-graph
// * @author asus
// *
// */
//public abstract class JRiskCgTf extends SceneTransformer {
//	// private DepJarJRisk depJarJRisk;
//	protected Set<String> entryClses;	//入口类集合
//	protected Set<String> conflictJarClses;		//冲突jar类集合
//	protected Set<String> riskMthds;	//风险方法集合
//	protected Set<String> rchMthds;
//	protected IGraph graph;
//	protected long CHATime = -1;
//	private long startSootBeforeCHATime;
//	public JRiskCgTf(DepJarJRisk depJarJRisk) {
//		super();
//		// this.depJarJRisk = depJarJRisk;
//		//获得入口类。。。
//		entryClses = depJarJRisk.getEntryJar().getAllCls(true);
//		conflictJarClses = depJarJRisk.getConflictJar().getAllCls(true);
//		riskMthds = depJarJRisk.newGetThrownMthdsWithoutFilter();
//		rchMthds = new HashSet<String>();
//	}
//	/**
//	 * 重构函数
//	 * @param depJarJRisk
//	 * @param thrownMethods
//	 */
//	public JRiskCgTf(DepJarJRisk depJarJRisk, Set<String> thrownMethods) {
//		super();
//		// this.depJarJRisk = depJarJRisk;
//		entryClses = depJarJRisk.getEntryJar().getAllCls(true);
//		conflictJarClses = depJarJRisk.getConflictJar().getAllCls(true);
//		riskMthds = thrownMethods;
//		rchMthds = new HashSet<String>();
//	}
//	public JRiskCgTf(DupClsJarPair dupClsJarPair, Set<String> thrownMethods) {
//		super();
//		// this.depJarJRisk = depJarJRisk;
//		entryClses = DepJars.i().getHostDepJar().getAllCls(true);
//		conflictJarClses = new HashSet<>();
//		riskMthds = thrownMethods;
//		rchMthds = new HashSet<String>();
//	}
//	public JRiskCgTf(DepJar depJar) {
//		super();
//		entryClses = DepJars.i().getHostDepJar().getAllCls(true);
//		riskMthds = depJar.getUsedMthds();
//		MyLogger.i().info("method size : " + riskMthds.size());
//		conflictJarClses = depJar.getAllCls(true);
//		rchMthds = new HashSet<String>();
//	}
//	public JRiskCgTf(DepJar depJar, Set<String> thrownMethods) {
//		super();
//		// this.depJarJRisk = depJarJRisk;
//		entryClses = DepJars.i().getHostDepJar().getAllCls(true);
//		conflictJarClses = depJar.getAllCls(true);
//		riskMthds = thrownMethods;
//		rchMthds = new HashSet<String>();
//	}
//	public JRiskCgTf() {
//		super();
//	}
//	@Override
//	protected void internalTransform(String arg0, Map<String, String> arg1) {
//		startSootBeforeCHATime = System.currentTimeMillis();
//		MyLogger.i().info("JRiskCgTf start..");
//		Map<String, String> cgMap = new HashMap<String, String>();
//		cgMap.put("enabled", "true");
//		cgMap.put("apponly", "true");
//		cgMap.put("all-reachable", "true");
//		// // set entry
//		// List<SootMethod> entryMthds = new ArrayList<SootMethod>();
//		//		List<MethodSource> mthds = new ArrayList<MethodSource>();
//		//		for (SootClass sootClass : Scene.v().getApplicationClasses()) {
//		//			if (entryClses.contains(sootClass.getName())) {// entry class
//		//				for (SootMethod method : sootClass.getMethods()) {
//		//					mthds.add(method.getSource());
//		//					// entryMthds.add(method);
//		//				}
//		//			}
//		//		}
//		// Scene.v().setEntryPoints(entryMthds);
//		initMthd2branch();
//		long startTime = System.currentTimeMillis();
//		CHATransformer.v().transform("wjtp", cgMap);
//		CHATime = System.currentTimeMillis() - startTime;
//		System.out.println("CHATransformer execute Time:" + CHATime + "ms");
//		//System.out.println(Scene.v().getCallGraph());
//		formGraph();
//		MyLogger.i().info("JRiskCgTf end..");
//	}
//	protected abstract void initMthd2branch();
//	protected abstract void formGraph();
//	protected boolean isHostClass(String clsName) {
////		TODO
////		return entryClses.contains(clsName) && !LibCopyInfo.isLibCopy(MavenUtil.getInstance().getProjectCor(), clsName);
//		return false;
//	}
//	public IGraph getGraph() {
//		return graph;
//	}
//	public long getCHATime() {
//		return CHATime;
//	}
//	public long getStartSootBeforeCHATime() {
//		return startSootBeforeCHATime;
//	}
//}
