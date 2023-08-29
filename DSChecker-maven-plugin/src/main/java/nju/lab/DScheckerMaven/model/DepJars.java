package nju.lab.DScheckerMaven.model;


import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IDepJars;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 表征依赖树中所有节点jar包的全局变量
 */
@Slf4j
public class DepJars implements IDepJars<DepJar> {
	private static DepJars instance;
	/**
	 * 返回依赖树中所有结点的jar包
	 * @return
	 */
	public static DepJars i() {
		return instance;
	}
	/**
	 * 用依赖树节点来初始化所有jar包
	 * @param nodeAdapters
	 * @throws Exception
	 */
	public static void init(NodeAdapters nodeAdapters) throws Exception {
		if (instance == null) {
			instance = new DepJars(nodeAdapters);
		}
	}
	private Set<DepJar> container;
	// sequenced container, jar classpath sequence same as the
	private List<DepJar> seqContainer;
	private Set<DepJar> usedDepJars;
	// sequenced container, sequenced used dep jars.
	private List<DepJar> seqUsedDepJars;

	private DepJar hostDepJar;
	private DepJars(NodeAdapters nodeAdapters) throws Exception {
		container = new HashSet<DepJar>();
		seqContainer = new ArrayList<>();
		for (NodeAdapter nodeAdapter : nodeAdapters.getAllNodeAdapter()) {
			DepJar addDepJar = new DepJar(nodeAdapter.getGroupId(), nodeAdapter.getArtifactId(), nodeAdapter.getVersion(),
					nodeAdapter.getClassifier(), nodeAdapter.getPriority(), nodeAdapter.getFilePath());
			if (!container.contains(addDepJar)) {
				container.add(addDepJar);
				seqContainer.add(addDepJar);
			}
		}
	}
	/**
	 * get used dep jars
	 * @return Set<DepJar> usedDepJars
	 * 获得所有使用的依赖的DepJar
	 */
	public Set<DepJar> getUsedDepJars() {
		if (this.usedDepJars == null) {
			Set<DepJar> usedDepJars = new HashSet<DepJar>();
			for (DepJar depJar : container) {
				if (depJar.isSelected()) { //只要该jar没有被manage而且INCLUDED
					usedDepJars.add(depJar); //那就把这个jar加入Set准备返回吧
				}
			}
			this.usedDepJars = usedDepJars;
		}
		return this.usedDepJars;
	}

	@Override
	public DepJar getSelectedDepJarById(String s) {
		for (DepJar depJar : getUsedDepJars()) {
			if (depJar.isSelected() && depJar.getName().equals(s)) {
				return depJar;
			}
		}
		return null;
	}

	/**
	 * 按加载优先级返回所有被加载的jar包
	 * @return
	 */
	public List<DepJar> getSeqUsedDepJars() {
		if (this.seqUsedDepJars == null) {
			List<DepJar> seqUsedDepJars = new ArrayList<>();
			for (DepJar depJar : seqContainer) {
				if (depJar.isSelected()) { //只要该jar没有被manage而且INCLUDED
					seqUsedDepJars.add(depJar); //那就把这个jar加入Set准备返回吧
				}
			}
			this.seqUsedDepJars = seqUsedDepJars;
		}
		return this.seqUsedDepJars;
	}
	/**
	 * get dep jar belonged to hots
	 * @return DepJar hostDepJar
	 */
	public DepJar getHostDepJar() {
		if (hostDepJar == null) {
			for (DepJar depJar : container) {
				if (depJar.isHost()) {
					if (hostDepJar != null) {
						log.warn("multiple depjar for host ");
					}
					hostDepJar = depJar;
				}
			}
			log.warn("depjar host is " + hostDepJar.toString()); //测试输出
		}
		return hostDepJar;
	}
	/**
	 * use groupId, artifactId, version and classifier to find the same DepJar
	 * @return same depJar or null
	 */
	public DepJar getDep(String groupId, String artifactId, String version, String classifier) {
		for (DepJar dep : container) {
			if (dep.isSame(groupId, artifactId, version, classifier)) {
				return dep;
			}
		}
		log.warn("cant find dep:" + groupId + ":" + artifactId + ":" + version + ":" + classifier);
		return null;
	}
	/**
	 * 获取依赖树中所有节点jar包
	 * @return
	 */
	public Set<DepJar> getAllDepJar() {
		return container;
	}

	@Override
	public Set<DepJar> getDepJarsWithScope(String scope) {
		Set<DepJar> depJars = usedDepJars.stream()
										 .filter(depJar -> depJar.getScope()!= null && depJar.getScope().equals(scope) && depJar.isSelected() && depJar.getDepth() == 1)
										 .collect(Collectors.toSet());
		return depJars;
	}
	@Override
	public Set<DepJar> getDepJarsWithScene(String scene){
		if (scene == "compile") {
			Set<DepJar> compileScopeDeps = getDepJarsWithScope("compile");
			Set<DepJar> runtimeDeps = getDepJarsWithScope("runtime");
			compileScopeDeps.addAll(runtimeDeps);
			return compileScopeDeps;
		}
		else if (scene == "runtime") {
			return getDepJarsWithScope("runtime");
		}
		else if (scene == "test"){
			return getDepJarsWithScope("test");
		}
		else {
			log.error("Invalid Scnene" + scene);
			return null;
		}
	}

	/**
	 * Obtains the paths of all the jars used in the dependency tree.
	 * @return
	 */
	@Override
	public Set<String> getUsedDepJarsPaths(){
		Set<String> usedJarPaths = new HashSet<>();
		for (DepJar depJar : getUsedDepJars()) {
			if (depJar.isHost()) {
				continue;
			}
			for (String path : depJar.getJarFilePaths(true)) {
				usedJarPaths.add(path);
			}
		}
		return usedJarPaths;
	}


