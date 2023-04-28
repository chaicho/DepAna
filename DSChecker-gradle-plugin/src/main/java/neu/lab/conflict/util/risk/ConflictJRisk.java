//package neu.lab.conflict.util.risk;
//import neu.lab.conflict.vo.GlobalVar;
//import neu.lab.conflict.container.DepJars;
//import neu.lab.conflict.graph.*;
//import neu.lab.conflict.graph.Dog.Strategy;
//import neu.lab.conflict.util.*;
//import neu.lab.conflict.vo.Conflict;
//import nju.lab.DSchecker.model.DepJar;
//import java.io.File;
//import java.util.*;
///**
// * 对Jar包版本冲突描述数据结构Conflict的进一步封装，方便检测
// * @author wangchao
// *
// */
//public class ConflictJRisk {
//	private Conflict conflict;	//conflict
//	private List<DepJarJRisk> jarRisks;	//conflict jar set
//	private Set<DepJar> realRiskJars;
//	private ArrayList<Record4path> records4path;
//	/**
//	 * 构造函数
//	 * 将conflict对象中的jar
//	 */
//	public ConflictJRisk(Conflict conflict) {
//		this.conflict = conflict;
//		jarRisks = new ArrayList<DepJarJRisk>();
//		for (DepJar jar : conflict.getDepJars()) {
//			jarRisks.add(new DepJarJRisk(jar, this));
//		}
//		realRiskJars = new HashSet<DepJar>();
//	}
//	/**
//	 * The new constructor of ConflictJRisk to detect new risk
//	 * @author yzsjy
//	 * @param conflict
//	 * @param depJars
//	 */
//	public ConflictJRisk(Conflict conflict, Set<DepJar> depJars) {
//		this.conflict = conflict;
//		jarRisks = new ArrayList<DepJarJRisk>();
//		for (DepJar jar : conflict.getDepJars(depJars)) {
//			jarRisks.add(new DepJarJRisk(jar, this));
//		}
//	}
//	/**
//	 * 获取这个jar包版本冲突中被加载的版本Jar包
//	 * @return
//	 */
//	public DepJar getUsedDepJar() {
//		return conflict.getUsedDepJar();
//	}
//	/**
//	 * 获取封装前的Conflict类型conflict
//	 * @return
//	 */
//	public Conflict getConflict() {
//		return conflict;
//	}
//	/**
//	 * 获取所有因版本冲突问题而被丢弃的jar包
//	 * @return
//	 */
//	public List<DepJarJRisk> getJarRisks() {
//		return jarRisks;
//	}
//	/**
//	 * 获取因jar包版本冲突问题而舍弃的jar包中所有相对于被加载版本独有的class
//	 * @author guoruijie
//	 * @return
//	 */
//	public Map<String, DepJar> getNotLoadedUsedClasses() {
//		MyLogger.i().info("getNotLoadedUsedClasses");
//		long startTime = 0;
//		//Set<String> usedRiskClasses = new HashSet<>();
//		Map<String, DepJar> usedRiskClasses2DepJar = new HashMap<>();
//		for (DepJarJRisk depJarJRisk : jarRisks) {
//			//usedRiskClasses.addAll(depJarJRisk.getUsedDepJar().getRiskRefedClasses(depJarJRisk.getConflictJar().getAllCls(true)));
//			if (depJarJRisk.getUsedDepJar() == null) {
//				return usedRiskClasses2DepJar;
//			}
//			Set<String> tmpUsedRiskClasses = depJarJRisk.getRefedThrownClasses();
//			for (String tmpUsedRiskClass : tmpUsedRiskClasses) {
//				usedRiskClasses2DepJar.put(tmpUsedRiskClass, depJarJRisk.getConflictJar());
//			}
//		}
//		return usedRiskClasses2DepJar;
//	}
//	/**
//	 * directly compute call graph without Filter。全量检测执行
//	 * @return
//	 */
//	public Set<String> getConflictLevelDirectly() {
//		long startTime = 0;
//		Set<String> usedRiskMethods = new HashSet<String>(); // 被使用的usedDepJar风险方法集合
//		System.out.println(getConflict().getGroupId() + getConflict().getArtifactId() + "jar包版本个数为:" + jarRisks.size());
//		for (DepJarJRisk depJarJRisk : jarRisks) {
//			startTime = System.currentTimeMillis();
//			System.out.println(depJarJRisk.getConflictJar().getSig() + "filter Risk Methods2执行时间:" + (System.currentTimeMillis() - startTime) + "ms\n");
//			if (depJarJRisk.getConflictJar().equals(depJarJRisk.getUsedDepJar())) {
//				continue;
//			}
//			// TODO has NullPointer Exception! maybe noCirCleRisk.getUsedDepJar is null
//			if (depJarJRisk.getUsedDepJar() == null) {
//				continue;
//			}
//			if (depJarJRisk.getConflictJar().getPriority() != -1  && depJarJRisk.getConflictJar().getVersion().equals(depJarJRisk.getUsedDepJar().getVersion())) {
//				continue;
//			}
//			if (depJarJRisk.newGetThrownMthdsWithoutFilter().size() <= 0) {
//				continue;// if riskmthds size < 0 continue;
//			}
//			// get path graph
//			startTime = System.currentTimeMillis();
//			Graph4path pathGraph = depJarJRisk.getGraph4distanceChooseCg().getGraph4path();
//			System.out.println(depJarJRisk.getConflictJar().getSig() + "获取Risk Methods调用路径前获取调用图执行时间:" + (System.currentTimeMillis() - startTime) + "ms\n");
//			startTime = System.currentTimeMillis();
//			Set<String> hostNds = pathGraph.getHostNds();
//			Map<String, IBook> books = new Dog(pathGraph).findRlt(hostNds, Conf.getInstance().DOG_DEP_FOR_PATH,
//					Strategy.NOT_RESET_BOOK);
//			System.out.println(depJarJRisk.getConflictJar().getName() + "获取Risk Methods调用路径执行时间:" + (System.currentTimeMillis() - startTime) + "ms\n");
//			MySortedMap<Integer, Record4path> dis2records = getDis2records(books, hostNds);
//			Set<String> bottomMethods = depJarJRisk.getMethodBottom4Path(books);
//			/**
//			 * polished by grj: save records4path to add route info in xml file
//			 */
//			if (this.records4path == null) {
//				this.records4path = new ArrayList<>();
//			}
//			this.records4path.addAll((ArrayList<Record4path>)dis2records.flat());
//			// get real risk methods, the filter will filter some negative risk methods
//			Set<String> isRealRiskMthds = bottomMethods;
//			if (GlobalVar.i().filterLambda) {
//				isRealRiskMthds = filterDisRecords(dis2records, bottomMethods);
//			}
//			usedRiskMethods.addAll(isRealRiskMthds);
//			System.out.println(depJarJRisk.getConflictJar().getName() + "过滤Risk Methods全部信息执行时间:" + (System.currentTimeMillis() - startTime) + "ms\n");
//			if (!isRealRiskMthds.isEmpty()) {
//				for (String isRealRiskMthd : isRealRiskMthds) {
//					GlobalVar.i().riskMethodMap.put(isRealRiskMthd, depJarJRisk.getConflictJar().getSig());
//				}
//				realRiskJars.add(depJarJRisk.getConflictJar());
//				MyLogger.i().info("Risk Jar : " + depJarJRisk.getConflictJar().getSig());
////				getRiskMethodPaths(dis2records, isRealRiskMthds);
//			}
//		}
//		StringBuilder sb = new StringBuilder();
//		if (usedRiskMethods.size() <= 0) {
//			sb.append("未发现被调用的而未加载的方法!");
//		} else {
//			sb.append("分析完毕! 共为" + this.conflict.getGroupId() + ':'
//					+ this.conflict.getArtifactId()
//					+ "找出" + usedRiskMethods.size() + "个被调用而未加载的方法:\n");
//			int i = 1;
//			for (String usedRiskMethod : usedRiskMethods) {
//				sb.append("\t (" + i++ + ") " + usedRiskMethod + '\n');
//			}
//		}
//		MyLogger.i().info(sb.toString());
//		return usedRiskMethods;
//	}
//	/**
//	 * directly compute call graph without filter，增量式检测
//	 * @return
//	 */
//	public Set<String> getConflictLevelDirectlyJarChanged(Collection<String> oldConflictsOrNewJars) {
//		long startTime = 0;
//		Set<String> usedRiskMethods = new HashSet<String>(); // 被使用的usedDepJar风险方法集合
//		String usedDepJarSig = "";
//		if (getUsedDepJar() != null) {
//			usedDepJarSig = getUsedDepJar().getSig();
//		}
//		System.out.println(getConflict().getGroupId() + getConflict().getArtifactId() + "jar包版本个数为:" + jarRisks.size());
//		for (DepJarJRisk depJarJRisk : jarRisks) {
//			if (getUsedDepJar() == null || depJarJRisk.getConflictJar() == null) {
//				continue;
//			}
//			if (depJarJRisk.getConflictJar().equals(getUsedDepJar())) {
//				continue;
//			}
//			if (!GlobalVar.i().specifyOne) {
//				boolean newConclict = true;
//				boolean lastExist = false;
//				for (String lastConflict : oldConflictsOrNewJars) {
//					String[] lastConflictArr = lastConflict.split(",");
//					if (lastConflictArr.length != 3) {
//						lastExist = false;
//						break;
//					}
//					String usedSig = lastConflictArr[0];
//					String conflictJarSig = lastConflictArr[1];
//					if (usedSig.equals(getUsedDepJar().getSig()) && conflictJarSig.equals(depJarJRisk.getConflictJar().getSig())) {
//						newConclict = false;
//						lastExist = true;
//						try {
//							Collection<String> prcDirPaths = depJarJRisk.getPrcDirPaths();
//							List<String> lastPrcDirPaths = Arrays.asList(lastConflictArr[2].split("&"));
//							if (prcDirPaths.size() > lastPrcDirPaths.size()) {
//								lastExist = false;
//								if (GlobalVar.i().testOutput) {
//									FileUtil.getInstance().writeStringToFile(new File(GlobalVar.i().newConflictFilePathJarConflict),
//											usedSig + "," + conflictJarSig + "," + lastConflictArr[2] + "," + PublicUtil.getInstance().list2StrSplitColonQuote(prcDirPaths) + "," + "diffSet" + "\n",
//											true);
//								}
//								break;
//							}
//							for (String prcDirPath : prcDirPaths) {
//								if (!lastPrcDirPaths.contains(prcDirPath)) {
//									lastExist = false;
//									if (GlobalVar.i().testOutput) {
//										FileUtil.getInstance().writeStringToFile(new File(GlobalVar.i().newConflictFilePathJarConflict),
//												usedSig + "," + conflictJarSig + "," + lastConflictArr[2] + "," + PublicUtil.getInstance().list2StrSplitColonQuote(prcDirPaths) + "," + "diffSet" + "\n",
//												true);
//									}
//									break;
//								}
//							}
//						} catch (Exception e) { System.err.println("Caught Exception!");
//							e.printStackTrace();
//						}
//						break;
//					}
//				}
//				if (lastExist) {
//					continue;
//				}
//				try {
//					if (GlobalVar.i().testOutput) {
//						if (newConclict) {
//							if (getUsedDepJar() != null) {
//								FileUtil.getInstance().writeStringToFile(new File(GlobalVar.i().newConflictFilePathJarConflict),
//										usedDepJarSig + "," + depJarJRisk.getConflictJar().getSig() + "," + "newConflict" + "\n", true);
//							}
//						}
//					}
//				} catch (Exception e) { System.err.println("Caught Exception!");
//					e.printStackTrace();
//				}
//			}
//			/**
//			 *
//			 * cheng's advice. simplify
//			 */
//			else {
//				if (depJarJRisk.getNeedDetect() == null) {
//					DepJar jar1 = getUsedDepJar();
//					DepJar jar2 = depJarJRisk.getConflictJar();
//					if (jar1 == null || jar2 == null) {
//						continue;
//					}
//					/**
//					 *
//					 */
//					if (!oldConflictsOrNewJars.contains(jar1.getName()) && !oldConflictsOrNewJars.contains(jar2.getName())) {
//						continue;
//					}
//					if (GlobalVar.i().testOutput) {
//						if (getUsedDepJar() != null) {
//							FileUtil.getInstance().writeStringToFile(new File(GlobalVar.i().newConflictFilePathJarConflict),
//									usedDepJarSig + "," + depJarJRisk.getConflictJar().getSig() + "," + "newConflict" + "\n", true);
//						}
//					}
//				}
//				else if ("false".equals(depJarJRisk.getNeedDetect())) {
//					continue;
//				}
//			}
//			startTime = System.currentTimeMillis();
//			System.out.println(depJarJRisk.getConflictJar().getSig() + "filter Risk Methods2执行时间:" + (System.currentTimeMillis() - startTime) + "ms\n");
//			// get path graph
//			startTime = System.currentTimeMillis();
//			Graph4path pathGraph = depJarJRisk.getGraph4distanceChooseCg().getGraph4path();
//			System.out.println(depJarJRisk.getConflictJar().getSig() + "获取Risk Methods调用路径前获取调用图执行时间:" + (System.currentTimeMillis() - startTime) + "ms\n");
//			startTime = System.currentTimeMillis();
//			Set<String> hostNds = pathGraph.getHostNds();
//			Map<String, IBook> books = new Dog(pathGraph).findRlt(hostNds, Conf.getInstance().DOG_DEP_FOR_PATH,
//					Strategy.NOT_RESET_BOOK);
//			System.out.println(depJarJRisk.getConflictJar().getName() + "获取Risk Methods调用路径执行时间:" + (System.currentTimeMillis() - startTime) + "ms\n");
//			MySortedMap<Integer, Record4path> dis2records = getDis2records(books, hostNds);
//			Set<String> bottomMethods = depJarJRisk.getMethodBottom4Path(books);
//			/**
//			 * polished by grj: save records4path to add route info in xml file
//			 */
//			if (this.records4path == null) {
//				this.records4path = new ArrayList<>();
//			}
//			this.records4path.addAll((ArrayList<Record4path>)dis2records.flat());
//			// get real risk methods, the filter will filter some negative risk methods
//			Set<String> isRealRiskMthds = bottomMethods;
//			if (GlobalVar.i().filterLambda) {
//				isRealRiskMthds = filterDisRecords(dis2records, bottomMethods);
//			}
//			usedRiskMethods.addAll(isRealRiskMthds);
//			System.out.println(depJarJRisk.getConflictJar().getName() + "过滤Risk Methods全部信息执行时间:" + (System.currentTimeMillis() - startTime) + "ms\n");
//			if (!isRealRiskMthds.isEmpty()) {
//				for (String isRealRiskMthd : isRealRiskMthds) {
//					GlobalVar.i().riskMethodMap.put(isRealRiskMthd, depJarJRisk.getConflictJar().getSig());
//				}
//				realRiskJars.add(depJarJRisk.getConflictJar());
//				MyLogger.i().info("Risk Jar : " + depJarJRisk.getConflictJar().getSig());
////				getRiskMethodPaths(dis2records, isRealRiskMthds);
//			}
//		}
//		StringBuilder sb = new StringBuilder();
//		if (usedRiskMethods.size() <= 0) {
//			sb.append("未发现被调用的而未加载的方法!");
//		} else {
//			sb.append("分析完毕! 共为" + this.conflict.getGroupId() + ':'
//					+ this.conflict.getArtifactId()
//					+ "找出" + usedRiskMethods.size() + "个被调用而未加载的方法:\n");
//			int i = 1;
//			for (String usedRiskMethod : usedRiskMethods) {
//				sb.append("\t (" + i++ + ") " + usedRiskMethod + '\n');
//			}
//		}
//		MyLogger.i().info(sb.toString());
//		return usedRiskMethods;
//	}
//
//
//	/**
//	 * 为场景6进行检测（jar包版本冲突问题）全量检测
//	 * @author guoruijie
//	 * @return
//	 */
//	public Set<String> getConflictLevel4Scene6Directly() {
//		long startTime = 0;
//		Set<String> usedRiskMethods = new HashSet<String>(); // 被使用的usedDepJar风险方法集合
//		System.out.println(getConflict().getGroupId() + getConflict().getArtifactId() + "jar包版本个数为:" + jarRisks.size());
//		for (DepJarJRisk depJarJRisk : jarRisks) {
//			if (depJarJRisk.getConflictJar().equals(depJarJRisk.getUsedDepJar())) {
//				continue;
//			}
//			if (depJarJRisk.newGetThrownMthdsWithoutFilter().size() <= 0) {
//				continue;// if riskmthds size < 0 continue;
//			}
//			startTime = System.currentTimeMillis();
//			System.out.println(depJarJRisk.getConflictJar().getName() + "filter Risk Methods2执行时间:" + (System.currentTimeMillis() - startTime) + "ms\n");
//			// get path graph
//			startTime = System.currentTimeMillis();
//			Graph4path pathGraph = depJarJRisk.getGraph4distance4Scene6ChooseCg().getGraph4path();
//			System.out.println(depJarJRisk.getConflictJar().getName() + "获取Risk Methods调用路径前获取调用图执行时间:" + (System.currentTimeMillis() - startTime) + "ms\n");
//			startTime = System.currentTimeMillis();
//			Set<String> hostNds = pathGraph.getHostNds();
//			Map<String, IBook> books = new Dog(pathGraph).findRlt(hostNds, Conf.getInstance().DOG_DEP_FOR_PATH,
//					Strategy.NOT_RESET_BOOK);
//			System.out.println(depJarJRisk.getConflictJar().getName() + "获取Risk Methods调用路径执行时间:" + (System.currentTimeMillis() - startTime) + "ms\n");
//			MySortedMap<Integer, Record4path> dis2records = getDis2records(books, hostNds);
//			Set<String> bottomMethods = depJarJRisk.getMethodBottom4Path(books);
//			/**
//			 * polished by grj: save records4path to add route info in xml file
//			 */
//			if (this.records4path == null) {
//				this.records4path = new ArrayList<>();
//			}
//			this.records4path.addAll((ArrayList<Record4path>)dis2records.flat());
//			// get real risk methods, the filter will filter some negative risk methods
//			Set<String> isRealRiskMthds = bottomMethods;
//			if (GlobalVar.i().filterLambda) {
//				isRealRiskMthds = filterDisRecords(dis2records, bottomMethods);
//			}
//			usedRiskMethods.addAll(isRealRiskMthds);
//			System.out.println(depJarJRisk.getConflictJar().getName() + "过滤Risk Methods全部信息执行时间:" + (System.currentTimeMillis() - startTime) + "ms\n");
//			if (!isRealRiskMthds.isEmpty()) {
//				for (String isRealRiskMthd : isRealRiskMthds) {
//					GlobalVar.i().riskMethodMap.put(isRealRiskMthd, depJarJRisk.getConflictJar().getSig());
//				}
//				realRiskJars.add(depJarJRisk.getConflictJar());
//				MyLogger.i().info("Risk Jar : " + depJarJRisk.getConflictJar().getSig());
////				getRiskMethodPaths(dis2records, isRealRiskMthds);
//			}
//		}
//		StringBuilder sb = new StringBuilder();
//		if (usedRiskMethods.size() <= 0) {
//			sb.append("未发现被调用的而未加载的方法!");
//		} else {
//			sb.append("分析完毕! 共为" + this.conflict.getGroupId() + ':'
//					+ this.conflict.getArtifactId()
//					+ "找出" + usedRiskMethods.size() + "个被调用而未加载的方法:\n");
//			int i = 1;
//			for (String usedRiskMethod : usedRiskMethods) {
//				sb.append("\t (" + i++ + ") " + usedRiskMethod + '\n');
//			}
//		}
//		MyLogger.i().info(String.valueOf(sb));
//		return usedRiskMethods;
//	}
//	/**
//	 * 为场景6增量式检测进行检测
//	 * @param lastConflicts
//	 * @return
//	 */
//	public Set<String> getConflictLevel4Scene6DirectlyJarChanged(Collection<String> lastConflicts) {
//		long startTime = 0;
//		Set<String> usedRiskMethods = new HashSet<String>(); // 被使用的usedDepJar风险方法集合
//		System.out.println(getConflict().getGroupId() + getConflict().getArtifactId() + "jar包版本个数为:" + jarRisks.size());
//		for (DepJarJRisk depJarJRisk : jarRisks) {
//			if (depJarJRisk.getConflictJar().equals(getUsedDepJar())) {
//				continue;
//			}
//			DepJar jar1 = getUsedDepJar();
//			DepJar jar2 = depJarJRisk.getConflictJar();
//			if (depJarJRisk.getNeedDetect() == null) {
//				if (jar1 == null || jar2 == null) {
//					continue;
//				}
//				/**
//				 *
//				 */
//				if (!lastConflicts.contains(jar1.getName()) && !lastConflicts.contains(jar2.getName())) {
//					continue;
//				}
//				if (GlobalVar.i().testOutput) {
//					if (getUsedDepJar() != null) {
//						FileUtil.getInstance().writeStringToFile(new File(GlobalVar.i().newConflictFilePathJarConflict),
//								jar1.getSig() + "," + depJarJRisk.getConflictJar().getSig() + "," + "newConflict" + "\n", true);
//					}
//				}
//			}
//			else if ("false".equals(depJarJRisk.getNeedDetect())) {
//				continue;
//			}
//			startTime = System.currentTimeMillis();
//			System.out.println(depJarJRisk.getConflictJar().getName() + "filter Risk Methods2执行时间:" + (System.currentTimeMillis() - startTime) + "ms\n");
//			// get path graph
//			startTime = System.currentTimeMillis();
//			Graph4path pathGraph = depJarJRisk.getGraph4distance4Scene6ChooseCg().getGraph4path();
//			System.out.println(depJarJRisk.getConflictJar().getName() + "获取Risk Methods调用路径前获取调用图执行时间:" + (System.currentTimeMillis() - startTime) + "ms\n");
//			startTime = System.currentTimeMillis();
//			Set<String> hostNds = pathGraph.getHostNds();
//			Map<String, IBook> books = new Dog(pathGraph).findRlt(hostNds, Conf.getInstance().DOG_DEP_FOR_PATH,
//					Strategy.NOT_RESET_BOOK);
//			System.out.println(depJarJRisk.getConflictJar().getName() + "获取Risk Methods调用路径执行时间:" + (System.currentTimeMillis() - startTime) + "ms\n");
//			MySortedMap<Integer, Record4path> dis2records = getDis2records(books, hostNds);
//			Set<String> bottomMethods = depJarJRisk.getMethodBottom4Path(books);
//			/**
//			 * polished by grj: save records4path to add route info in xml file
//			 */
//			if (this.records4path == null) {
//				this.records4path = new ArrayList<>();
//			}
//			this.records4path.addAll((ArrayList<Record4path>)dis2records.flat());
//			// get real risk methods, the filter will filter some negative risk methods
//			Set<String> isRealRiskMthds = bottomMethods;
//			if (GlobalVar.i().filterLambda) {
//				isRealRiskMthds = filterDisRecords(dis2records, bottomMethods);
//			}
//			usedRiskMethods.addAll(isRealRiskMthds);
//			System.out.println(depJarJRisk.getConflictJar().getName() + "过滤Risk Methods全部信息执行时间:" + (System.currentTimeMillis() - startTime) + "ms\n");
//			if (!isRealRiskMthds.isEmpty()) {
//				for (String isRealRiskMthd : isRealRiskMthds) {
//					GlobalVar.i().riskMethodMap.put(isRealRiskMthd, depJarJRisk.getConflictJar().getSig());
//				}
//				realRiskJars.add(depJarJRisk.getConflictJar());
//				MyLogger.i().info("Risk Jar : " + depJarJRisk.getConflictJar().getSig());
////				getRiskMethodPaths(dis2records, isRealRiskMthds);
//			}
//		}
//		StringBuilder sb = new StringBuilder();
//		if (usedRiskMethods.size() <= 0) {
//			sb.append("未发现被调用的而未加载的方法!");
//		} else {
//			sb.append("分析完毕! 共为" + this.conflict.getGroupId() + ':'
//					+ this.conflict.getArtifactId()
//					+ "找出" + usedRiskMethods.size() + "个被调用而未加载的方法:\n");
//			int i = 1;
//			for (String usedRiskMethod : usedRiskMethods) {
//				sb.append("\t (" + i++ + ") " + usedRiskMethod + '\n');
//			}
//		}
//		MyLogger.i().info(String.valueOf(sb));
//		return usedRiskMethods;
//	}
//	/**
//	 * get call record
//	 * @param books : call books
//	 * @param hostNds : host nodes
//	 * @return MySortedMap<Integer, Record4path> dis2records
//	 */
//	private MySortedMap<Integer, Record4path> getDis2records(Map<String, IBook> books, Set<String> hostNds) {
//		MySortedMap<Integer, Record4path> dis2records = new MySortedMap<Integer, Record4path>();
//		for (String topMthd : books.keySet()) {
//			if (hostNds.contains(topMthd)) {
//				Book4path book = (Book4path) (books.get(topMthd));
//				for (IRecord iRecord : book.getRecords()) {
//					Record4path record = (Record4path) iRecord;
//					dis2records.add(record.getPathlen(), record);
//				}
//			}
//		}
//		return dis2records;
//	}
//
//
//	/**
//	 * to get call path
//	 * @param mthdCallPath : method call path
//	 * @return string
//	 */
//	private String addJarPath(String mthdCallPath) {
//		StringBuilder sb = new StringBuilder();
//		String[] mthds = mthdCallPath.split("\\n");
//		for (int i = 0; i < mthds.length - 1; i++) {
//			// last method is risk method,don't need calculate.
//			String mthd = mthds[i];
//			String cls = SootUtil.getInstance().mthdSig2cls(mthd);
//			DepJar depJar = DepJars.i().getClassJar(cls);
//			String jarPath = "";
//			if (depJar != null) {
//				jarPath = depJar.getJarFilePaths(true).get(0);
//			}
//			sb.append(mthd + " " + jarPath + "\n");
//		}
//		sb.append(mthds[mthds.length - 1]);
//		return sb.toString();
//	}
//	/**
//	 * If the filtering method contains a method in the current risk method call path, the call path will be filtered out
//	 * 如果过滤方法中包含当前风险方法调用路径中的某一个方法，则过滤掉此调用路径
//	 * @param dis2records
//	 * @param bottomMethods risk methods when not filter
//	 * @return
//	 */
//	public Set<String> filterDisRecords(MySortedMap<Integer, Record4path> dis2records, Set<String> bottomMethods)  {
//		Set<String> isRealRiskMthds = new HashSet<>();
//		for (Record4path record : dis2records.flat()) {
//			String[] mthds = record.getPathStr().split("\\n");
//			String riskMthdDetail = mthds[mthds.length - 1];
//			if (!isRealRiskMthds.contains(riskMthdDetail)) {
//				int i = 0;
//				while (true) {
//					//if (i < mthds.length - 1) {
//					// TODO if need to filter lambda methods?
//					if (i < mthds.length) {
//						String mthd = mthds[i];
//						String sigName = SootUtil.getInstance().mthdSig2nameRemoveTheFirstSpace(mthd);
//						//过滤lembda方法，防止误报
//						if (GlobalVar.i().filterMthds.contains(sigName) || sigName.contains("lambda$")) {
//							break;
//						}
//						i++;
//						continue;
//					}
//					isRealRiskMthds.add(riskMthdDetail);
//					break;
//				}
//			}
//			if (isRealRiskMthds.size() == bottomMethods.size()) {
//				break;
//			}
//		}
//		return isRealRiskMthds;
//	}
//	/**
//	 * By Grj
//	 */
//	public ArrayList<Record4path> getRecords4path() {
//		return records4path;
//	}
//	/**
//	 * equals
//	 * @param o
//	 * @return
//	 */
//	@Override
//	public boolean equals(Object o) {
//		if (this == o) {
//			return true;
//		}
//		if (o == null || getClass() != o.getClass()) {
//			return false;
//		}
//		ConflictJRisk that = (ConflictJRisk) o;
//		return Objects.equals(conflict, that.conflict) && Objects.equals(jarRisks, that.jarRisks) && Objects.equals(realRiskJars, that.realRiskJars) && Objects.equals(records4path, that.records4path);
//	}
//	/**
//	 * hashCode
//	 * @return
//	 */
//	@Override
//	public int hashCode() {
//		return Objects.hash(conflict, jarRisks, realRiskJars, records4path);
//	}
//}
