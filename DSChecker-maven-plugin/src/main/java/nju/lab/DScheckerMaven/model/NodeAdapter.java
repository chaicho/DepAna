package nju.lab.DScheckerMaven.model;

import neu.lab.conflict.GlobalVar;
import neu.lab.conflict.container.NodeAdapters;
import neu.lab.conflict.util.ClassifierUtil;
import neu.lab.conflict.util.Conf;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.evoshell.ShellConfig;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTree;


import java.io.File;
import java.util.*;
////import org.neo4j.cypher.internal.compiler.v2_3.No;

/**
 * @author asus
 * 依赖树节点适配器
 */
public class NodeAdapter {
	/**
	 * 依赖树节点
	 */
	protected DependencyNode node;
	/**
	 * 关联的Jar包数据结构
	 */
	protected DepJar depJar;
	/**
	 * jar包本地路径（其实这个list大小一般是1）
	 */
	protected List<String> 	filePaths;
	/**
	 * 依赖树节点被加载优先级
	 */
	protected int priority;
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		NodeAdapter that = (NodeAdapter) o;
		return priority == that.priority && Objects.equals(node, that.node) && Objects.equals(depJar, that.depJar) && Objects.equals(filePaths, that.filePaths);
	}
	@Override
	public int hashCode() {
		return Objects.hash(node, depJar, filePaths, priority);
	}
	protected static DependencyNode tempNode;
	protected static DependencyNode self;
	/**
	 * 拿node初始化适配器（NodeAdapter就是DependencyNode的适配器）
	 * @param node
	 */
	public NodeAdapter(DependencyNode node) {
		this.node = node;
		if (node != null) {
			resolve();
		}
	}
	public NodeAdapter(DependencyNode node, int priority) {
		this.node = node;
		this.priority = priority;
		if (node != null) {
			resolve();
		}
	}
	public DependencyNode getNode() {
		return node;
	}

	private void resolve() {
		try {
			// inner project is target/classes
			if (!isInnerProject()) {
				if (null == node.getPremanagedVersion()) {
					// artifact version of node is the version declared in pom.	节点的构件版本是POM中声明的版本。
					if (!node.getArtifact().isResolved()) {
						MavenUtil.getInstance().resolve(node.getArtifact());
					}
				} else {
					Artifact artifact = MavenUtil.getInstance().getArtifact(getGroupId(), getArtifactId(), getVersion(),
							getType(), getClassifier(), getScope());
					if (!artifact.isResolved()) {
						//解析这个构件
						MavenUtil.getInstance().resolve(artifact);
					}
				}
			}
		} catch (ArtifactResolutionException | ArtifactNotFoundException e) {
			MavenUtil.getInstance().getLog().warn("cant resolve " + this.toString());
		}
	}
	public String getGroupId() {
		return node.getArtifact().getGroupId();
	}
	public String getScope() {
		return node.getArtifact().getScope();
	}
	public String getArtifactId() {
		return node.getArtifact().getArtifactId();
	}
	public int getState() {
		return node.getState();
	}
	public String getVersion() {
		if (null != node.getPremanagedVersion()) {
			return node.getPremanagedVersion();
		} else {
			return node.getArtifact().getVersion();
		}
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	/**
	 * version changes because of dependency management
	 * 被dependency management更改过版本
	 * @return
	 */
	public boolean isVersionChanged() {
		return null != node.getPremanagedVersion();
	}
	/**
	 * change it from protected to public
	 * @author yzsjy
	 * @return
	 */
	public String getType() {
		return node.getArtifact().getType();
	}
	public String getClassifier() {
		return ClassifierUtil.transformClf(node.getArtifact().getClassifier());
	}
	/**
	 * used version is select from this node,if version was from management ,this node will return false.
	 * 这个版本的node是否被使用，如果被management更改过版本，将返回false
	 * @return
	 */
	public boolean isNodeSelected() {
		if (isVersionChanged()) {
			return false;
		}
		return node.getState() == DependencyNode.INCLUDED;
	}
	public String getManagedVersion() {
		return node.getArtifact().getVersion();
	}
	/**
	 * @param includeSelf
	 *            :whether includes self
	 * @return ancestors(from down to top) 从下至上
	 */
	public LinkedList<NodeAdapter> getAncestors(boolean includeSelf) {
		LinkedList<NodeAdapter> ancestors = new LinkedList<NodeAdapter>();
		if (includeSelf) {
			ancestors.add(this);
		}
		NodeAdapter father = getParent();
		while (null != father) {
			ancestors.add(father);
			father = father.getParent();
		}
		return ancestors;
	}
	/**
	 * jar class paths, contains cousins
	 * @param includeSelf : whether includes self
	 * @return List<String> jarCps
	 */
	public Collection<String> getAncestorJarCps(boolean includeSelf, Map<Integer, Integer> map, Map<Integer, Integer> distantRelativesMap){
		List<String> jarCps = new ArrayList<String>();
		Map<Integer, Integer> tempMap = new HashMap<>(0);
		if (includeSelf) {
			jarCps.addAll(this.getFilePath());
		}
		NodeAdapter father = getParent();
		tempNode = node.getParent();
		self = node;
		int level = 0;
		while (null != father) {
			List<NodeAdapter> cousins = getCousins();
			tempMap.put(level++, cousins.size());
			for(NodeAdapter cousion : cousins) {
				jarCps.addAll(cousion.getFilePath());
			}
			jarCps.addAll(father.getFilePath());
			father = father.getParent();
		}
		transformMap(tempMap, map, level);
		initDistantRelativesMap(map, distantRelativesMap, level);
		return jarCps;
	}
	/**
	 * get one route ancestor nodeAdapters for specified nodeAdapter
	 * do not include self!
	 * @param nodeAdapter
	 * @return
	 */
	public Collection<NodeAdapter> getOneRouteAncestors(NodeAdapter nodeAdapter) {
		Collection<NodeAdapter> ret = new HashSet<>();
		NodeAdapter father = nodeAdapter.getParent();
		while (null != father) {
			ret.add(father);
			father = father.getParent();
		}
		return ret;
	}
	/**
	 * get all duplicates nodeadapters for specified nodeAdapter
	 * do not include self!
	 * @param nodeAdapter
	 * @return
	 */
	public Collection<NodeAdapter> getAllDuplicates(NodeAdapter nodeAdapter) {
		Set<NodeAdapter> ret = new HashSet<>();
		String nodeAdapterGroupId = nodeAdapter.getGroupId();
		String nodeAdapterArtifactId = nodeAdapter.getArtifactId();
		String nodeAdapterVersion = nodeAdapter.getVersion();
		for (NodeAdapter nodeAdapter1 : NodeAdapters.i().getAllNodeAdapter()) {
			if (nodeAdapter1.getNode() != null && nodeAdapter1.getState() == DependencyNode.OMITTED_FOR_DUPLICATE) {
				if (nodeAdapter1.getGroupId().equals(nodeAdapterGroupId) &&
				nodeAdapter1.getArtifactId().equals(nodeAdapterArtifactId)
						&& nodeAdapter1.getVersion().equals(nodeAdapterVersion)) {
					ret.add(nodeAdapter1);
				}
			}
		}
		return ret;
	}
	/**
	 * get all same name nodeadapters for specified nodeAdapter
	 * do not include self!
	 * @param nodeAdapter
	 * @return
	 */
	public Collection<NodeAdapter> getAllSameNameJars(NodeAdapter nodeAdapter) {
		Set<NodeAdapter> ret = new HashSet<>();
		String nodeAdapterGroupId = nodeAdapter.getGroupId();
		String nodeAdapterArtifactId = nodeAdapter.getArtifactId();
		String nodeAdapterVersion = nodeAdapter.getVersion();
		for (NodeAdapter nodeAdapter1 : NodeAdapters.i().getAllNodeAdapter()) {
			if (nodeAdapter1.getNode() != null && nodeAdapter1.getState() == DependencyNode.OMITTED_FOR_DUPLICATE) {
				if (nodeAdapter1.getGroupId().equals(nodeAdapterGroupId) &&
						nodeAdapter1.getArtifactId().equals(nodeAdapterArtifactId)) {
					ret.add(nodeAdapter1);
				}
			}
		}
		return ret;
	}
	/**
	 * get immediate ancestor jar class paths, don't contain cousins
	 * @param includeSelf : whether includes self
	 * @return List<String> jarCps
	 */
	public Collection<String> getImmediateAncestorJarCps(boolean includeSelf){
		Set<NodeAdapter> loadedNodes = new HashSet<>();
		if (includeSelf) {
			loadedNodes.add(NodeAdapters.i().getNodeAdapter(node));
		}
		if (node == null) {
		}
		List<String> jarCps = new ArrayList<String>();
		if (!GlobalVar.i().prune2) {
			NodeAdapter father = getParent();
			Set<NodeAdapter> visited = new HashSet<>();
			if (father != null) {
				loadedNodes.add(father);
				Collection<NodeAdapter> duplicates = getAllDuplicates(father);
				LinkedList<NodeAdapter> queue = new LinkedList<>();
				queue.push(father);
				duplicates.forEach(dup -> {
					queue.push(dup);
				});
				visited.add(father);
				visited.addAll(duplicates);
				loadedNodes.add(father);
				while (queue.size() != 0) {
					NodeAdapter cur = queue.poll();
					NodeAdapter curFather = cur.getParent();
					if (curFather == null) {
						continue;
					}
					loadedNodes.add(curFather);
					if (!visited.contains(curFather)) {
						visited.add(curFather);
						queue.push(curFather);
					}
					Collection<NodeAdapter> curFatherDuplicates = getAllDuplicates(curFather);
					for (NodeAdapter curFatherDuplicate : curFatherDuplicates) {
						if (!visited.contains(curFatherDuplicate)) {
							queue.push(curFatherDuplicate);
							visited.add(curFatherDuplicate);
						}
					}
				}
			}
		}
		// open strategy 2
		else {
			Collection<NodeAdapter> allSameNameJars = getAllSameNameJars(this);
			NodeAdapter father = getParent();
			Collection<NodeAdapter> sameNameJarFathers = new HashSet<>();
			Set<NodeAdapter> visited = new HashSet<>();
			boolean hasFatherNotNull = false;
			if (father != null) {
				loadedNodes.add(father);
				hasFatherNotNull = true;
			}
			for (NodeAdapter allSameNameJar : allSameNameJars) {
				if (allSameNameJar != null && allSameNameJar.getParent() != null) {
					loadedNodes.add(allSameNameJar.getParent());
					sameNameJarFathers.add(allSameNameJar.getParent());
					hasFatherNotNull = true;
				}
			}
			if (hasFatherNotNull) {
				LinkedList<NodeAdapter> queue = new LinkedList<>();
				//queue.push(father);
				Set<NodeAdapter> allNeedPushNodeAdapters = new HashSet<>();
				allNeedPushNodeAdapters.add(father);
				Collection<NodeAdapter> duplicates = getAllDuplicates(father);
				allNeedPushNodeAdapters.addAll(duplicates);
				for (NodeAdapter sameNameJarFather : sameNameJarFathers) {
					Collection<NodeAdapter> sameNameJarFatherDuplicates = getAllDuplicates(sameNameJarFather);
					allNeedPushNodeAdapters.addAll(sameNameJarFatherDuplicates);
				}
				allNeedPushNodeAdapters.forEach(queue::push);
				visited.addAll(allNeedPushNodeAdapters);
				while (queue.size() != 0) {
					NodeAdapter cur = queue.poll();
					NodeAdapter curFather = cur.getParent();
					if (curFather == null) {
						continue;
					}
					loadedNodes.add(curFather);
					if (!visited.contains(curFather)) {
						visited.add(curFather);
						queue.push(curFather);
					}
					Collection<NodeAdapter> curFatherDuplicates = getAllDuplicates(curFather);
					for (NodeAdapter curFatherDuplicate : curFatherDuplicates) {
						if (!visited.contains(curFatherDuplicate)) {
							queue.push(curFatherDuplicate);
							visited.add(curFatherDuplicate);
						}
					}
				}
			}
		}
		//first level
		Map<String, NodeAdapter> loadedNodesMap = initLoadedNodesMap(loadedNodes);
		List<NodeAdapter> needAddNodes = addExcludeNodes(loadedNodesMap);
		Conf.getInstance().needAddNodeList.addAll(needAddNodes);
		for(NodeAdapter needAddNode : needAddNodes){
			loadedNodes.add(needAddNode);
			NodeAdapter needAddFather = needAddNode.getParent();
			while (null != needAddFather) {
				loadedNodes.add(needAddFather);
				needAddFather = needAddFather.getParent();
			}
		}
		//second level
		long startTime = System.currentTimeMillis();
		Map<String, NodeAdapter> firstLevelNeedAddNodesMap = initLoadedNodesMap(needAddNodes);
		List<NodeAdapter> firstLevelNeedAddNodes = addExcludeNodes(firstLevelNeedAddNodesMap);
		Conf.getInstance().firstLevelNeedAddNodeList.addAll(firstLevelNeedAddNodes);
		for(NodeAdapter firstLevelNeedAddNode : firstLevelNeedAddNodes){
			loadedNodes.add(firstLevelNeedAddNode);
			NodeAdapter needAddFather = firstLevelNeedAddNode.getParent();
			while (null != needAddFather) {
				loadedNodes.add(needAddFather);
				needAddFather = needAddFather.getParent();
			}
		}
		long runTime = System.currentTimeMillis() - startTime;
		Conf.getInstance().secondLevelRunTime.add(runTime);
		loadedNodes.remove(NodeAdapters.i().getNodeAdapter(node));
		for(NodeAdapter loadedNode : loadedNodes){
			jarCps.addAll(loadedNode.getFilePath());
		}
		return jarCps;
	}
	/**
	 * get immediate ancestor jar class paths, don't contain cousins
	 * @param includeSelf : whether includes self
	 * @return List<String> jarCps
	 */
	public Collection<DepJar> getImmediateAncestorJars(boolean includeSelf){
		//modified by grj, Set cannot maintain sequence
		List<NodeAdapter> loadedNodes = new ArrayList<>();
		if (includeSelf) {
			loadedNodes.add(NodeAdapters.i().getNodeAdapter(node));
		}
		List<DepJar> jars = new ArrayList<>();
		if (node == null) {
		}
		List<String> jarCps = new ArrayList<String>();
		if (!GlobalVar.i().prune2) {
			NodeAdapter father = getParent();
			Set<NodeAdapter> visited = new HashSet<>();
			if (father != null) {
				loadedNodes.add(father);
				Collection<NodeAdapter> duplicates = getAllDuplicates(father);
				LinkedList<NodeAdapter> queue = new LinkedList<>();
				queue.push(father);
				duplicates.forEach(dup -> {
					queue.push(dup);
				});
				visited.add(father);
				visited.addAll(duplicates);
				loadedNodes.add(father);
				while (queue.size() != 0) {
					NodeAdapter cur = queue.poll();
					NodeAdapter curFather = cur.getParent();
					if (curFather == null) {
						continue;
					}
					loadedNodes.add(curFather);
					if (!visited.contains(curFather)) {
						visited.add(curFather);
						queue.push(curFather);
					}
					Collection<NodeAdapter> curFatherDuplicates = getAllDuplicates(curFather);
					for (NodeAdapter curFatherDuplicate : curFatherDuplicates) {
						if (!visited.contains(curFatherDuplicate)) {
							queue.push(curFatherDuplicate);
							visited.add(curFatherDuplicate);
						}
					}
				}
			}
		}
		// open strategy 2
		else {
			Collection<NodeAdapter> allSameNameJars = getAllSameNameJars(this);
			NodeAdapter father = getParent();
			Collection<NodeAdapter> sameNameJarFathers = new HashSet<>();
			Set<NodeAdapter> visited = new HashSet<>();
			boolean hasFatherNotNull = false;
			if (father != null) {
				loadedNodes.add(father);
				hasFatherNotNull = true;
			}
			for (NodeAdapter allSameNameJar : allSameNameJars) {
				if (allSameNameJar != null && allSameNameJar.getParent() != null) {
					loadedNodes.add(allSameNameJar.getParent());
					sameNameJarFathers.add(allSameNameJar.getParent());
					hasFatherNotNull = true;
				}
			}
			if (hasFatherNotNull) {
				LinkedList<NodeAdapter> queue = new LinkedList<>();
				//queue.push(father);
				Set<NodeAdapter> allNeedPushNodeAdapters = new HashSet<>();
				allNeedPushNodeAdapters.add(father);
				Collection<NodeAdapter> duplicates = getAllDuplicates(father);
				allNeedPushNodeAdapters.addAll(duplicates);
				for (NodeAdapter sameNameJarFather : sameNameJarFathers) {
					Collection<NodeAdapter> sameNameJarFatherDuplicates = getAllDuplicates(sameNameJarFather);
					allNeedPushNodeAdapters.addAll(sameNameJarFatherDuplicates);
				}
				allNeedPushNodeAdapters.forEach(queue::push);
				visited.addAll(allNeedPushNodeAdapters);
				while (queue.size() != 0) {
					NodeAdapter cur = queue.poll();
					NodeAdapter curFather = cur.getParent();
					if (curFather == null) {
						continue;
					}
					loadedNodes.add(curFather);
					if (!visited.contains(curFather)) {
						visited.add(curFather);
						queue.push(curFather);
					}
					Collection<NodeAdapter> curFatherDuplicates = getAllDuplicates(curFather);
					for (NodeAdapter curFatherDuplicate : curFatherDuplicates) {
						if (!visited.contains(curFatherDuplicate)) {
							queue.push(curFatherDuplicate);
							visited.add(curFatherDuplicate);
						}
					}
				}
			}
		}
		/*String oriSig = node.getArtifact().getGroupId() + ":" + node.getArtifact().getArtifactId();
		List<DepJar> jars = new ArrayList<>();
		NodeAdapter father = getParent();
		while (null != father) {
			loadedNodes.add(father);
			father = father.getParent();
		}*/
		//first level
		Map<String, NodeAdapter> loadedNodesMap = initLoadedNodesMap(loadedNodes);
		List<NodeAdapter> needAddNodes = addExcludeNodes(loadedNodesMap);
		Conf.getInstance().needAddNodeList.addAll(needAddNodes);
		for(NodeAdapter needAddNode : needAddNodes){
			loadedNodes.add(needAddNode);
			NodeAdapter needAddFather = needAddNode.getParent();
			while (null != needAddFather) {
				loadedNodes.add(needAddFather);
				needAddFather = needAddFather.getParent();
			}
		}
		long startTime = System.currentTimeMillis();
		Map<String, NodeAdapter> firstLevelNeedAddNodesMap = initLoadedNodesMap(needAddNodes);
		List<NodeAdapter> firstLevelNeedAddNodes = addExcludeNodes(firstLevelNeedAddNodesMap);
		Conf.getInstance().firstLevelNeedAddNodeList.addAll(firstLevelNeedAddNodes);
		for(NodeAdapter firstLevelNeedAddNode : firstLevelNeedAddNodes){
			loadedNodes.add(firstLevelNeedAddNode);
			NodeAdapter needAddFather = firstLevelNeedAddNode.getParent();
			while (null != needAddFather) {
				loadedNodes.add(needAddFather);
				needAddFather = needAddFather.getParent();
			}
		}
		long runTime = System.currentTimeMillis() - startTime;
		Conf.getInstance().secondLevelRunTime.add(runTime);
		loadedNodes.remove(NodeAdapters.i().getNodeAdapter(node));
		for(NodeAdapter loadedNode : loadedNodes){
			jars.add(loadedNode.getDepJar());
		}
		return jars;
	}
	/**
	 * get immediate ancestor jar class paths, don't contain cousins
	 * @param includeSelf : whether includes self
	 * @return List<String> jarCps
	 */
	public Collection<String> getImmediateAncestorJarCps4Scene6(boolean includeSelf){
		Set<NodeAdapter> loadedNodes = new HashSet<>();
		if (includeSelf) {
			loadedNodes.add(NodeAdapters.i().getNodeAdapter(node));
		}
		String oriSig = node.getArtifact().getGroupId() + ":" + node.getArtifact().getArtifactId();
		List<String> jarCps = new ArrayList<String>();
		NodeAdapter father = getRealParent();
		while (null != father) {
			loadedNodes.add(father);
			father = father.getParent();
		}
		//first level
		Map<String, NodeAdapter> loadedNodesMap = initLoadedNodesMap(loadedNodes);
		List<NodeAdapter> needAddNodes = addExcludeNodes(loadedNodesMap);
		Conf.getInstance().needAddNodeList.addAll(needAddNodes);
//		for(NodeAdapter needAddNode : needAddNodes){
//			MavenUtil.getInstance().getLog().warn("add Exclude node : " + needAddNode.getSelectedNodeWholeSig());
//		}
//		Set<NodeAdapter> firstLevelLoadedNodes = new HashSet<>();
		for(NodeAdapter needAddNode : needAddNodes){
//			MavenUtil.getInstance().getLog().warn("needAddNode sig : " + needAddNode.getSelectedNodeWholeSig());
			loadedNodes.add(needAddNode);
//			firstLevelLoadedNodes.add(needAddNode);
			NodeAdapter needAddFather = needAddNode.getParent();
			while (null != needAddFather) {
				loadedNodes.add(needAddFather);
//				firstLevelLoadedNodes.add(needAddFather);
				needAddFather = needAddFather.getParent();
			}
		}
//		for(NodeAdapter firstLevelLoadedNode : firstLevelLoadedNodes){
//			MavenUtil.getInstance().getLog().warn("first level add node : " + firstLevelLoadedNode.getSelectedNodeWholeSig());
//		}
		//second level
		long startTime = System.currentTimeMillis();
		Map<String, NodeAdapter> firstLevelNeedAddNodesMap = initLoadedNodesMap(needAddNodes);
		List<NodeAdapter> firstLevelNeedAddNodes = addExcludeNodes(firstLevelNeedAddNodesMap);
		Conf.getInstance().firstLevelNeedAddNodeList.addAll(firstLevelNeedAddNodes);
//		MavenUtil.getInstance().getLog().warn("firstLevelNeedAddNodes Size : " + firstLevelNeedAddNodes.size());
//		Set<NodeAdapter> secondLevelLoadedNodes = new HashSet<>();
		for(NodeAdapter firstLevelNeedAddNode : firstLevelNeedAddNodes){
//			MavenUtil.getInstance().getLog().warn("needAddNode sig : " + needAddNode.getSelectedNodeWholeSig());
			loadedNodes.add(firstLevelNeedAddNode);
//			secondLevelLoadedNodes.add(firstLevelNeedAddNode);
			NodeAdapter needAddFather = firstLevelNeedAddNode.getParent();
			while (null != needAddFather) {
				loadedNodes.add(needAddFather);
//				secondLevelLoadedNodes.add(needAddFather);
				needAddFather = needAddFather.getParent();
			}
		}
		long runTime = System.currentTimeMillis() - startTime;
		Conf.getInstance().secondLevelRunTime.add(runTime);
//		for(NodeAdapter secondLevelLoadedNode : secondLevelLoadedNodes){
//			MavenUtil.getInstance().getLog().warn("second level add node : " + secondLevelLoadedNode.getSelectedNodeWholeSig());
//		}
//		MavenUtil.getInstance().getLog().warn("before");
//		for(NodeAdapter loadedNode : loadedNodes){
//			MavenUtil.getInstance().getLog().warn("loadedNode : " + loadedNode.getSelectedNodeWholeSig());
//		}
//		List<DependencyNode> directDepNodeList = Conf.getInstance().dependencyList.get(1);
//		for(DependencyNode directDepNode : directDepNodeList){
//			String tempSig = directDepNode.getArtifact().getGroupId() + ":" + directDepNode.getArtifact().getArtifactId();
//			if(!oriSig.equals(tempSig)) {
//				loadedNodes.add(NodeAdapters.i().getNodeAdapter(directDepNode));
//			}
//		}
////		MavenUtil.getInstance().getLog().warn("mid");
////		for(NodeAdapter loadedNode : loadedNodes){
////			MavenUtil.getInstance().getLog().warn("loadedNode : " + loadedNode.getSelectedNodeWholeSig());
////		}
		loadedNodes.remove(NodeAdapters.i().getNodeAdapter(node));
//		MavenUtil.getInstance().getLog().warn("after");
		for(NodeAdapter loadedNode : loadedNodes){
//			MavenUtil.getInstance().getLog().warn("loadedNode : " + loadedNode.getSelectedNodeWholeSig());
			jarCps.addAll(loadedNode.getFilePath());
		}
//		for(String jarCp : jarCps){
//			MavenUtil.getInstance().getLog().warn("jarCp : " + jarCp);
//		}
		return jarCps;
	}
	/**
	 * get immediate ancestor jar class paths, don't contain cousins
	 * @param includeSelf : whether includes self
	 * @return List<String> jarCps
	 */
	public Collection<DepJar> getImmediateAncestorJars4Scene6(boolean includeSelf){
		Set<NodeAdapter> loadedNodes = new HashSet<>();
		if (includeSelf) {
			loadedNodes.add(NodeAdapters.i().getNodeAdapter(node));
		}
		String oriSig = node.getArtifact().getGroupId() + ":" + node.getArtifact().getArtifactId();
		List<DepJar> jars = new ArrayList<>();
		NodeAdapter father = getRealParent();
		while (null != father) {
			loadedNodes.add(father);
			father = father.getParent();
		}
		//first level
		Map<String, NodeAdapter> loadedNodesMap = initLoadedNodesMap(loadedNodes);
		List<NodeAdapter> needAddNodes = addExcludeNodes(loadedNodesMap);
		Conf.getInstance().needAddNodeList.addAll(needAddNodes);
		for(NodeAdapter needAddNode : needAddNodes){
			loadedNodes.add(needAddNode);
			NodeAdapter needAddFather = needAddNode.getParent();
			while (null != needAddFather) {
				loadedNodes.add(needAddFather);
				needAddFather = needAddFather.getParent();
			}
		}
		long startTime = System.currentTimeMillis();
		Map<String, NodeAdapter> firstLevelNeedAddNodesMap = initLoadedNodesMap(needAddNodes);
		List<NodeAdapter> firstLevelNeedAddNodes = addExcludeNodes(firstLevelNeedAddNodesMap);
		Conf.getInstance().firstLevelNeedAddNodeList.addAll(firstLevelNeedAddNodes);
		for(NodeAdapter firstLevelNeedAddNode : firstLevelNeedAddNodes){
			loadedNodes.add(firstLevelNeedAddNode);
			NodeAdapter needAddFather = firstLevelNeedAddNode.getParent();
			while (null != needAddFather) {
				loadedNodes.add(needAddFather);
				needAddFather = needAddFather.getParent();
			}
		}
		long runTime = System.currentTimeMillis() - startTime;
		Conf.getInstance().secondLevelRunTime.add(runTime);
		loadedNodes.remove(NodeAdapters.i().getNodeAdapter(node));
		for(NodeAdapter loadedNode : loadedNodes){
			jars.add(loadedNode.getDepJar());
		}
		return jars;
	}
	/**
	 * get only father jar class paths, don't contain cousins and don't consider exclusion
	 * @return List<String> jarCps
	 */
	public Collection<String> getOnlyFatherJarCps() {
		Set<NodeAdapter> loadedNodes = new HashSet<>();
		List<String> jarCps = new ArrayList<String>();
		NodeAdapter father = getParent();
		while (null != father) {
			loadedNodes.add(father);
			father = father.getParent();
		}
		for(NodeAdapter loadedNode : loadedNodes){
			jarCps.addAll(loadedNode.getFilePath());
		}
//		for(String jarCp : jarCps){
//			MavenUtil.getInstance().getLog().warn("jarCp : " + jarCp);
//		}
		return jarCps;
	}
	/**
	 * print, for test
	 * @param map
	 */
	private void printNodesMap(Map<String, NodeAdapter> map){
		for(Map.Entry<String, NodeAdapter> entry : map.entrySet()){
			MavenUtil.getInstance().getLog().warn("needAddNode : " + entry.getKey());
			MavenUtil.getInstance().getLog().warn("NodeAdapter value : " + entry.getValue().getSelectedNodeWholeSig());
		}
	}
	/**
	 * init loaded nodes, used to search excluded nodes
	 * @param loadedNodes
	 * @return Map<String, NodeAdapter> loadedNodesMap
	 */
	private Map<String, NodeAdapter> initLoadedNodesMap(Set<NodeAdapter> loadedNodes){
		Map<String, NodeAdapter> loadedNodesMap = new HashMap<>(0);
		for(NodeAdapter loadedNode : loadedNodes){
			String sig = loadedNode.getOnlySelectedNodeSig();
			loadedNodesMap.put(sig, loadedNode);
		}
		return loadedNodesMap;
	}
	/**
	 * init loaded nodes, used to search excluded nodes
	 * @param needAddNodes
	 * @return Map<String, NodeAdapter> loadedNodesMap
	 */
	private Map<String, NodeAdapter> initLoadedNodesMap(List<NodeAdapter> needAddNodes){
		Map<String, NodeAdapter> loadedNodesMap = new HashMap<>(0);
		for(NodeAdapter loadedNode : needAddNodes){
			String sig = loadedNode.getOnlySelectedNodeSig();
			loadedNodesMap.put(sig, loadedNode);
		}
		return loadedNodesMap;
	}
	/**
	 * if the node is excluded, add it
	 * @param loadedNodesMap
	 * @return List<NodeAdapter> needAddNodes
	 */
	private List<NodeAdapter> addExcludeNodes(Map<String, NodeAdapter> loadedNodesMap){
		List<NodeAdapter> needAddNodes = new ArrayList<>();
		for(Map.Entry<String, List<NodeAdapter>> entry : Conf.getInstance().dependencyMap.entrySet()){
			if (loadedNodesMap.containsKey(entry.getKey())){
				needAddNodes.addAll(entry.getValue());
			}
		}
		return needAddNodes;
	}
	/**
	 * old version
	 * @param allCousins : all of the cousins
	 * @return List<NodeAdapter> needLoads
	 */
	private List<NodeAdapter> detectCousins(List<NodeAdapter> allCousins){
		List<NodeAdapter> needLoads = new ArrayList<>();
		for(NodeAdapter cousin : allCousins){
			MavenUtil.getInstance().getLog().info("cousin : " + cousin.getSelectedNodeWholeSig());
			String localDirPath = ShellConfig.mvnRep + cousin.getGroupId().replace(".", File.separator) +
					File.separator + cousin.getArtifactId() + File.separator + cousin.getVersion() + File.separator;
			String localPomPath = localDirPath + cousin.getArtifactId() + "-" + cousin.getVersion() + ".pom";
			MavenUtil.getInstance().getLog().info("localPath : " + localPomPath);
			File file = new File(localPomPath);
			if (file.exists()){
				SAXReader reader = new SAXReader();
				try {
					Document document = reader.read(file);
					Element root = document.getRootElement();
					Element dependencies = root.element("dependencies");
					if(dependencies != null) {
						for (Object o : dependencies.elements("dependency")) {
							Element dependency = (Element) o;
							Element exclusions = dependency.element("exclusions");
							if (exclusions != null) {
								for (Object oTwo : exclusions.elements("exclusion")) {
									Element exclusion = (Element) oTwo;
									Element groupId = exclusion.element("groupId");
									Element artId = exclusion.element("artifactId");
									if (node.getArtifact().getGroupId().equals(groupId.getText())
											&& node.getArtifact().getArtifactId().equals(artId.getText())) {
										needLoads.add(cousin);
									}
								}
							}
						}
					}
				} catch (DocumentException e) {
					e.printStackTrace();
				}
			}
		}
		return needLoads;
	}
	/**
	 * old version
	 */
	private void transformMap(Map<Integer, Integer> tempMap, Map<Integer, Integer> map, int level){
		for(int oriLevel : tempMap.keySet()){
			map.put(level - oriLevel, tempMap.get(oriLevel));
		}
		map.put(0, 0);
	}
	/**
	 * get distant relative node map 得到远房亲戚节点图， old version
	 * @param level 层次遍历层数
	 */
	private void initDistantRelativesMap(Map<Integer, Integer> map, Map<Integer, Integer> distantRelativesMap, int level){
		String oriSig = node.getArtifact().getGroupId() + ":" + node.getArtifact().getArtifactId();
		for(int tempLevel : map.keySet()){
			List<String> allNode = Conf.getInstance().list.get(tempLevel);
			if (!allNode.isEmpty()) {
				int cousinsIncludeSelf = map.get(tempLevel) + 1;
				int val = allNode.size() - cousinsIncludeSelf;
				if (allNode.contains(oriSig)) {
					--val;
				}
				if(tempLevel == level){
					++val;
				}
				if (val < 0){
					val = 0;
				}
				distantRelativesMap.put(tempLevel, val);
			} else {
				distantRelativesMap.put(tempLevel, 0);
			}
		}
	}
	/**
	 * print, for test
	 * @param map
	 */
	private void printMap(Map<Integer, Integer> map){
		for(int level : map.keySet()){
			MavenUtil.getInstance().getLog().warn("level : " + level);
			MavenUtil.getInstance().getLog().warn("size : " + map.get(level));
		}
	}
	/**
	 * 得到父节点
	 * @return
	 */
	public NodeAdapter 	getParent() {
		if (null == node.getParent()) {
			return null;
		}
		return NodeAdapters.i().getNodeAdapter(node.getParent());
	}
	/**
	 * 获取这个适配器对应的依赖树节点的所有子节点的适配器
	 * @return
	 */
	public List<NodeAdapter> getChildren() {
		if (node == null) {
			return null;
		}
		if (null == node.getChildren()) {
			return null;
		}
		List<NodeAdapter> ret = new ArrayList<>();
		node.getChildren().forEach(child -> {
			NodeAdapter childAdapter = NodeAdapters.i().getNodeAdapter(child);
			if (childAdapter != null) {
				ret.add(childAdapter);
			}
		});
		return ret;
	}
	/**
	 * 获取jar包本地路径
	 * @return
	 */
	public String getPomFilePath() {
		String localPomPath = "";
		// TODO maybe has some problem. if dependency cycle occurs?
		if (NodeAdapters.i().getNodeAdapter(MavenUtil.getInstance().getMojo().root).getGroupId().equals(this.getGroupId())
				&& NodeAdapters.i().getNodeAdapter(MavenUtil.getInstance().getMojo().root).getArtifactId().equals(this.getArtifactId())){
			localPomPath = MavenUtil.getInstance().getProjectPom();
		}else {
			String localDirPath = ShellConfig.mvnRep + this.getGroupId().replace(".", File.separator) +
					File.separator + this.getArtifactId() + File.separator + this.getVersion() + File.separator;
			localPomPath = localDirPath + this.getArtifactId() + "-" + this.getVersion() + ".pom";
		}
		return localPomPath;
	}
	/**
	 * get node's cousins, old version
	 * @return List<NodeAdapter> cousins
	 */
	private List<NodeAdapter> getCousins(){
		List<NodeAdapter> cousins = new ArrayList<>();
		String oriSig = node.getArtifact().getGroupId() + ":" + node.getArtifact().getArtifactId();
		if(null == tempNode.getChildren()) {
			return null;
		}
		for(DependencyNode child : tempNode.getChildren()) {
			String tempSig = child.getArtifact().getGroupId() + ":" + child.getArtifact().getArtifactId();
			if(!child.equals(self) && !oriSig.equals(tempSig) && NodeAdapters.i().getNodeAdapter(child).isNodeSelected()) {
				cousins.add(NodeAdapters.i().getNodeAdapter(child));
			}
		}
		self = tempNode;
		tempNode = tempNode.getParent();
		return cousins;
	}
	/**
	 * 获取groupId:artifactId
	 * @return
	 */
	public String getOnlySelectedNodeSig(){
		return getGroupId() + ":" + getArtifactId();
	}
	/**
	 * 获取groupId:artifactId:version
	 * @return
	 */
	public String getSelectedNodeWholeSig(){
		return getGroupId() + ":" + getArtifactId() + ":" + getVersion();
	}
	/**
	 * 得到文件路径
	 * @return
	 */
	public List<String> getFilePath() {
		if ("groovy-all".equals(this.getArtifactId())) {
			System.out.println();
		}
		if (filePaths == null) {
			filePaths = new ArrayList<String>();
			if (isInnerProject()) {// inner project is target/classes
				filePaths.add(MavenUtil.getInstance().getMavenProject(this).getBuild().getOutputDirectory());
				// filePaths = UtilGetter.i().getSrcPaths();
			} else {// dependency is repository address
				try {
					if (null == node.getPremanagedVersion()) {
						filePaths.add(node.getArtifact().getFile().getAbsolutePath());
					} else {
						Artifact artifact = MavenUtil.getInstance().getArtifact(getGroupId(), getArtifactId(), getVersion(),
								getType(), getClassifier(), getScope());
						if (!artifact.isResolved()) {
							MavenUtil.getInstance().resolve(artifact);
						}
						filePaths.add(artifact.getFile().getAbsolutePath());
					}
				} catch (ArtifactResolutionException e) {
					MavenUtil.getInstance().getLog().warn("cant resolve " + this.toString());
				} catch (ArtifactNotFoundException e) {
					MavenUtil.getInstance().getLog().warn("cant resolve " + this.toString());
				}
			}
		}
		MavenUtil.getInstance().getLog().debug("node filepath for " + toString() + " : " + filePaths);
		if (GlobalVar.i().isTest && new File(filePaths.get(0)).isDirectory() && !filePaths.get(0).endsWith("test-classes")) {
			List<String> ret= new ArrayList<>();
			ret.add("");
			ret.set(0, filePaths.get(0).substring(0, filePaths.get(0).lastIndexOf("classes")) + "test-classes");
			MavenUtil.getInstance().getLog().info("ret Nodeadapter" + ret);
			return ret;
		}
		return filePaths;
	}
	/**
	 * 是否是Host项目的节点适配器
	 * @return
	 */
	public boolean isInnerProject() {
		return MavenUtil.getInstance().isInner(this);
	}
	/**
	 * judge whether is self
	 * @return boolean
	 */
	public boolean isSelf(DependencyNode node2) {
		return node.equals(node2);
	}
	/**
	 * judge whether is self
	 * @return boolean
	 */
	public boolean isSelf(MavenProject mavenProject) {
		return isSelf(mavenProject.getGroupId(), mavenProject.getArtifactId(), mavenProject.getVersion(),
				ClassifierUtil.transformClf(mavenProject.getArtifact().getClassifier()));
	}
	/**
	 * judge whether is self
	 * @return boolean
	 */
	public boolean isSelf(String groupId2, String artifactId2, String version2, String classifier2) {
		return getGroupId().equals(groupId2) && getArtifactId().equals(artifactId2) && getVersion().equals(version2)
				&& getClassifier().equals(classifier2);
	}
	/**
	 * judge whether is the same lib
	 * @param nodeAdapter to be compared
	 * @return
	 */
	public boolean isSameLib(NodeAdapter nodeAdapter) {
		return getGroupId().equals(nodeAdapter.getGroupId()) && getArtifactId().equals(nodeAdapter.getArtifactId());
	}
	public MavenProject getSelfMavenProject() {
		return MavenUtil.getInstance().getMavenProject(this);
	}
	/**
	 * 获取该适配器对应的Jar包数据结构
	 * @return
	 */
	public DepJar getDepJar() {
		if (depJar == null) {
			depJar = DepJars.i().getDep(this);
		}
		return depJar;
	}
	@Override
	public String toString() {
		String scope = getScope();
		if (null == scope) {
			scope = "";
		}
		return getGroupId() + ":" + getArtifactId() + ":" + getVersion() + ":" + getClassifier() + ":" + scope + " priority : " + priority;
	}
	/**
	 * get the groupId and artifactId of the node in the original dependency tree
	 * @author yzsjy
	 * @return
	 */
	public String getSig() {
		return getGroupId() + ":" + getArtifactId();
	}
	/**
	 * get invoke path included self
	 * @return string
	 */
	public String getWholePath() {
		StringBuilder sb = new StringBuilder(toString());
		NodeAdapter father = getParent();
		while (null != father) {
			sb.insert(0, father.toString() + " + ");
			father = father.getParent();
		}
		return sb.toString();
	}
	/**
	 * Scene6, node path
	 * @return
	 */
	public String getScene6WholePath() {
		NodeAdapter realParent = getRealParent();
		if (realParent == null) {
			return getWholePath();
		} else {
			StringBuilder sb = new StringBuilder(toString());
			while (null != realParent) {
				sb.insert(0, realParent.toString() + " + ");
				realParent = realParent.getParent();
			}
			return sb.toString();
		}
	}
	/**
	 * detect pom file to get real parent
	 * @return
	 */
	private NodeAdapter getRealParent() {
		DependencyNode parent = node.getParent();
		if (parent == null) {
			return null;
		}
		List<DependencyNode> children = parent.getChildren();
		for (DependencyNode node : children) {
			DependencyTree tmpTree = new DependencyTree(node, node.getChildren());
			DependencyNode tmpNode = new DependencyNode(node.getArtifact());
			DependencyTree tmpTree2 = new DependencyTree(tmpNode, tmpNode.getChildren());
			String localDirPath = ShellConfig.mvnRep + node.getArtifact().getGroupId().replace(".", File.separator) +
					File.separator + node.getArtifact().getArtifactId() + File.separator
					+ node.getArtifact().getVersion() + File.separator;
			String localPomPath = localDirPath + node.getArtifact().getArtifactId() + "-"
					+ node.getArtifact().getVersion() + ".pom";
			if (detect(localPomPath)) {
				return NodeAdapters.i().getNodeAdapter(node);
			}
		}
		return null;
	}
	/**
	 * detect pom file
	 * @param localPomPath
	 * @return
	 */
	private boolean detect(String localPomPath){
		File file = new File(localPomPath);
		if (file.exists()){
			SAXReader reader = new SAXReader();
			try {
				Document document = reader.read(file);
				Element root = document.getRootElement();
				Element dependencies = root.element("dependencies");
				if(dependencies != null) {
					for (Object o : dependencies.elements("dependency")) {
						Element dependency = (Element) o;
						Element groupId = dependency.element("groupId");
						Element artifactId = dependency.element("artifactId");
						if (node.getArtifact().getGroupId().equals(groupId.getText())
								&& node.getArtifact().getArtifactId().equals(artifactId.getText())) {
							return true;
						}
					}
				}
			} catch (DocumentException e) {
				e.printStackTrace();
			}
		} else {
			MavenUtil.getInstance().getLog().warn("get circular node real parent wrong");
		}
		return false;
	}
	/**
	 * 获取树根？
	 * @return
	 */
	@Deprecated
	public NodeAdapter getPreNode() {
		NodeAdapter father = getParent();
		NodeAdapter preNode = null;
		while (null != father && !father.getDepJar().isHost()) {
			preNode = father;
			father = father.getParent();
		}
		return preNode;
	}
	/**
	 * get ancestors
	 * @return LinkedList<NodeAdapter> wholeAncestors
	 */
	public List<NodeAdapter> getWholeAncestors() {
		LinkedList<NodeAdapter> wholeAncestors = new LinkedList<>();
		NodeAdapter father = getParent();
		while (null != father) {
			wholeAncestors.addFirst(father);
			father = father.getParent();
		}
		return wholeAncestors;
	}
	/**
	 * 获取<groupId:artifactId:version>
	 * @return
	 */
	public String getWholeSig() {
		return "<" + getGroupId() + ":" + getArtifactId() + ":" + getVersion() + ">";
	}
	/**
	 * 获取groupId:artifactId:version
	 * @return
	 */
	public String getNodeInfo() {
		return getGroupId() + ":" + getArtifactId() + ":" + getVersion();
	}
	/**
	 * get node depth
	 * @return depth
	 */
	public int getNodeDepth() {
		int depth = 1;
		NodeAdapter father = getParent();
		while (null != father) {
			depth++;
			father = father.getParent();
		}
		return depth;
	}
	/**
	 * 返回自身
	 * @return
	 */
	public NodeAdapter getBeforeManagedNode() {
		return this;
	}
	/**
	 * 获取节点是否被加载
	 * @return
	 */
	public boolean isOriginalSelected() {
		return node.getState() == DependencyNode.INCLUDED;
	}
}