	/**
	 * 获取所有jar包的本地路径
	 * @return
	 */
	public Set<String> getAllJarPaths() {
		Set<String> usedJarPaths = new HashSet<>();
		for (DepJar depJar : DepJars.i().getAllDepJar()) {
			for (String path : depJar.getJarFilePaths(true)) {
				usedJarPaths.add(path);
			}
		}
		return usedJarPaths;
	}
	/**
	 * use nodeAdapter to find the same DeoJar
	 * kernel is getDep(String groupId, String artifactId, String version, String classifier)
	 * @return same depJar or null
	 */
	public DepJar getDep(NodeAdapter nodeAdapter) {
		return getDep(nodeAdapter.getGroupId(), nodeAdapter.getArtifactId(), nodeAdapter.getVersion(),
				nodeAdapter.getClassifier());
	}
	/**
	 * 此函数存在多态
	 * get all used dep jar's file path
	 * @return
	 */
	public List<String> getUsedJarPaths() {
		List<String> usedJarPaths = new ArrayList<String>();
		for (DepJar depJar : DepJars.i().getAllDepJar()) {
			if (depJar.isSelected()) {
				for (String path : depJar.getJarFilePaths(true)) {
					usedJarPaths.add(path);
				}
			}
		}
		return usedJarPaths;
	}
	/**
	 *
	 * @param usedDepJar
	 * @return
	 */
	public List<String> getUsedJarPaths(DepJar usedDepJar) {
		List<String> usedJarPaths = new ArrayList<String>();
		for (DepJar depJar : DepJars.i().getAllDepJar()) {
			if (depJar.isSelected()) {
				if (depJar.isSameLib(usedDepJar)) {
				} else {
					for (String path : depJar.getJarFilePaths(true)) {
						usedJarPaths.add(path);
					}
				}
			}
			for (String path : usedDepJar.getJarFilePaths(true)) {
				usedJarPaths.add(path);
			}
		}
		return usedJarPaths;
	}
	/**
	 * @return path1;path2;path3
	 */
	public String getUsedJarPathsStr() {
		Set<String> usedJarPath = new LinkedHashSet<String>();
		StringBuilder sb = new StringBuilder();
		for (String path : getUsedJarPaths()) {
			sb.append(path + File.pathSeparator);
		}
		String paths = sb.toString();
		paths = paths.substring(0, paths.length() - 1);// delete last ;
		return paths;
	}
	/**
	 * @param cls
	 * @return usedDepJar that has class.
	 */
	public DepJar getClassJar(String cls) {
		for (DepJar depJar : DepJars.i().getAllDepJar()) {
			if (depJar.isSelected()) {
				if (depJar.containsCls(cls)) {
					return depJar;
				}
			}
		}
		return null;
	}
	/**
	 * 根据三坐标搜索jar包
	 * @param nodeInfo
	 * @return
	 */
	public DepJar getDepJar(String[] nodeInfo) {
		DepJar targetDepJar = null;
		for (DepJar depJar : container) {
			if (depJar.getGroupId().equals(nodeInfo[0])
					&& depJar.getArtifactId().equals(nodeInfo[1])
					&& depJar.getVersion().equals(nodeInfo[2])) {
				targetDepJar = depJar;
				break;
			}
		}
		return targetDepJar;
	}
	/**
	 * for cp arguments, add jar.
	 * @param jar
	 * @return
	 */
	public List<String> getUsedJarPathsSeqForRisk(DepJar jar) {
		List<String> ret = new ArrayList<String>();
		for (DepJar depJar : DepJars.i().getSeqUsedDepJars()) {
			if (!depJar.isSameLib(jar)) {
				for (String path : depJar.getJarFilePaths(true)) {
					ret.add(path);
				}
			}
		}
		ret.add(jar.getJarFilePath());
		return ret;
	}
	/**
	 * when cg, depJars sequence matters. sortDepJars, make the jar relative order same as classpath
	 * @param depJars
	 * @return
	 */
	public void sortDepJars(List<DepJar> depJars) {
		Collections.sort(depJars, (o1, o2) -> {
			if (o1.getPriority() == -1 && o2.getPriority() != -1) {
				return 1;
			}
			if (o2.getPriority() == -1 && o1.getPriority() != -1) {
				return -1;
			}
			int diff = o1.getPriority() - o2.getPriority();
			if (diff > 0) {
				return 1;
			} else if (diff < 0) {
				return -1;
			} else {
				return 0;
			}
		});
	}
	/**
	 * 按jar包文件名字母顺序排序jar包
	 * @param depJars
	 */
	@Deprecated
	public void sortDepJarsLetter(List<DepJar> depJars) {
		Collections.sort(depJars, (o1, o2) -> {
			String[] o1Split ;
			String[] o2Split;
			String windowsSep = "\\";
			if (windowsSep.equals(File.separator)) {
				o1Split = o1.getJarFilePath().split("\\\\");
				o2Split = o2.getJarFilePath().split("\\\\");
			}
			else {
				o1Split = o1.getJarFilePath().split(File.separator);
				o2Split = o2.getJarFilePath().split(File.separator);
			}
			return o1Split[o1Split.length - 1].compareTo(o2Split[o2Split.length - 1]);
		});
	}
	/**
	 * 获取指定groupId, artifactId的被使用jar包
	 * @param groupId
	 * @param artifactId
	 * @return
	 */
	public DepJar getUsedDepJar(String groupId, String artifactId) {
		for (DepJar depJar : getUsedDepJars()) {
			if (depJar.getGroupId().equals(groupId) && depJar.getArtifactId().equals(artifactId)) {
				return depJar;
			}
		}
		log.warn("No used dep Jar for " + groupId + ":" + artifactId);
		return null;
	}



}
