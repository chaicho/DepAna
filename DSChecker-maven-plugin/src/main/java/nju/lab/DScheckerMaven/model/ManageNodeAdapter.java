package nju.lab.DScheckerMaven.model;

import nju.lab.DScheckerMaven.util.MavenUtil;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.shared.dependency.tree.DependencyNode;

import java.util.ArrayList;
import java.util.List;

/**
 * some depjar may be from dependency management instead of dependency tree.We
 * design ManageNodeAdapter for depJar of this type.
 *
 * @author asus
 *
 */
public class ManageNodeAdapter extends NodeAdapter {
	private String groupId;
	private String artifactId;// artifactId
	private String version;// version
	private String classifier;
	private String type;
	private String scope;
	private Artifact artifact;
	private NodeAdapter beforeManagedNode;
	public ManageNodeAdapter(NodeAdapter nodeAdapter) {
		super(null);
		groupId = nodeAdapter.getGroupId();
		artifactId = nodeAdapter.getArtifactId();
		version = nodeAdapter.getManagedVersion();
		classifier = nodeAdapter.getClassifier();
		type = nodeAdapter.getType();
		scope = nodeAdapter.getManagedScope();
		beforeManagedNode = nodeAdapter;
		this.node = nodeAdapter.getNode();
		if (nodeAdapter.getState() == DependencyNode.OMITTED_FOR_CONFLICT) {
			priority = -1;
		}
		try {
			artifact = MavenUtil.getInstance().getArtifact(getGroupId(), getArtifactId(), getVersion(), getType(),
					getClassifier(), getScope());
			if (!artifact.isResolved()) {
				MavenUtil.getInstance().resolve(artifact);
			}
		} catch (ArtifactResolutionException e) {
			MavenUtil.getInstance().getLog().warn("cant resolve " + this.toString());
		} catch (ArtifactNotFoundException e) {
			MavenUtil.getInstance().getLog().warn("cant resolve " + this.toString());
		}
	}
	@Override
    public String getGroupId() {
		return groupId;
	}
	@Override
	public String getArtifactId() {
		return artifactId;
	}
	@Override
	public String getVersion() {
		return version;
	}
	@Override
	public String getClassifier() {
		return classifier;
	}
	/**
	 * 判断这个节点是否被加载
	 * @return
	 */
	@Override
	public boolean isNodeSelected() {
		return node.getState() == DependencyNode.INCLUDED;// has a probability that node is omitted for conflict with other nodes.
	}
	/**
	 *
	 * @return
	 */
	@Deprecated
	public boolean isVersionSelected() {
		return true;
	}
	/**
	 * 获取被manage之后的version
	 * @return
	 */
	@Override
	public String getManagedVersion() {
		return version;
	}
	/**
	 * 获取这个NodeAdapter在依赖树中的父节点对应的NodeAdapter
	 * @return
	 */
	@Override
	public NodeAdapter getParent() {
		if (null == node.getParent()) {
			return null;
		}
		return NodeAdapters.i().getNodeAdapter(node.getParent());
	}
	@Override
	public String getType() {
		return type;
	}
	@Override
	public String getScope() {
		return scope;
	}
	@Override
	public boolean isVersionChanged() {
		return false;
	}
	@Override
	public List<String> getFilePath() {
		if (filePaths == null) {
			filePaths = new ArrayList<String>();
			if (isInnerProject()) {// inner project is target/classes
				// filePaths = UtilGetter.i().getSrcPaths();
				filePaths.add(MavenUtil.getInstance().getMavenProject(this).getBuild().getOutputDirectory());
			} else {// dependency is repository address
				String path = artifact.getFile().getAbsolutePath();
				filePaths.add(path);
			}
		}
		MavenUtil.getInstance().getLog().debug("node filepath for " + toString() + " : " + filePaths);
		return filePaths;
	}

	public NodeAdapter getBeforeManagedNode() {
		return beforeManagedNode;
	}
	@Override
	public boolean isSelf(DependencyNode node2) {
		return false;
	}
}
