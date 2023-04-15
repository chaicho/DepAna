//package neu.lab.conflict.util.risk;
//
//import neu.lab.conflict.util.MyLogger;
//import neu.lab.conflict.vo.GlobalVar;
//import neu.lab.conflict.container.DepJars;
//import neu.lab.conflict.graph.*;
//import neu.lab.conflict.util.soot.SootJRiskCg;
//import neu.lab.conflict.util.Conf;
//import neu.lab.conflict.util.SootUtil;
//import neu.lab.conflict.vo.DepJar;
//import neu.lab.conflict.vo.GsonEdgeVO;
//import neu.lab.conflict.vo.MethodCall;
//import soot.jimple.toolkits.callgraph.CallGraph;
//
//import java.util.*;
//
///**
// * 对DepJar的进一步封装，便于检测。
// *
// * @author wangchao
// *
// */
//public class DepJarJRisk {
//	private DepJar depJar; // 依赖jar
//	private DepJar usedDepJar; // 正在使用的jar
//	private ConflictJRisk conflictRisk; // 有风险的冲突jar
//	private Set<String> thrownMthds; // 抛弃的方法
//	// private Set<String> rchedMthds;
//	private Graph4distance graph4distance; // 图
//	private Map<String, IBook> books; // book记录用
//	private Set<String> newThrownMthds; //为了提升效率而避免干扰原先代码，郭。
//	private Set<String> refedThrownClasses;
//	private CallGraph cg = null;//Soot 分析得出的调用图
//	private boolean filterBufValid = false;
//	//private boolean
//	private String needDetect = null;
//	/**
//	 * 用于增量式检测，判断是否需要检测
//	 * @return
//	 */
//	public String getNeedDetect() {
//		return needDetect;
//	}
//	/**
//	 * set是否需检测
//	 * @param needDetect
//	 */
//	public void setNeedDetect(String needDetect) {
//		this.needDetect = needDetect;
//	}
//	/**
//	 * 构造函数
//	 * @param depJar jar包
//	 * @param conflictRisk 冲突
//	 */
//	public DepJarJRisk(DepJar depJar, ConflictJRisk conflictRisk) {
//		this.depJar = depJar;
//		this.usedDepJar = conflictRisk.getUsedDepJar();
//		this.conflictRisk = conflictRisk;
//		// calculate thrownMthd
//		// calculate call-graph
//	}
//	/**
//	 * 构造函数
//	 * @param depJar jar包
//	 */
//	public DepJarJRisk(DepJar depJar) {
//		this.depJar = depJar;
//		// calculate thrownMthd
//		// calculate call-graph
//	}
//	/**
//	 * 得到版本
//	 */
//	public String getVersion() {
//		return depJar.getVersion();
//	}
//    /**
//     * 获取正在使用的Jar
//     * @return
//     */
//	public DepJar getUsedDepJar(){
//	    return usedDepJar;
//    }
//	/**
//	 * 过滤缓存是否有效
//	 * @return
//	 */
//	@Deprecated
//    public boolean filterBufValid() {
//		return this.filterBufValid;
//		//return new File(this.getConflictJar().getFilterBufferFilePath()).exists();
//	}
//	/**
//	 *
//	 * @param filterBufValid
//	 */
//	@Deprecated
//	public void setFilterBufValid(boolean filterBufValid) {
//		this.filterBufValid = filterBufValid;
//	}
//	@Deprecated
//	public boolean getFilterBufValid() {
//		return filterBufValid;
//	}
//	/**
//	 * 返回因jar包版本冲突问题而丢失的classes
//	 * @return
//	 */
//	@Deprecated
//	public Set<String> getThrownClasses() {
//		Set<String> thrownClasses = usedDepJar.getRiskClasses(depJar.getAllCls(false));
//		return thrownClasses;
//	}
//	/**
//	 * By Grj, get
//	 * @return
//	 * @author guoruijie
//	 */
//	public ConflictJRisk getConflictRisk() {
//		return this.conflictRisk;
//	}
//	/**
//	 * set
//	 * @param thrownMthds
//	 */
//	public void setNewThrownMthds(Set<String> thrownMthds) {
//		this.newThrownMthds = thrownMthds;
//		if (this.newThrownMthds == null) {
//			this.newThrownMthds = new HashSet<>();
//		}
//	}
//	/**
//	 *
//	 * @param edgeCg
//	 * @return
//	 */
//	@Deprecated
//	public Graph4distance getGraph4distanceWithEdgeCg(List<GsonEdgeVO> edgeCg) {
//		if (graph4distance == null) {
//			//Set<String> thrownmethods = getThrownMthds();
//			Set<String> thrownmethods = newGetThrownMthdsWithoutFilter();
//			if (thrownmethods.size() > 0) {
//				IGraph iGraph = null;
//				iGraph = SootJRiskCg.i().generateGraphWithBuffer(edgeCg, this);
//				if (iGraph != null) {
//					graph4distance = (Graph4distance) iGraph;
//				} else {
//					graph4distance = new Graph4distance(new HashMap<String, Node4distance>(0),
//							new ArrayList<MethodCall>());
//				}
//			} else {
//				graph4distance = new Graph4distance(new HashMap<String, Node4distance>(0), new ArrayList<MethodCall>());
//			}
//		}
//		return graph4distance;
//	}
//	/**
//	 * set，不过滤风险方法，就用该jar和被使用jar的差集中可能被引用到的风险方法集合
//	 */
//	public void setNewThrownMthdsNoFilter() {
//		DepJar usedDepJar = conflictRisk.getUsedDepJar();
//		/**
//		 * 存在这种可能性：usedDepJar为null。假如我们不考虑非compile阶段，
//		 * 因冲突被舍弃的版本为compile阶段，但被保留的版本却是provided阶段，则usedDepJar为null。
//		 */
//		if (usedDepJar == null) {
//			newThrownMthds = new HashSet<>();
//			return;
//		}
//		newThrownMthds = conflictRisk.getUsedDepJar().getRiskMthds(depJar.getAllMthd());
//		//newThrownMthds = depJar.getAllMthd();
//	}
//	/**set All Mthd Risk Methods, for filter. After Filter compute minus set*/
//	public void setNewThrownMthdsNoFilterAllMthd() {
//		/**
//		 * 存在这种可能性：usedDepJar为null。假如我们不考虑非compile阶段，
//		 * 因冲突被舍弃的版本为compile阶段，但被保留的版本却是provided阶段，则usedDepJar为null。
//		 */
//		newThrownMthds = new HashSet<>(depJar.getAllMthd());
//	}
//	/**
//	 * 新获取风险方法函数
//	 * @author guoruijie
//	 * @return
//	 */
//	public Set<String> newGetThrownMthds() {
//		if (newThrownMthds == null) {
//			newThrownMthds = new HashSet<>();
//		}
//		return newThrownMthds;
//	}
//	/**
//	 * 获取该jar和被使用jar方法差集（获取风险方法）
//	 * @return
//	 */
//	public Set<String> newGetThrownMthdsWithoutFilter() {
//		// "<neu.lab.plug.testcase.homemade.host.prob.ProbBottom: void m()>"
//		// if (thrownMthds == null) {
//		// //TODO1
//		// thrownMthds = new HashSet<String>();
//		// thrownMthds.add("<com.fasterxml.jackson.core.JsonFactory: boolean
//		// requiresPropertyOrdering()>");
//		if (newThrownMthds == null) {
//			synchronized (this) {
//				DepJar usedDepJar = conflictRisk.getUsedDepJar();
//				/**
//				 * 存在这种可能性：usedDepJar为null。假如我们不考虑非compile阶段，
//				 * 因冲突被舍弃的版本为compile阶段，但被保留的版本却是provided阶段，则usedDepJar为null。
//				 */
//				if (usedDepJar == null) {
//					newThrownMthds = new HashSet<>();
//					return newThrownMthds;
//				}
//				newThrownMthds = usedDepJar.getRiskMthds(depJar.getAllMthd());
//			}
//		}
//		return newThrownMthds;
//	}
//	/**
//	 * 针对NoClassDefFoundError的检测，找出该jar与被使用jar的差集
//	 * @return
//	 */
//	public Set<String> getRefedThrownClasses() {
//		// "<neu.lab.plug.testcase.homemade.host.prob.ProbBottom: void m()>"
//		// if (thrownMthds == null) {
//		// //TODO1
//		// thrownMthds = new HashSet<String>();
//		// thrownMthds.add("<com.fasterxml.jackson.core.JsonFactory: boolean
//		// requiresPropertyOrdering()>");
//		if (refedThrownClasses == null) {
//			synchronized (this) {
//				DepJar usedDepJar = conflictRisk.getUsedDepJar();
//				/**
//				 * 存在这种可能性：usedDepJar为null。假如我们不考虑非compile阶段，
//				 * 因冲突被舍弃的版本为compile阶段，但被保留的版本却是provided阶段，则usedDepJar为null。
//				 */
//				if (usedDepJar == null) {
//					refedThrownClasses = new HashSet<>();
//					return refedThrownClasses;
//				}
//				refedThrownClasses = usedDepJar.getRiskRefedClasses(depJar.getAllCls(true));
//			}
//		}
//		return refedThrownClasses;
//	}
//	/**
//	 * 用于内核检测
//	 * @param books
//	 * @return
//	 */
//	public Set<String> getMethodBottom4Path(Map<String, IBook> books) {
//		Set<String> bottomMethods = new HashSet<String>();
//		for (IBook book : books.values()) {
//			// MyLogger.i().info("book:"+book.getNodeName());
//			for (IRecord iRecord : book.getRecords()) {
//				Record4path record = (Record4path) iRecord;
//				bottomMethods.add(record.getRiskMthd());
//			}
//		}
//		return bottomMethods;
//	}
//	/**
//	 * modify class paths when not use all jar
//	 * @author Nos
//	 * @return List<String> classpaths
//	 */
//	public Collection<String> getPrcDirPaths() throws Exception {
//		List<String> classpaths = new ArrayList<String>();
////		List<String> oldClassPaths = new ArrayList<String>();
////		Map<Integer, Integer> cousinsMap = new HashMap<>(0);
////		Map<Integer, Integer> distantRelativesMap = new HashMap<>(0);
//		if (GlobalVar.i().useAllJar) {
//			classpaths = depJar.getReplaceCp();
//		} else {
//			MyLogger.i().info("not add all jar to process");
//			try{
//				classpaths.addAll(this.depJar.getJarFilePaths(true));
//				classpaths.addAll(this.depJar.getOnlyFatherJarCps(true));
//			}catch(NullPointerException e){System.err.println("Caught Exception!");
//				classpaths = new ArrayList<String>();
//			}
//		}
//		Map<String, Integer> tempMap;
//		if (!GlobalVar.i().useAllJar) {
//			if (Conf.getInstance().purEditionMap.containsKey(conflictRisk.getConflict().getSig())) {
//				tempMap = Conf.getInstance().purEditionMap.get(conflictRisk.getConflict().getSig());
//			} else {
//				tempMap = new HashMap<>(0);
//			}
//			tempMap.put(this.depJar.getVersion(), classpaths.size());
//			Conf.getInstance().purEditionMap.put(conflictRisk.getConflict().getSig(), tempMap);
//		}
//		classpaths = SootUtil.getInstance().invalidClassPreprocess(classpaths);
//		return classpaths;
//	}
//	/**
//	 * By grj, get Prc Jars
//	 * @return
//	 * @throws Exception
//	 */
//	public Collection<DepJar> getPrcDepJars() throws Exception {
//		List<DepJar> depJars = new ArrayList<>();
//		if (GlobalVar.i().useAllJar) {
//			depJars = depJar.getReplaceJarList();
//		} else {
//			//MyLogger.i().info("not add all jar to process");
//			try {
//				depJars.add(this.depJar);
//				depJars.addAll(this.depJar.getOnlyFatherJars(true));
//			} catch (NullPointerException e) { System.err.println("Caught Exception!");
//				depJars = new ArrayList<DepJar>();
//			}
//		}
//		Map<String, Integer> tempMap;
//		if (!GlobalVar.i().useAllJar) {
//			if (Conf.getInstance().purEditionMap.containsKey(conflictRisk.getConflict().getSig())) {
//				tempMap = Conf.getInstance().purEditionMap.get(conflictRisk.getConflict().getSig());
//			} else {
//				tempMap = new HashMap<>(0);
//			}
//			tempMap.put(this.depJar.getVersion(), depJars.size());
//			Conf.getInstance().purEditionMap.put(conflictRisk.getConflict().getSig(), tempMap);
//		}
//		return depJars;
//	}
//	/**
//	 * 为场景6获取从被舍弃jar到host项目路径上所有jar包
//	 * @return
//	 * @throws Exception
//	 */
//	public Collection<DepJar> getPrcDepJars4Scene6() throws Exception {
//		List<DepJar> depJars = new ArrayList<>();
//		if (GlobalVar.i().useAllJar) {
//			depJars = depJar.getReplaceJarList();
//			depJars.add(usedDepJar);
//		} else {
//			MyLogger.i().info("not add all jar to process");
//			try{
//				depJars.add(this.depJar);
//				depJars.addAll(this.depJar.getOnlyFatherJars4Scene6(true));
//			}catch(NullPointerException e){System.err.println("Caught Exception!");
//				depJars = new ArrayList<>();
//			}
//		}
//		Map<String, Integer> tempMap;
//		if (!GlobalVar.i().useAllJar) {
//			if (Conf.getInstance().purEditionMap.containsKey(conflictRisk.getConflict().getSig())) {
//				tempMap = Conf.getInstance().purEditionMap.get(conflictRisk.getConflict().getSig());
//			} else {
//				tempMap = new HashMap<>(0);
//			}
//			tempMap.put(this.depJar.getVersion(), depJars.size());
//			Conf.getInstance().purEditionMap.put(conflictRisk.getConflict().getSig(), tempMap);
//		}
//		return depJars;
//	}
//	/**
//	 * 为场景6获取从被舍弃jar到host项目路径上所有jar包路径
//	 * @return
//	 * @throws Exception
//	 */
//	public Collection<String> getPrcDirPaths4Scene6() throws Exception {
//		List<String> classpaths = new ArrayList<String>();
//		if (GlobalVar.i().useAllJar) {
//			classpaths = depJar.getReplaceCp();
//			classpaths.addAll(usedDepJar.getJarFilePaths(true));
//		} else {
//			MyLogger.i().info("not add all jar to process");
//			try{
//				classpaths.addAll(this.depJar.getJarFilePaths(true));
//				classpaths.addAll(this.depJar.getOnlyFatherJarCps4Scene6(true));
//			}catch(NullPointerException e){System.err.println("Caught Exception!");
//				classpaths = new ArrayList<String>();
//			}
//		}
//		Map<String, Integer> tempMap;
//		if (!GlobalVar.i().useAllJar) {
//			if (Conf.getInstance().purEditionMap.containsKey(conflictRisk.getConflict().getSig())) {
//				tempMap = Conf.getInstance().purEditionMap.get(conflictRisk.getConflict().getSig());
//			} else {
//				tempMap = new HashMap<>(0);
//			}
//			tempMap.put(this.depJar.getVersion(), classpaths.size());
//			Conf.getInstance().purEditionMap.put(conflictRisk.getConflict().getSig(), tempMap);
//		}
//		classpaths = SootUtil.getInstance().invalidClassPreprocess(classpaths);
//		return classpaths;
//	}
//	/**
//	 * 获取主项目jar包
//	 * @return
//	 */
//	public DepJar getEntryJar() {
//		return DepJars.i().getHostDepJar();
//	}
//	/**
//	 *
//	 * @return
//	 */
//	public DepJar getConflictJar() {
//		return depJar;
//	}
//	/**
//	 * 获取调用图邻接链表
//	 * @return
//	 */
//	public Graph4distance getGraph4distanceChooseCg() {
//		if (graph4distance == null) {
//			//Set<String> thrownmethods = getThrownMthds();
//			Set<String> thrownmethods = newGetThrownMthdsWithoutFilter();
//			MyLogger.i().info("getGraph4distanceChooseCg newGetSize: " + thrownmethods.size());
//			if (thrownmethods.size() > 0) {
//				IGraph iGraph = null;
//				if (GlobalVar.i().sootProcess) {
//					assert false;
////					iGraph = neu.lab.conflict.sootprocess.SootJRiskCg.i().getGraph4distanceChooseCg(this);
//				}
//				else {
//					if (GlobalVar.i().cgFast) {
//						iGraph = SootJRiskCg.i().getGraph4distanceChooseCgFast(this);
//					}
//					else {
//						iGraph = SootJRiskCg.i().getGraph4distanceChooseCg(this);
//					}
//				}
//				if (iGraph != null) {
//					graph4distance = (Graph4distance) iGraph;
//				} else {
//					graph4distance = new Graph4distance(new HashMap<String, Node4distance>(0),
//							new ArrayList<MethodCall>());
//				}
//			} else {
//				graph4distance = new Graph4distance(new HashMap<String, Node4distance>(0), new ArrayList<MethodCall>());
//			}
//		}
//		return graph4distance;
//	}
//	/**
//	 * 获取调用图邻接链表Graph4distance
//	 * @return
//	 */
//	public Graph4distance getGraph4distance4Scene6ChooseCg() {
//		if (graph4distance == null) {
//			//Set<String> thrownmethods = getThrownMthds();
//			Set<String> thrownmethods = newGetThrownMthdsWithoutFilter();
//			if (thrownmethods.size() > 0) {
//				IGraph iGraph = null;
//				if (GlobalVar.i().sootProcess) {
//					assert false;
////					iGraph = neu.lab.conflict.sootprocess.SootJRiskCg.i().getGraph4distance4Scene6ChooseCg(this);
//				}
//				else {
//					if (!GlobalVar.i().cgFast) {
//						iGraph = SootJRiskCg.i().getGraph4distance4Scene6ChooseCg(this);
//					} else {
//						iGraph = SootJRiskCg.i().getGraph4distanceChooseCgFast4Scene6(this);
//					}
//				}
//				if (iGraph != null) {
//					graph4distance = (Graph4distance) iGraph;
//				} else {
//					graph4distance = new Graph4distance(new HashMap<String, Node4distance>(0),
//							new ArrayList<MethodCall>());
//				}
//			} else {
//				graph4distance = new Graph4distance(new HashMap<String, Node4distance>(0), new ArrayList<MethodCall>());
//			}
//		}
//		return graph4distance;
//	}
//	/**
//	 * toString
//	 * @return
//	 */
//	@Override
//	public String toString() {
//		return depJar.toString() + " in conflict " + conflictRisk.getConflict().toString();
//	}
//	/**
//	 * set
//	 * @return
//	 */
//	public CallGraph getCg() {
//		return cg;
//	}
//	/**
//	 * set
//	 * @param cg
//	 */
//	public void setCg(CallGraph cg) {
//		this.cg = cg;
//	}
//	/**
//	 * get
//	 * @return
//	 */
//	public DepJar getDepJar() {
//		return depJar;
//	}
//	/**
//	 * set
//	 * @param depJar
//	 */
//	public void setDepJar(DepJar depJar) {
//		this.depJar = depJar;
//	}
//	/**
//	 * set
//	 * @param usedDepJar
//	 */
//	public void setUsedDepJar(DepJar usedDepJar) {
//		this.usedDepJar = usedDepJar;
//	}
//	/**
//	 * set
//	 * @param conflictRisk
//	 */
//	public void setConflictRisk(ConflictJRisk conflictRisk) {
//		this.conflictRisk = conflictRisk;
//	}
//	/**
//	 * set
//	 * @param thrownMthds
//	 */
//	@Deprecated
//	public void setThrownMthds(Set<String> thrownMthds) {
//		this.thrownMthds = thrownMthds;
//	}
//	/**
//	 *
//	 * set
//	 * @param graph4distance
//	 */
//	public void setGraph4distance(Graph4distance graph4distance) {
//		this.graph4distance = graph4distance;
//	}
//	/**
//	 * getBooks
//	 * @return
//	 */
//	public Map<String, IBook> getBooks() {
//		return books;
//	}
//	/**
//	 * setBooks
//	 * @param books
//	 */
//	public void setBooks(Map<String, IBook> books) {
//		this.books = books;
//	}
//	/**
//	 * get
//	 * @return
//	 */
//	public Set<String> getNewThrownMthds() {
//		return newThrownMthds;
//	}
//	/**
//	 * 获取过滤
//	 * @return
//	 */
//	@Deprecated
//	public boolean isFilterBufValid() {
//		return filterBufValid;
//	}
//}
