package neu.lab.conflict.vo;
//import neu.lab.conflict.util.risk.ConflictJRisk;
import neu.lab.conflict.util.MyLogger;
import nju.lab.DSchecker.model.DepJar;
import nju.lab.DSchecker.model.NodeAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * 描述Jar包版本冲突问题的数据结构
 * @author guoruijie ...
 */
public class Conflict {
	private String groupId;
	private String artifactId;
	private Set<NodeAdapter> nodes;
	private Set<DepJar> depJars;
	private DepJar usedDepJar;
	/**
	 * by grj. map conflictJRisk
	 * @param groupId
	 * @param artifactId
	 */
//	private ConflictJRisk conflictJRisk;
//	/**
//	 * 获取封装的冲突描述类
//	 * @return
//	 */
//	public ConflictJRisk getConflictJRisk() {
//		if (conflictJRisk == null) {
//			conflictJRisk = new ConflictJRisk(this);
//		}
//		return conflictJRisk;
//	}
//	/**
//	 * 设置封装的冲突描述类
//	 * @param conflictJRisk
//	 */
//	public void setConflictJRisk(ConflictJRisk conflictJRisk) {
//		this.conflictJRisk = conflictJRisk;
//	}
	// private ConflictRiskAna riskAna;
	/**
	 * 构造函数
	 * @param groupId
	 * @param artifactId
	 */
	public Conflict(String groupId, String artifactId) {
		nodes = new HashSet<NodeAdapter>();
		this.groupId = groupId;
		this.artifactId = artifactId;
	}
	/**
	 * 得到使用的DepJar
	 * @return usedDepJar
	 */
	public DepJar getUsedDepJar() {
		if (null == usedDepJar) {
			for (DepJar depJar : getDepJars()) {
				if (depJar.isSelected()) {
					if (null != usedDepJar) {
						MyLogger.i().warn("duplicate used version for dependency:" + groupId + ":" + artifactId);
					}
					usedDepJar = depJar;
				}
			}
		}
		if (null ==usedDepJar) {
			MyLogger.i().warn("Strange! no used version for dependency:" + groupId + ":" + artifactId);
		}
		return usedDepJar;
	}
	/**
	 * 设置usedDepJar
	 */
	public void setUsedDepJar(DepJar depJar) {
		usedDepJar = depJar;
	}
	/**
	 * 得到除了被选中的jar以外的其他被依赖的jar包
	 * @return
	 */
	public Set<DepJar> getOtherDepJar4Use() {
		Set<DepJar> usedDepJars = new HashSet<DepJar>();
		for (DepJar depJar : depJars) {
			System.out.println("conflict.getotherdepjar4use" + depJar.toString());
			if (depJar.isSelected()) {
				System.out.println("select depJar" + depJar.toString());
			}
			else {
				usedDepJars.add(depJar);
			}
		}
		return usedDepJars;
	}
	/**
	 * 新增一个这个冲突涉及到的依赖树中的节点
	 * @param nodeAdapter
	 */
	public void addNode(NodeAdapter nodeAdapter) {
		nodes.add(nodeAdapter);
	}
	/**
	 * 同一个构件
	 * @param groupId2
	 * @param artifactId2
	 * @return
	 */
	public boolean sameArtifact(String groupId2, String artifactId2) {
		return groupId.equals(groupId2) && artifactId.equals(artifactId2);
	}
	/**
	 * get all dep jars, no parameter
	 * @return depJars
	 */
	public Set<DepJar> getDepJars() {
		if (depJars == null) {
			depJars = new HashSet<DepJar>();
			for (NodeAdapter nodeAdapter : nodes) {
				depJars.add(nodeAdapter.getDepJar());
			}
		}
		return depJars;
	}
	/**
	 * get all depjars of the new conflict after repairing
	 * @author yzsjy
	 * @param depJar
	 * @return
	 */
	public Set<DepJar> getDepJars(Set<DepJar> depJar) {
		depJars = new HashSet<DepJar>();
		for (DepJar jar : depJar) {
			if (jar.getGroupId().equals(this.getGroupId()) && jar.getArtifactId().equals(this.getArtifactId())) {
				depJars.add(jar);
			}
		}
		return depJars;
	}
	public Set<NodeAdapter> getNodeAdapters() {
		return this.nodes;
	}
	/**
	 * verify whither is conflict, when size > 1, return true
	 * @return boolean
	 */
	public boolean isConflict() {
		return getDepJars().size() > 1;
	}
	/**
	 * 无条件创建一个对冲突数据结构的封装
	 * @return
	 */
//	public ConflictJRisk getJRisk() {
//		return new ConflictJRisk(this);
//	}
	/**
	 * toStrnig
	 * @return
	 */
	@Override
	public String toString() {
		String str = groupId + ":" + artifactId + " conflict version:";
		for (DepJar depJar : depJars) {
			str = str + depJar.getVersion() + ":" + depJar.getClassifier() + "-";
		}
		str = str + "---used jar:" + getUsedDepJar().getVersion() + ":" + getUsedDepJar().getClassifier();
		return str;
	}
	/**
	 * get
	 * @return
	 */
	public String getConflict() {
		String str = groupId + "." + artifactId + "+" + artifactId;
		return str;
	}
	/**
	 * get
	 * @return
	 */
	public String getGroupId() {
		return groupId;
	}
	/**
	 * get
	 * @return
	 */
	public String getArtifactId() {
		return artifactId;
	}
	/**
	 * 返回jar包版本冲突的groupId:artifactId
	 * @return
	 */
	public String getSig() {
		return getGroupId() + ":" + getArtifactId();
	}
	/**
	 * @return first version is the used version
	 */
	public List<String> getVersions(){
		List<String> versions = new ArrayList<String>();
		if (getUsedDepJar() != null) {
			versions.add(getUsedDepJar().getVersion());
		}
		for(DepJar depJar:depJars) {
			String version = depJar.getVersion();
			if(!versions.contains(version)) {
				versions.add("/"+version);
			}
		}
		return versions;
	}
	/**
	 * 获取冲突中jar包所有版本号
	 * @return
	 */
	public List<String> getOriginVersions() {
		List<String> versions = new ArrayList<String>();
		versions.add(getUsedDepJar().getVersion());
		for(DepJar depJar:depJars) {
			String version = depJar.getVersion();
			if(!versions.contains(version)) {
				versions.add(version);
			}
		}
		return versions;
	}
	/**
	 * get no use version
	 * @author Nos
	 * @return String ver
	 */
	public String getNoUseVersion() {
		for (DepJar depJar : depJars) {
			if(!depJar.isSelected()) {
				return depJar.getVersion();
			}
		}
		return "";
	}
	/**
	 * get no use versions
	 * @author Nos
	 * @return List<String> versions
	 */
	public List<String> getNoUseVersions(){
		List<String> versions = new ArrayList<>();
		for (DepJar depJar : depJars) {
			if(!depJar.isSelected()) {
				if (!versions.contains(depJar.getVersion())) {
					versions.add(depJar.getVersion());
				}
			}
		}
		return versions;
	}
	/**
	 * 获取所有被舍弃jar包
	 * @return
	 */
	@Deprecated
	public Set<DepJar> getNoUsedDepJar() {
		Set<DepJar> unusedDepJars = new HashSet<>();
		for (DepJar depJar : depJars) {
			if (!depJar.isSelected()) {
				unusedDepJars.add(depJar);
			}
		}
		return unusedDepJars;
	}
	/**
	 * unknown
	 * @return
	 */
	public String getConflictJarInfo() {
		Set<String> unusedDepJars = new HashSet<>();
		for (DepJar depJar : depJars) {
			if (!depJar.isSelected()) {
				unusedDepJars.add(depJar.getSig());
			}
		}
		int size = unusedDepJars.size();
		if (size < 2) {
			return unusedDepJars.iterator().next();
		} else {
			int num = 0;
			String info = null;
			for (String unusedDepJar : unusedDepJars) {
				if (num == 0) {
					info = unusedDepJar;
				} else if (num == size - 1) {
					info += " and " + unusedDepJar;
				} else {
					info += ", " + unusedDepJar;
				}
				num++;
			}
			return info;
		}
	}
	/**
	 * 获取nodeAdapters数量
	 * @return
	 */
	public int getSize(){
		return nodes.size();
	}
	/**
	 * get used node
	 * @return usedNode
	 */
	public NodeAdapter getUsedNode() {
		NodeAdapter usedNode = null;
		for (NodeAdapter node : nodes) {
			if (node.isNodeSelected()) {
				usedNode = node;
			}
		}
		return usedNode;
	}
}
