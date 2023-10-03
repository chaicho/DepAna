package nju.lab.DScheckerMaven.model;

import lombok.extern.slf4j.Slf4j;

import nju.lab.DScheckerMaven.util.ClassifierUtil;
import nju.lab.DScheckerMaven.util.GlobalVar;
import nju.lab.DScheckerMaven.util.MavenUtil;
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
@Slf4j
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
	public DepJar getDepJar() {
		if (depJar == null) {
			depJar = DepJars.i().getDep(this);
		}
		return depJar;
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
			log.warn("cant resolve " + this.toString());
		}
	}

	public boolean isInnerProject() {
		return MavenUtil.getInstance().isInner(this);
	}
	public boolean isSelf(DependencyNode node2) {
		return node.equals(node2);
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
	 * judge whether is self
	 * @return boolean
	 */
	public boolean isSelf(MavenProject mavenProject) {
		return isSelf(mavenProject.getGroupId(), mavenProject.getArtifactId(), mavenProject.getVersion(),
				ClassifierUtil.transformClf(mavenProject.getArtifact().getClassifier()));
	}

	public String getGroupId() {
		return node.getArtifact().getGroupId();
	}
	/**
	 * If the node's scope is reset due to conflict or managed by the dependency management node, return their original scopes.
	 */
	public String getScope() {
		if (null != node.getOriginalScope()) {
			return node.getOriginalScope();
		}
		if (null != node.getPremanagedScope()) {
			return node.getPremanagedScope();
		}
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
	public String getPreManagedScope() {
		return node.getPremanagedScope();
	}
	public String getManagedVersion() {
		return node.getArtifact().getVersion();
	}
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
					log.warn("cant resolve " + this.toString());
				} catch (ArtifactNotFoundException e) {
					log.warn("cant resolve " + this.toString());
				}
			}
		}
		log.debug("node filepath for " + toString() + " : " + filePaths);
		if (GlobalVar.i().isTest && new File(filePaths.get(0)).isDirectory() && !filePaths.get(0).endsWith("test-classes")) {
			List<String> ret= new ArrayList<>();
			ret.add("");
			ret.set(0, filePaths.get(0).substring(0, filePaths.get(0).lastIndexOf("classes")) + "test-classes");
			log.info("ret Nodeadapter" + ret);
			return ret;
		}
		return filePaths;
	}

	public NodeAdapter 	getParent() {
		if (null == node.getParent()) {
			return null;
		}
		return NodeAdapters.i().getNodeAdapter(node.getParent());
	}

	public int getNodeDepth() {
		int depth = 0;
		NodeAdapter father = getParent();
		while (null != father) {
			depth++;
			father = father.getParent();
		}
		return depth;
	}
	@Override
	public String toString() {
		String scope = getScope();
		if (null == scope) {
			scope = "";
		}
		return getGroupId() + ":" + getArtifactId() + ":" + getVersion() + ":" + getClassifier() + ":" + scope + " priority : " + priority;
	}
	public String getDisplayName() {
		String scope = getScope();
		if (null == scope) {
			scope = "";
		}
		return getGroupId() + ":" + getArtifactId() + ":" + getVersion() + ":" + getClassifier()+":" + scope;
	}
	public String getWholePath() {
		StringBuilder sb = new StringBuilder(getDisplayName());
		NodeAdapter father = getParent();
		while (null != father) {
			sb.insert(0, father.getDisplayName() + " -> ");
			father = father.getParent();
		}
		return sb.toString();
	}
}
