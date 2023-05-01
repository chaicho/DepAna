package nju.lab.DScheckerMaven.util;


import nju.lab.DScheckerMaven.model.NodeAdapter;
import nju.lab.DScheckerMaven.mojos.BaseMojo;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * 涉及Maven相关API使用的工具类
 * @author guoruijie
 */
public class MavenUtil {
	private static class MavenUtilHolder {
		private static final MavenUtil INSTANCE = new MavenUtil();
	}
	private MavenUtil() {
	}
	public static MavenUtil getInstance() {
		return MavenUtilHolder.INSTANCE;
	}
	private Set<String> hostClses;
	/**
	 * get host classes
	 * @return Set<String> hostClses
	 */
	private Set<String> getHostClses() {
		if (hostClses == null) {
			hostClses = new HashSet<String>();
			if (null != this.getSrcPaths()) {
				for (String srcDir : this.getSrcPaths()) {
					hostClses.addAll(SootUtil.getInstance().getJarClasses(srcDir));
				}
			}
		}
		return hostClses;
	}
	/**
	 * verify whether is host class
	 * @param clsSig : class signature
	 * @return boolean
	 */
	public boolean isHostClass(String clsSig) {
		return getHostClses().contains(clsSig);
	}
	private BaseMojo mojo;
	/**
	 * 判断nodeAdapter是否是Host项目
	 * @param nodeAdapter
	 * @return
	 */
	public boolean isInner(NodeAdapter nodeAdapter) {
		return nodeAdapter.isSelf(mojo.project);
		// for (MavenProject mavenProject : mojo.reactorProjects) {
		// if (nodeAdapter.isSelf(mavenProject))
		// return true;
		// }
		// return false;
	}
	/**
	 * 获取nodeAdapter对应的MavenProject数据结构
	 * @param nodeAdapter
	 * @return
	 */
	public MavenProject getMavenProject(NodeAdapter nodeAdapter) {
		for (MavenProject mavenProject : mojo.reactorProjects) {
			if (nodeAdapter.isSelf(mavenProject)) {
				return mavenProject;
			}
		}
		return null;
	}
	public void setMojo(BaseMojo mojo) {
		this.mojo = mojo;
	}
	/**
	 * 将本地缺失的jar包下载下来
	 * @param artifact
	 * @throws ArtifactResolutionException
	 * @throws ArtifactNotFoundException
	 */
	public void resolve(Artifact artifact) throws ArtifactResolutionException, ArtifactNotFoundException {
		long startTime = System.currentTimeMillis();
		mojo.resolver.resolve(artifact, mojo.remoteRepositories, mojo.localRepository);
		long resolveTime = System.currentTimeMillis() - startTime;
	}
	/**
	 * 获取Maven logger
	 * @return
	 */
	public Log getLog() {
		return mojo.getLog();
	}
	/**
	 * 根据指定的groupId, artifactId等信息获取Artifact数据结构
	 * @param groupId
	 * @param artifactId
	 * @param versionRange
	 * @param type
	 * @param classifier
	 * @param scope
	 * @return
	 */
	public Artifact getArtifact(String groupId, String artifactId, String versionRange, String type, String classifier,
			String scope) {
		try {
			return mojo.factory.createDependencyArtifact(groupId, artifactId,
					VersionRange.createFromVersionSpec(versionRange), type, classifier, scope);
		} catch (InvalidVersionSpecificationException e) {
			getLog().error("cant create Artifact!", e);
			return null;
		}
	}
	/**
	 * project info
	 * @return groupId:artifactId:version@filePath
	 */
	public String getProjectInfo() {
		return mojo.project.getGroupId() + ":" + mojo.project.getArtifactId() + ":" + mojo.project.getVersion() + "@"
				+ mojo.project.getFile().getAbsolutePath();
	}
	/**
	 * 获取被测项目名称
	 * @return
	 */
	public String getProjectName() {
		return mojo.project.getName();
	}
	/**
	 * 得到项目pom.xml的位置
	 * @return
	 */
	public String getProjectPom() {
		return mojo.project.getFile().getAbsolutePath();
	}
	/**
	 * @return groupId:artifactId:version
	 */
	public String getProjectCor() {
		return mojo.project.getGroupId() + ":" + mojo.project.getArtifactId() + ":" + mojo.project.getVersion();
	}
	/**
	 * @return groupId
	 */
	public String getProjectGroupId() {
		return mojo.project.getGroupId();
	}
	/**
	 * @return artifactId
	 */
	public String getProjectArtifactId() {
		return mojo.project.getArtifactId();
	}
	/**
	 * @return version
	 */
	public String getProjectVersion() {
		return mojo.project.getVersion();
	}
	/**
	 * @return BaseMojo mojo
	 */
	public BaseMojo getMojo() {
		return mojo;
	}
	/**D:\cWS\eclipse1\testcase.top
	 * @return
	 */
	public File getBaseDir() {
		return mojo.project.getBasedir();
	}
	/**
	 * 获取被测项目target目录
	 * @return
	 */
	public File getBuildDir() {
		return mojo.buildDir;
	}
	/**
	 * get host src path
	 * @return src path
	 */
	public List<String> getSrcPaths() {
		List<String> srcPaths = new ArrayList<String>();
		if(this.mojo==null) {
			return null;
		}
		for (String srcPath : this.mojo.compileSourceRoots) {
			if (new File(srcPath).exists()) {
				srcPaths.add(srcPath);
			}
		}
		return srcPaths;
	}
	/**
	 * get maven local repository
	 * use File.separator
	 * @return local repository
	 */
	public String getMvnRep() {
		return this.mojo.localRepository.getBasedir() + File.separator;
	}
	/**
	 * create maven temp file.
	 * for different operating system.
	 */
	public File createMvnTempScriptFile(String line) {
		File tempFile = null;
		try{
			if (OSinfo.getInstance().isLinux()) {
				tempFile = File.createTempFile("mvncmdFile", ".sh");
				// tempFile = File.createTempFile("mvncmdFile", ".sh");
			}
			else {
				tempFile = File.createTempFile("mvncmdFile", ".bat");
			}
			tempFile.setExecutable(true);
			tempFile.setReadable(true);
			tempFile.setWritable(true);
			tempFile.deleteOnExit();
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			if (OSinfo.getInstance().isLinux()) {
				writer.write("#!/bin/bash\n");
			}
			writer.write(line + '\n');
			writer.close();
			MavenUtil.getInstance().getLog().info("mvn temp File content:" + FileUtils.readFileToString(tempFile));
		}catch (Exception e) { System.err.println("Caught Exception!");
			e.printStackTrace();
		}
		if (tempFile == null) {
			System.err.println("[ERROR] temp file create failed");
		}
		return tempFile;
	}
	/**
	 * 构建Maven虚拟环境（集成相关）
	 * @param projDir
	 * @return
	 */
	@Deprecated
	public boolean createVirtualMavenEnvironment(String projDir) {
		long startTime = System.currentTimeMillis();
		File dotMaven = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("maven-wrapper")).getFile() +
				File.separator + ".mvn");
		File mvnw = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("maven-wrapper")).getFile() +
				File.separator + "mvnw");
		File mvnwcmd = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("maven-wrapper")).getFile() +
				File.separator + "mvnw.cmd");
		File projBase = new File(projDir);
		try {
			FileUtils.copyDirectory(dotMaven, new File(projBase.getAbsolutePath() + File.separator + ".mvn"));
			FileUtils.copyFile(mvnw, new File(projBase.getAbsolutePath() + File.separator + "mvnw"));
			FileUtils.copyFile(mvnwcmd, new File(projBase.getAbsolutePath() + File.separator + "mvnw.cmd"));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		long endTime = System.currentTimeMillis();
		System.out.println(("execute time: " + (endTime - startTime)));
		return true;
	}
	/**
	 * create maven temp file.
	 * for different operating system.
	 */
	public File createMvnTempScriptFileNoLog(String line) {
		File tempFile = null;
		try{
			if (OSinfo.getInstance().isLinux()) {
				tempFile = File.createTempFile("mvncmdFile", ".sh");
				// tempFile = File.createTempFile("mvncmdFile", ".sh");
			}
			else {
				tempFile = File.createTempFile("mvncmdFile", ".bat");
			}
			tempFile.setExecutable(true);
			tempFile.setReadable(true);
			tempFile.setWritable(true);
			tempFile.deleteOnExit();
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			if (OSinfo.getInstance().isLinux()) {
				writer.write("#!/bin/bash\n");
			}
			writer.write(line + '\n');
			writer.close();
			System.err.println("mvn temp File content:" + FileUtils.readFileToString(tempFile));
		}catch (Exception e) { System.err.println("Caught Exception!");
			e.printStackTrace();
		}
		if (tempFile == null) {
			System.err.println("[ERROR] temp file create failed");
		}
		return tempFile;
	}
	/**
	 *
	 * @param root
	 * @param groupId
	 * @param artifactId
	 * @return
	 */
	@Deprecated
	public Set<String> getSubTreeNewDepJarArtifactIds(DependencyNode root, String groupId, String artifactId) {
		Set<String> ret = new HashSet<>();
		class GetTreeJarsVisitor implements DependencyNodeVisitor {
			Set<String> jarArtifactIds = new HashSet<>();
			@Override
			public boolean visit(DependencyNode dependencyNode) {
				String groupId_artifactId = dependencyNode.getArtifact().getGroupId() + ":" + dependencyNode.getArtifact().getArtifactId();
				if ((dependencyNode.getState() != DependencyNode.INCLUDED || dependencyNode.getPremanagedVersion() != null) // omitted version
						&& !jarArtifactIds.contains(groupId_artifactId)) {
					jarArtifactIds.add(groupId_artifactId);
				}
				return true;
			}
			@Override
			public boolean endVisit(DependencyNode dependencyNode) {
				return true;
			}
			public Set<String> getJarArtifactIds() {
				return jarArtifactIds;
			}
		}
		for (DependencyNode child : root.getChildren()) {
			/**
			 * this is the child of the root which version has been changed
			 */
			if (child.getArtifact().getGroupId().equals(groupId) && child.getArtifact().getArtifactId().equals(artifactId)) {
				GetTreeJarsVisitor visitor = new GetTreeJarsVisitor();
				child.accept(visitor);
				ret = visitor.getJarArtifactIds();
				return ret;
			}
		}
		return ret;
	}
	/**
	 * get root's subTree all nodes'
	 * @param root
	 * @param newArtifactSigs
	 * @return
	 */
	public Set<String> getSubTreeNewDepJarArtifactIds(DependencyNode root, List<String> newArtifactSigs) {
		Set<String> ret = new HashSet<>();
		class GetTreeJarsVisitor implements DependencyNodeVisitor {
			Set<String> jarArtifactIds = new HashSet<>();
			@Override
			public boolean visit(DependencyNode dependencyNode) {
				String groupId_artifactId = dependencyNode.getArtifact().getGroupId() + ":" + dependencyNode.getArtifact().getArtifactId();
				if (!jarArtifactIds.contains(groupId_artifactId)) {
					jarArtifactIds.add(groupId_artifactId);
				}
				return true;
			}
			@Override
			public boolean endVisit(DependencyNode dependencyNode) {
				return true;
			}
			public Set<String> getJarArtifactIds() {
				return jarArtifactIds;
			}
		}
		for (DependencyNode child : root.getChildren()) {
			/**
			 * this is the child of the root which version has been changed
			 */
			boolean newConflict = false;
			for (String newArtifactSig : newArtifactSigs) {
				String []arrSplit = newArtifactSig.split(":");
				if (arrSplit.length < 2) {
					continue;
				}
				String groupId = arrSplit[0];
				String artifactId = arrSplit[1];
				if (arrSplit.length == 2) {
					if (child.getArtifact().getGroupId().equals(groupId) &&
							child.getArtifact().getArtifactId().equals(artifactId)) {
						newConflict = true;
						break;
					}
				}
				if (arrSplit.length == 3) {
					String version = arrSplit[2];
					if (child.getArtifact().getGroupId().equals(groupId) &&
							child.getArtifact().getArtifactId().equals(artifactId) &&
							child.getArtifact().getVersion().equals(version)) {
						newConflict = true;
						break;
					}
				}
			}
			/**
			 * one
			 */
			if (newConflict)
			/*if ((child.getArtifact().getGroupId().equals(groupId1) && child.getArtifact().getArtifactId().equals(artifactId1)) ||
					(child.getArtifact().getGroupId().equals(groupId2) && child.getArtifact().getArtifactId().equals(artifactId2))) */{
				GetTreeJarsVisitor visitor = new GetTreeJarsVisitor();
				child.accept(visitor);
				ret.addAll(visitor.getJarArtifactIds());
			}
		}
		return ret;
	}
}
