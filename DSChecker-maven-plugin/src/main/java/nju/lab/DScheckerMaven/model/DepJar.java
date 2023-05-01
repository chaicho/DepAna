package nju.lab.DScheckerMaven.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.istack.NotNull;
import javassist.ClassPool;
import neu.lab.conflict.GlobalVar;
import neu.lab.conflict.container.AllCls;
import neu.lab.conflict.container.AllRefedCls;
import neu.lab.conflict.container.NodeAdapters;
import neu.lab.conflict.soot.JarAna;
import neu.lab.conflict.util.*;
import org.apache.commons.io.FileUtils;
import soot.jimple.toolkits.callgraph.CallGraph;

import java.io.*;
import java.util.*;

;

/**
 * @author asus
 *
 */
public class DepJar {
	private String groupId;
	private String artifactId;// artifactId
	private String version;// version
	private String classifier;
	private List<String> jarFilePaths;// host project may have multiple source.
	private Map<String, ClassVO> clsTb;// all class in jar
	//private Set<String> clsSigs;// only all class signatures defined in jars
	private Set<String> phantomClsSet;// all phantom classes in jar
	private Set<NodeAdapter> nodeAdapters;// all
	private Set<String> allMthd;
	private Set<String> allCls;
	private Map<String, Collection<String>> allRefedCls;
	private Set<String> allMthdAfterFilter;
	private boolean canUseFilterBuffer = false;
	private Map<String, ClassVO> allClass;// all class in jar
	private int priority;
	private CallGraph jarCg;//与该Cg相关的Cg。
	private CallGraph singleJarCg;//该jar自身的cg
	/**
	 * 初始化
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @param classifier
	 * @param priority
	 * @param jarFilePaths
	 */
	public DepJar(String groupId, String artifactId, String version, String classifier, int priority, List<String> jarFilePaths) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.classifier = classifier;
		this.priority = priority;
		this.jarFilePaths = jarFilePaths;
	}
	/**
	 * 初始化
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @param classifier
	 * @param jarFilePaths
	 */
	public DepJar(String groupId, String artifactId, String version, String classifier, List<String> jarFilePaths) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.classifier = classifier;
		this.jarFilePaths = jarFilePaths;
	}
	public void setCanUseFilterBuffer(boolean canUseFilterBuffer) {
		this.canUseFilterBuffer = canUseFilterBuffer;
	}
	/**
	 * get jar may have risk thinking same class in different dependency,selected jar may have risk;
	 * Not thinking same class in different dependency,selected jar is safe
	 *
	 * @return
	 */
	public boolean isRisk() {
		return !this.isSelected();
	}
	/**
	 *	all class in jar中是不是包含某一class
	 *
	 */
	public boolean containsCls(String clsSig) {
		return this.getAllCls(true).contains(clsSig);
	}
	public Set<NodeAdapter> getNodeAdapters() {
		if (nodeAdapters == null) {
			nodeAdapters = NodeAdapters.i().getNodeAdapters(this);
		}
		return nodeAdapters;
	}
	/**
	 * get all dep Paths
	 * notice : is different from String getAllDepPaths()
	 * kernel is same, format different
	 * @return String path
	 */
	public String getAllDepPath() {
		StringBuilder sb = new StringBuilder(toString() + ":");
		for (NodeAdapter node : getNodeAdapters()) {
			sb.append("  [");
			sb.append(node.getWholePath());
			sb.append("]");
		}
		return sb.toString();
	}
	public String getAllDepPath4Scene6() {
		StringBuilder sb = new StringBuilder(toString() + ":");
		for (NodeAdapter node : getNodeAdapters()) {
			sb.append("  [");
			sb.append(node.getScene6WholePath());
			sb.append("]");
		}
		return sb.toString();
	}
	public List<String> getDepPaths() {
		List<String> paths = new ArrayList<String>();
		for (NodeAdapter node : getNodeAdapters()) {
			paths.add(node.getWholePath());
		}
		return paths;
	}
	/**
	 * get all dep Paths
	 * @author Nos
	 * @return String path
	 */
	public String getAllDepPaths(){
		StringBuilder sb = new StringBuilder(toString() + ":");
		for (NodeAdapter node : getNodeAdapters()) {
			sb.append(node.getWholePath());
			sb.append(";");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	/**
	 * @return the import path of depJar.
	 */
	public String getValidDepPath() {
		StringBuilder sb = new StringBuilder(toString() + ":");
		for (NodeAdapter node : getNodeAdapters()) {
			if (node.isNodeSelected()) {
				sb.append("  [");
				sb.append(node.getWholePath());
				sb.append("]");
			}
		}
		return sb.toString();
	}
	/**
	 * maybe useful
	 * @return
	 */
	public NodeAdapter getSelectedNode() {
		for (NodeAdapter node : getNodeAdapters()) {
			if (node.isNodeSelected()) {
				return node;
			}
		}
		return null;
	}
	/**
	 * @return whether the scope is provided, but this node must be selected
	 */
	public boolean isProvided() {
		for (NodeAdapter node : getNodeAdapters()) {
			if (node.isNodeSelected()) {
				return "provided".equals(node.getScope());
			}
		}
		return false;
	}
	/**
	 * @return whether is selected
	 * 只要
	 */
	public boolean isSelected() {
		for (NodeAdapter nodeAdapter : getNodeAdapters()) {
			if (nodeAdapter.isNodeSelected()) {
				// if manageNodeAdapter, must not version changed. but sometimes not loaded
				if (nodeAdapter instanceof ManageNodeAdapter && nodeAdapter.getPriority() == -1) {
					continue;
				}
				return true;
			}
			// 2022.8.10. modify bug. maybe can do more, reconstruct NodeAdapter code.
			if (nodeAdapter instanceof ManageNodeAdapter && nodeAdapter.getPriority() == 0) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 得到这个jar所有类的集合
	 * TODO modify logic here
	 * @return
	 */
	public Map<String, ClassVO> getClsTbRealTime() {
		if (clsTb == null) {
			if (null == this.getJarFilePaths(true)) {
				// no file
				clsTb = new HashMap<String, ClassVO>(0);
				MavenUtil.getInstance().getLog().warn("can't find jarFile for:" + toString());
			} else {
				if (GlobalVar.i().sootProcess) {
					clsTb = neu.lab.conflict.sootprocess.JarAna.i().deconstruct(this.getJarFilePaths(true));
				}
				else {
					clsTb = JarAna.i().deconstruct(this.getJarFilePaths(true));
				}
				if (clsTb.size() == 0) {
					MavenUtil.getInstance().getLog().warn("get empty clsTb for " + toString());
				}
				for (ClassVO clsVO : clsTb.values()) {
					clsVO.setDepJar(this);
				}
			}
		}
		return clsTb;
	}
	/**
	 * 得到这个jar所有类的集合 polished by grj
	 * @return
	 */
	synchronized public Map<String, ClassVO> getClsTb() {
		/**
		 * TODO add soot Process relevant for getClsTb
		 */
		Map<String, ClassVO> ret = new HashMap<>();
		if (clsTb == null) {
			if (GlobalVar.i().useClsTbBuffer) {
				clsTb = getClsTbWithBuffer();
			} else {
				clsTb = getClsTbRealTime();
			}
		}
		if (clsTb.size() == 0) {
			MavenUtil.getInstance().getLog().warn("get empty clsTb for " + toString());
		}
		return clsTb;
	}
	// if buffer file exists, considered as valid
	private boolean isBufferValidByExist(String bufferFilePath) {
		File bufferFile = new File(bufferFilePath);
		// if buffer File doesn't exist or is not a file, not valid.
		if (!bufferFile.exists() || !bufferFile.isFile()) {
			return false;
		}
		return true;
	}
	/**
	 * 缓冲区是有效
	 * 暂时不能删，可能有用if buffer file exists, considered as valid
	 * @param bufferFilePath 缓冲文件路径
	 * @return boolean
	 */
	private boolean isBufferValid(String bufferFilePath) {
		File bufferFile = new File(bufferFilePath);
		// if buffer File doesn't exist or is not a file, not valid.
		if (!bufferFile.exists() || !bufferFile.isFile()) {
			return false;
		}
		return true;
	}
	/**
	 * 缓冲区是有效时间吗
	 *暂时不能删，可能有用，compare jar file generate time and jarHash file generate file to decide if buffer valid
	 * @param jarFilePath    jar文件路径
	 * @param bufferFilePath 缓冲文件路径
	 * @return boolean
	 */
	private boolean isBufferValidByTime(String jarFilePath, String bufferFilePath) {
		File bufferFile = new File(bufferFilePath);
		// if buffer File doesn't exist or is not a file, not valid.
		if (!bufferFile.exists() || !bufferFile.isFile()) {
			return false;
		}
		File jarFile = new File(jarFilePath);
		// if buffer File generated later than jar file, considered as valid
		if (jarFile.lastModified() < bufferFile.lastModified()) {
			return true;
		}
		return false;
	}
	/**
	 * 缓冲有效散列吗
	 * 暂时不能删，可能有用，
	 * @param jarFilePath     jar文件路径
	 * @param bufferFilePath  缓冲文件路径
	 * @param jarHashFilePath jar散列文件路径
	 * @return boolean
	 * compare jar file generate time and buffer file generate time's jar hash to decide if buffer valid
	 */
	private boolean isBufferValidByHash(String jarFilePath, String bufferFilePath, String jarHashFilePath) {
		File bufferFile = new File(bufferFilePath);
		// if buffer File doesn't exist or is not a file, not valid.
		if (!bufferFile.exists() || !bufferFile.isFile()) {
			MavenUtil.getInstance().getLog().error(bufferFile + "is not a File!!");
			return false;
		}
		File jarFile = new File(jarFilePath);
		// if buffer File generated later than jar file, considered as valid
		File hashFile = new File(jarHashFilePath);
		// get store SHA from file
		String storeSHA1 = "";
		try {
			storeSHA1 = FileUtils.readFileToString(hashFile);
		}
		catch (Exception e) { System.err.println("Caught Exception!");
			e.printStackTrace();
		}
		// get jar File's sha1
		String nowSHA1 = FileUtil.getInstance().getFileSHA1(jarFile);
		// if hashcode donot match, buffer invalid
		if (nowSHA1.equals(storeSHA1)) {
			return true;
		}
		return false;
	}
	/**
	 * 得到这个jar所有类的集合 polished by grj
	 * @return
	 */
	synchronized public Map<String, ClassVO> getClsTbWithBuffer() {
		/**
		 * TODO add soot Process relevant for getClsTb
		 */
		// if jar is host classes or do not use ClsTbBuffer, getClsTb RealTime
		if (isHost()) {
			return getClsTbRealTime();
		}
		// else, check if there is a buffer, read buffer and set Cls Tb
		if (clsTb == null) {
			if (null == this.getJarFilePaths(true)) {
				// no file
				clsTb = new HashMap<String, ClassVO>(0);
				MavenUtil.getInstance().getLog().warn("can't find jarFile for:" + toString());
			} else {
				String jarFilePath = this.getJarFilePaths(true).get(0);
				File jarClsBufferFile = new File(getClsTbBufferFilePath());
				// check if buffer file valid
				if (!GlobalVar.i().updateClsTbBuffer && isBufferValidByExist(/*jarFilePath, */getClsTbBufferFilePath())) {
					try {
						String gsonClsTbStr;
						gsonClsTbStr = FileUtils.readFileToString(jarClsBufferFile);
						Set<GsonClassVO> gsonClsTb = null;
						gsonClsTb = new Gson().fromJson(gsonClsTbStr, new TypeToken<HashSet<GsonClassVO>>() {
						}.getType());
						if (gsonClsTb != null) {
							clsTb = new HashMap<>();
							for (GsonClassVO gsonClsVO : gsonClsTb) {
								ClassVO clsVO = new ClassVO(gsonClsVO.getClsSig());
								for (GsonMethodVO gsonMthdVO : gsonClsVO.getMthds()) {
									MethodVO mthdVO = new MethodVO(gsonMthdVO.getMthdSig(), clsVO);
									mthdVO.setInMthds(gsonMthdVO.getInMthds());
									clsVO.addMethod(mthdVO);
								}
								clsTb.put(clsVO.getClsSig(), clsVO);
								clsVO.setDepJar(this);
							}
						}
						if (clsTb != null) {
							return clsTb;
						}
					} catch (IOException e) { System.err.println("Caught Exception!");
						e.printStackTrace();
					}
				}
				clsTb = getClsTbRealTime();// if clsTb got failed from buffer, then get it real time
				// write clsTb into buffer! generate buffer
				if (clsTb.size() == 0) {
					MavenUtil.getInstance().getLog().warn("get empty clsTb for " + toString());
				}
				// generate buffer and write to file
				Set<GsonClassVO> gsonClsTb = new HashSet<>();
				for (ClassVO clsVO : clsTb.values()) {
					Set<GsonMethodVO> gsonMthdsVOs = new HashSet<GsonMethodVO>();
					for (MethodVO mthdVO : clsVO.getMthds()) {
						gsonMthdsVOs.add(new GsonMethodVO(mthdVO.getMthdSig(), mthdVO.getInMthds()));
					}
					GsonClassVO tmp = new GsonClassVO(clsVO.getClsSig(), gsonMthdsVOs);
					gsonClsTb.add(tmp);
					clsVO.setDepJar(this);
				}
				try {
					Writer writer = new BufferedWriter(new FileWriter(jarClsBufferFile));
					writer.write(new Gson().toJson(gsonClsTb));
					writer.close();
				} catch (IOException e) { System.err.println("Caught Exception!");
					e.printStackTrace();
				}
			}
		}
		return clsTb;
	}
	/**
	 * 得到这个jar所有类的集合 polished by grj
	 * Now : do not use allCls to analyze. ClsTb is clsTb, do not relevant to other procedure.
	 * TODO : further combine to update the efficiency
	 * @return
	 */
	public Map<String, ClassVO> getClsTbWithBufferSootProcess() {
		/**
		 * if clsTb has been computed, return.
		 */
		if (clsTb != null) {
			return clsTb;
		}
		/**
		 * check if jarFilePaths = null. if no jar Path, cannot compute
		 */
		if (null == this.getJarFilePaths(true)) {
			// no file
			clsTb = new HashMap<String, ClassVO>(0);
			MavenUtil.getInstance().getLog().warn("can't find jarFile for:" + toString());
		}
		/**
		 * if is Host(target/classes always new, do not use buffer; if user specify not using buffer, do not use buffer
		 */
		if (isHost() || !GlobalVar.i().useClsTbBuffer) {
			/**
			 * use process to deconstruct
			 */
			clsTb = neu.lab.conflict.sootprocess.JarAna.i().deconstruct(this.getJarFilePaths(true));
			if (clsTb.size() == 0) {
				MavenUtil.getInstance().getLog().warn("get empty clsTb for " + toString());
			}
			for (ClassVO clsVO : clsTb.values()) {
				clsVO.setDepJar(this);
			}
			return clsTb;
		}
		/** if user want to use buffer
		 *
		 */
		if (!this.isHost()) {
			String jarFilePath = this.getJarFilePaths(true).get(0);
			File jarClsBufferFile = new File(getClsTbBufferFilePath());
			boolean readBufferDone = false;
			if (jarClsBufferFile.exists()) { //如果有缓存文件
				//检查该缓存文件是不是最新的。如果是最新的，pom文件里应有我们打的标记
				try {
					File jarRemoteFile; //new File(jarFilePath.substring(0, jarFilePath.lastIndexOf(File.separator) + 1) + "_remote.repositories");
					jarRemoteFile = new File(jarFilePath);
					if (jarRemoteFile.lastModified() < jarClsBufferFile.lastModified()) { //如果安装时间早于缓存更新时间，缓存有效
						String gsonClsTbStr;
						gsonClsTbStr = FileUtils.readFileToString(jarClsBufferFile);
						Set<GsonClassVO> gsonClsTb = null;
						gsonClsTb = new Gson().fromJson(gsonClsTbStr, new TypeToken<HashSet<GsonClassVO>>() {
						}.getType());
						if (gsonClsTb != null) {
							clsTb = new HashMap<>();
							for (GsonClassVO gsonClsVO : gsonClsTb) {
								ClassVO clsVO = new ClassVO(gsonClsVO.getClsSig());
								for (GsonMethodVO gsonMthdVO : gsonClsVO.getMthds()) {
									MethodVO mthdVO = new MethodVO(gsonMthdVO.getMthdSig(), clsVO);
									mthdVO.setInMthds(gsonMthdVO.getInMthds());
									clsVO.addMethod(mthdVO);
								}
								clsTb.put(clsVO.getClsSig(), clsVO);
								clsVO.setDepJar(this);
							}
						}
					}
					readBufferDone = true;
					if (clsTb != null) {
						return clsTb;
					}
				} catch (IOException e) { System.err.println("Caught Exception!");
					e.printStackTrace();
				}
			} else {
				FileUtil.getInstance().createNewFile(jarClsBufferFile);
			}
			clsTb = neu.lab.conflict.sootprocess.JarAna.i().deconstruct(this.getJarFilePaths(true)); // sootprocess
			if (clsTb.size() == 0) {
				MavenUtil.getInstance().getLog().warn("get empty clsTb for " + toString());
			}
			Set<GsonClassVO> gsonClsTb = new HashSet<>();
			for (ClassVO clsVO : clsTb.values()) {
				Set<GsonMethodVO> gsonMthdsVOs = new HashSet<GsonMethodVO>();
				for (MethodVO mthdVO : clsVO.getMthds()) {
					gsonMthdsVOs.add(new GsonMethodVO(mthdVO.getMthdSig(), mthdVO.getInMthds()));
				}
				GsonClassVO tmp = new GsonClassVO(clsVO.getClsSig(), gsonMthdsVOs);
				gsonClsTb.add(tmp);
				clsVO.setDepJar(this);
			}
			try {
				Writer writer = new BufferedWriter(new FileWriter(jarClsBufferFile));
				writer.write(new Gson().toJson(gsonClsTb));
				writer.close();
			} catch (IOException e) { System.err.println("Caught Exception!");
				e.printStackTrace();
			}
		}
		return clsTb;
	}
	public Map<String, ClassVO> getClsTb(boolean useBuffer) {
		if (!useBuffer) {
			if (clsTb == null) {
				if (null == this.getJarFilePaths(true)) {
					// no file
					clsTb = new HashMap<String, ClassVO>(0);
					MavenUtil.getInstance().getLog().warn("can't find jarFile for:" + toString());
				} else {
					if (GlobalVar.i().combineClsTbWithPhantom) {
						JarAna.i().deconstructAllCls(this, this.getJarFilePaths(true), this.allCls);
					}
					else {
						clsTb = JarAna.i().deconstruct(this.getJarFilePaths(true));
					}
					if (clsTb.size() == 0) {
						MavenUtil.getInstance().getLog().warn("get empty clsTb for " + toString());
					}
					for (ClassVO clsVO : clsTb.values()) {
						clsVO.setDepJar(this);
					}
				}
			}
			return clsTb;
		}
		if (clsTb == null) {
			if (null == this.getJarFilePaths(true)) {
				// no file
				clsTb = new HashMap<String, ClassVO>(0);
				MavenUtil.getInstance().getLog().warn("can't find jarFile for:" + toString());
			} else {
				if (!this.isHost()) {
					String jarFilePath = this.getJarFilePaths(true).get(0);
				/*String jarClsBufferFilePath;
				if (jarFilePath.endsWith(".jar"))
					 jarClsBufferFilePath = jarFilePath.substring(0, jarFilePath.lastIndexOf('.')) + ".cbf";
				else jarClsBufferFilePath = jarFilePath + ".cbf";*/
					File jarClsBufferFile = new File(getAllClsBufferDir() + this.artifactId + ".cbf");
					boolean readBufferDone = false;
					if (jarClsBufferFile.exists()) { //如果有缓存文件
						//检查该缓存文件是不是最新的。如果是最新的，pom文件里应有我们打的标记
						try {
							File jarRemoteFile = new File(jarFilePath.substring(0, jarFilePath.lastIndexOf(File.separator) + 1) + "_remote.repositories");
							if (jarRemoteFile.lastModified() < jarClsBufferFile.lastModified()) { //如果安装时间早于缓存更新时间，缓存有效
								String gsonClsTbStr;
								gsonClsTbStr = FileUtils.readFileToString(jarClsBufferFile);
								if (gsonClsTbStr.startsWith("[")) { //if the file is empty, then some fault happens because the cbf didn't generate
									clsTb = new HashMap<>();
									Set<GsonClassVO> gsonClsTb = new HashSet<>();
									gsonClsTb = new Gson().fromJson(gsonClsTbStr, new TypeToken<HashSet<GsonClassVO>>() {
									}.getType());
									if (gsonClsTb == null) {
										return clsTb;
									}
									for (GsonClassVO gsonClsVO : gsonClsTb) {
										ClassVO clsVO = new ClassVO(gsonClsVO.getClsSig());
										for (GsonMethodVO gsonMthdVO : gsonClsVO.getMthds()) {
											MethodVO mthdVO = new MethodVO(gsonMthdVO.getMthdSig(), clsVO);
											mthdVO.setInMthds(gsonMthdVO.getInMthds());
											clsVO.addMethod(mthdVO);
										}
										clsTb.put(clsVO.getClsSig(), clsVO);
										clsVO.setDepJar(this);
									}
								}
							}
							readBufferDone = true;
							if (clsTb != null) {
								return clsTb;
							}
						} catch (IOException e) { System.err.println("Caught Exception!");
							e.printStackTrace();
						}
					} else {
						FileUtil.getInstance().createNewFile(jarClsBufferFile);
					}
					if (allCls == null) {
						allCls = getAllCls(true);
					}
					/**
					 * deconstruct all
					 */
					if (GlobalVar.i().combineClsTbWithPhantom) {
						JarAna.i().deconstructAllCls(this, this.getJarFilePaths(true), allCls);
						writePhantomClassesToBuffer(phantomClsSet);
					}
					else {
						clsTb = JarAna.i().deconstruct(this.getJarFilePaths(true), allCls);
					}
					if (clsTb.size() == 0) {
						MavenUtil.getInstance().getLog().warn("get empty clsTb for " + toString());
					}
					Set<GsonClassVO> gsonClsTb = new HashSet<>();
					for (ClassVO clsVO : clsTb.values()) {
						Set<GsonMethodVO> gsonMthdsVOs = new HashSet<GsonMethodVO>();
						for (MethodVO mthdVO : clsVO.getMthds()) {
							gsonMthdsVOs.add(new GsonMethodVO(mthdVO.getMthdSig(), mthdVO.getInMthds()));
						}
						GsonClassVO tmp = new GsonClassVO(clsVO.getClsSig(), gsonMthdsVOs);
						gsonClsTb.add(tmp);
						clsVO.setDepJar(this);
					}
					try {
						Writer writer = new BufferedWriter(new FileWriter(jarClsBufferFile));
						writer.write(new Gson().toJson(gsonClsTb));
						writer.close();
					} catch (IOException e) { System.err.println("Caught Exception!");
						e.printStackTrace();
					}
				}
			}
		}
		return clsTb;
	}
	/**
	 * Directly use soot to get phantom classes signatures.
	 * @return
	 */
	public Set<String> getPhantomClassesSigs() {
		if (phantomClsSet == null) {
			if (null == this.getJarFilePaths(true)) {
				// no file
				phantomClsSet = new HashSet<>();
				MavenUtil.getInstance().getLog().warn("can't find jarFile for:" + toString());
			} else {
				phantomClsSet = JarAna.i().deconstructPhantom(this.getJarFilePaths(true));
				if (phantomClsSet.size() == 0) {
					MavenUtil.getInstance().getLog().warn("get empty phantom classes set for " + toString());
				}
			}
		}
		return phantomClsSet;
	}
	/**
	 * get All Phantom Classes' Signatures with Buffer. If no buffer, use soot to analyze jars and then create buffer.
	 * By grj
	 * @return
	 */
	public Set<String> getPhantomClassesSigsWithBuffer() {
		if (phantomClsSet == null) {
			if (null == this.getJarFilePaths(true)) {
				// no file
				phantomClsSet = new HashSet<>();
				MavenUtil.getInstance().getLog().warn("can't find jarFile for:" + toString());
			} else {
				String jarFilePath = this.getJarFilePaths(true).get(0);
				File jarPhantomClsBufferFile = new File(getAllClsBufferDir() + this.artifactId + "_phantom.txt");
				if (jarPhantomClsBufferFile.exists()) { //如果有缓存文件
					//检查该缓存文件是不是最新的。如果是最新的，pom文件里应有我们打的标记
					try {
						File jarRemoteFile = new File(jarFilePath.substring(0, jarFilePath.lastIndexOf(File.separator) + 1) + "_remote.repositories");
						if (jarRemoteFile.lastModified() < jarPhantomClsBufferFile.lastModified()) { //如果安装时间早于缓存更新时间，缓存有效
							String gsonPhantomClsSetStr;
							gsonPhantomClsSetStr = FileUtils.readFileToString(jarPhantomClsBufferFile);
							phantomClsSet = new Gson().fromJson(gsonPhantomClsSetStr, new TypeToken<HashSet<String>>() {
							}.getType());
						}
						if (phantomClsSet != null) {
							return phantomClsSet;
						}
					} catch (IOException e) { System.err.println("Caught Exception!");
						e.printStackTrace();
					}
				}
				else {
					FileUtil.getInstance().createNewFile(jarPhantomClsBufferFile);
				}
				phantomClsSet = JarAna.i().deconstructPhantom(this.getJarFilePaths(true));
				if (phantomClsSet.size() == 0) {
					MavenUtil.getInstance().getLog().warn("get empty phantom classes set for " + toString());
				}
				try {
					Writer writer = new BufferedWriter(new FileWriter(jarPhantomClsBufferFile));
					writer.write(new Gson().toJson(phantomClsSet));
					writer.close();
				}
				catch (IOException e) { System.err.println("Caught Exception!");
					e.printStackTrace();
				}
			}
		}
		return phantomClsSet;
	}
	/**
	 * 得到这个jar所有类的集合顺道获取所有phantom Classes
	 * @return
	 */
	public void writePhantomClassesToBuffer(@NotNull Set<String> phantomClsSet) {
		File jarPhantomClsBufferFile = new File(getAllClsBufferDir() + this.artifactId + "_phantom.txt");
		try {
			Writer writer = new BufferedWriter(new FileWriter(jarPhantomClsBufferFile));
			writer.write(new Gson().toJson(phantomClsSet));
			writer.close();
		}
		catch (IOException e) { System.err.println("Caught Exception!");
			e.printStackTrace();
		}
	}
	public Map<String, ClassVO> getClsTbWithPhantomClasses() {
		if (clsTb == null) {
			if (null == this.getJarFilePaths(true)) {
				// no file
				clsTb = new HashMap<String, ClassVO>(0);
				MavenUtil.getInstance().getLog().warn("can't find jarFile for:" + toString());
			} else {
				String jarFilePath = this.getJarFilePaths(true).get(0);
				File jarClsBufferFile = new File(getClsTbBufferFilePath());
				if (jarClsBufferFile.exists()) { //如果有缓存文件
					//检查该缓存文件是不是最新的。如果是最新的，pom文件里应有我们打的标记
					try {
						File jarRemoteFile = new File(jarFilePath.substring(0, jarFilePath.lastIndexOf(File.separator) + 1) + "_remote.repositories");
						if (jarRemoteFile.lastModified() < jarClsBufferFile.lastModified()) { //如果安装时间早于缓存更新时间，缓存有效
							clsTb = new HashMap<>();
							Set<GsonClassVO> gsonClsTb = new HashSet<>();
							String gsonClsTbStr;
							gsonClsTbStr = FileUtils.readFileToString(jarClsBufferFile);
							gsonClsTb = new Gson().fromJson(gsonClsTbStr, new TypeToken<HashSet<GsonClassVO>>() {
							}.getType());
							for (GsonClassVO gsonClsVO : gsonClsTb) {
								ClassVO clsVO = new ClassVO(gsonClsVO.getClsSig());
								for (GsonMethodVO gsonMthdVO : gsonClsVO.getMthds()) {
									MethodVO mthdVO = new MethodVO(gsonMthdVO.getMthdSig(), clsVO);
									mthdVO.setInMthds(gsonMthdVO.getInMthds());
									clsVO.addMethod(mthdVO);
								}
								clsTb.put(clsVO.getClsSig(), clsVO);
								clsVO.setDepJar(this);
							}
						}
						if (clsTb != null) {
							return clsTb;
						}
					} catch (IOException e) { System.err.println("Caught Exception!");
						e.printStackTrace();
					}
				}
				if (allCls == null) {
					allCls = getAllCls(true);
				}
				/**
				 * set clsTb and also phantomClasses
				 */
				clsTb = JarAna.i().deconstructAllCls(this, this.getJarFilePaths(true), allCls);
				if (clsTb.size() == 0) {
					MavenUtil.getInstance().getLog().warn("get empty clsTb for " + toString());
				}
				Set<GsonClassVO> gsonClsTb = new HashSet<>();
				for (ClassVO clsVO : clsTb.values()) {
					Set<GsonMethodVO> gsonMthdsVOs = new HashSet<GsonMethodVO>();
					for (MethodVO mthdVO : clsVO.getMthds()) {
						gsonMthdsVOs.add(new GsonMethodVO(mthdVO.getMthdSig(), mthdVO.getInMthds()));
					}
					GsonClassVO tmp = new GsonClassVO(clsVO.getClsSig(), gsonMthdsVOs);
					gsonClsTb.add(tmp);
					clsVO.setDepJar(this);
				}
				try {
					Writer writer = new BufferedWriter(new FileWriter(jarClsBufferFile));
					writer.write(new Gson().toJson(gsonClsTb));
					writer.close();
				}
				catch (IOException e) { System.err.println("Caught Exception!");
					e.printStackTrace();
				}
			}
		}
		return clsTb;
	}
	public void setPhantomClsSet(Set<String> phantomClsSet) {
		this.phantomClsSet = phantomClsSet;
	}
	public Set<String> getClsSigs() {
		if (allCls == null) {
			if (clsTb != null) {
				allCls = clsTb.keySet();
			} else {
				allCls = SootUtil.getInstance().getJarsClasses(this.getJarFilePaths(true));
			}
		}
		return allCls;
	}
	/**
	 * 在另一depJar里找到了本Jar某个phantom Class的定义
	 * By grj
	 */
	public boolean hasPhantomDefInJar(DepJar depJar) {
		for (String sig : getPhantomClassesSigsWithBuffer()) {
			if (depJar.getAllCls(true).contains(sig)) {
				return true;
			}
		}
		return false;
	}
	public ClassVO getClassVO(String clsSig) {
		return getClsTb().get(clsSig);
	}
	/**
	 * 得到这个jar的所有方法
	 * @return
	 */
	public Set<String> getAllMthd() {
		if (allMthd == null) {
			allMthd = new HashSet<String>();
			/**
			 * add this synchronize for multiprocess get callgraph
			 */
			synchronized (this){
				for (ClassVO cls : getClsTb().values()) {
					for (MethodVO mthd : cls.getMthds()) {
						allMthd.add(mthd.getMthdSig());
					}
				}
			}
		}
		return allMthd;
	}
	public Set<String> getUsedMthds() {
		Set<String> allMethods = getAllMthd();
		Set<String> usedMethods = new HashSet<>();
		for (String method : allMethods) {
			if (AllRefedCls.i().contains(SootUtil.getInstance().mthdSig2cls(method))) {
				usedMethods.add(method);
			}
		}
		return usedMethods;
	}
	public boolean containsMthd(String mthd) {
		return getAllMthd().contains(mthd);
	}
	/**
	 * 得到本depjar独有的cls
	 * @param otherJar
	 * @return
	 */
	public Set<String> getOnlyClses(DepJar otherJar) {
		Set<String> onlyCls = new HashSet<String>();
		Set<String> otherAll = otherJar.getAllCls(true);
		for (String clsSig : getAllCls(true)) {
			if (!otherAll.contains(clsSig)) {
				onlyCls.add(clsSig);
			}
		}
		return onlyCls;
	}
	/**
	 * 得到本depjar独有的mthds
	 * @param otherJar
	 * @return
	 */
	public Set<String> getOnlyMthds(DepJar otherJar) {
		Set<String> onlyMthds = new HashSet<String>();
		for (String clsSig : getClsTb().keySet()) {
			ClassVO otherCls = otherJar.getClassVO(clsSig);
			if (otherCls != null) {
				ClassVO cls = getClassVO(clsSig);
				for (MethodVO mthd : cls.getMthds()) {
					if (!otherCls.hasMethod(mthd.getMthdSig())) {
						onlyMthds.add(mthd.getMthdSig());
					}
				}
			}
		}
		return onlyMthds;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DepJar) {
			return isSelf((DepJar) obj);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return groupId.hashCode() * 31 * 31 + artifactId.hashCode() * 31 + version.hashCode()
				+ classifier.hashCode() * 31 * 31 * 31;
	}
	/**
	 * @return groupId:artifactId:version:classifier
	 */
	@Override
	public String toString() {
		return groupId + ":" + artifactId + ":" + version + ":" + classifier;
	}
	/**
	 * @return groupId:artifactId:version
	 */
	public String getSig() {
		return groupId + ":" + artifactId + ":" + version;
	}
	/**
	 * @return groupId:artifactId
	 */
	public String getName() {
		return groupId + ":" + artifactId;
	}
	/**
	 * @return groupId
	 */
	public String getGroupId() {
		return groupId;
	}
	/**
	 * @return artifactId
	 */
	public String getArtifactId() {
		return artifactId;
	}
	/**
	 * @return version
	 */
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	/**
	 * @return classifier
	 */
	public String getClassifier() {
		return classifier;
	}
	/**
	 * whether is the same deoJar
	 * @param groupId2 : 目标groupId
	 * @param artifactId2 : 目标artifactId
	 * @param version2 : 目标version
	 * @param classifier2 : 目标classifier
	 * @return boolean
	 */
	public boolean isSame(String groupId2, String artifactId2, String version2, String classifier2) {
		return groupId.equals(groupId2) && artifactId.equals(artifactId2) && version.equals(version2)
				&& classifier.equals(classifier2);
	}
	/**
	 * 是否为同一个
	 * @param dep
	 * @return
	 */
	public boolean isSelf(DepJar dep) {
		return isSame(dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), dep.getClassifier());
	}
	/**
	 * 没有比较版本
	 * @param depJar
	 * @return
	 */
	public boolean isSameLib(DepJar depJar) {
		return getGroupId().equals(depJar.getGroupId()) && getArtifactId().equals(depJar.getArtifactId());
	}
	/**
	 * 没有比较Classifier, 比较groupId, artifactId, version
	 * @param depJar : 待比较的jar
	 * @return boolean
	 */
	public boolean isSameJarIgnoreClassifier(DepJar depJar) {
		return getGroupId().equals(depJar.getGroupId()) && getArtifactId().equals(depJar.getArtifactId())
				&& getVersion().equals(depJar.getVersion());
	}
	public void setClsTb(Map<String, ClassVO> clsTb) {
		this.clsTb = clsTb;
	}
	/**
	 *
	 * all class in jar中是不是包含某一class
	 */
	public boolean containClass(String classSig) {
		return this.getAllClass().containsKey(classSig);
	}
	/**
	 * 得到这个jar所有类的集合
	 *
	 * @return
	 */
	public Map<String, ClassVO> getAllClass() {
		if (allClass == null) {
			if (null == this.getJarFilePaths(true)) {
				// no file
				allClass = new HashMap<String, ClassVO>(0);
				MavenUtil.getInstance().getLog().warn("can't find jarFile for:" + toString());
			} else {
				allClass = JarAna.i().deconstruct(this.getJarFilePaths(true));
				if (allClass.size() == 0) {
					MavenUtil.getInstance().getLog().warn("get empty allClass for " + toString());
				}
				for (ClassVO clsVO : allClass.values()) {
					clsVO.setDepJar(this);
				}
			}
		}
		return allClass;
	}
	public Set<String> getRiskClasses(Collection<String> entryClasses) {
		Set<String> riskClasses = new HashSet<String>();
		for (String cls : entryClasses) {
			if (!this.containClass(cls)) {
				riskClasses.add(cls);
			}
		}
		return riskClasses;
	}
	/**
	 * note:from the view of usedJar. e.g.
	 * getReplaceJar().getRiskMthds(getRchedMthds());
	 *
	 * @param testMthds
	 * @return
	 */
	public Set<String> getRiskMthds(Collection<String> testMthds) {
		Set<String> riskMthds = new HashSet<String>();
		System.out.println("DepJar.getRiskMthds begin");
		for (String testMthd : testMthds) {
			if (!this.containsMthd(testMthd) && (
					(GlobalVar.i().refReachable && AllRefedCls.iReachable().contains(SootUtil.getInstance().mthdSig2cls(testMthd))) ||
							(!GlobalVar.i().refReachable && AllRefedCls.i().contains(SootUtil.getInstance().mthdSig2cls(testMthd)))
			)
			) {
				// don't have method,and class is used. 使用这个类，但是没有方法
				if (this.containsCls(SootUtil.getInstance().mthdSig2cls(testMthd))) {
					// has class.don't have method.	有这个类，没有方法
					riskMthds.add(testMthd);
				} else if (!AllCls.i().contains(SootUtil.getInstance().mthdSig2cls(testMthd))) {
					// This jar don't have class,and all jar don't have class.	这个jar没有这个class，所有加载的jar都没有
					riskMthds.add(testMthd);
				}
			}
		}
		System.out.println("DepJar.getRiskMthds end");
		return riskMthds;
	}
	/**
	 * return refed classes
	 *
	 * @param testClasses
	 * @return
	 */
	public Set<String> getRiskRefedClasses(Collection<String> testClasses) {
		Set<String> riskClasses = new HashSet<String>();
		System.out.println("DepJar.getRiskClasses begin, class detect ref reachable:" + GlobalVar.i().classDetectRefReachable);
		for (String testClass : testClasses) {
			/**
			 * never open refReachable(has bug)
			 */
			if (!this.containsCls(testClass) &&
					((!GlobalVar.i().classDetectRefReachable && AllRefedCls.iNotReachable().contains(testClass)) ||
							(GlobalVar.i().classDetectRefReachable && AllRefedCls.iReachable().contains(testClass))
			)) {
				if (!AllCls.i().contains(testClass)) {
					riskClasses.add(testClass);
				}
			}
		}
		System.out.println("DepJar.getRiskClasses end");
		return riskClasses;
	}
	synchronized public Set<String>  getAllCls(boolean useTarget) {
		if (allCls == null) {
			if (!GlobalVar.i().useAllClsBuffer) {
				return getAllClsRealTime(useTarget);
			} else {
				return getAllClsWithBuffer(useTarget);
			}
		}
		return allCls;
	}
	synchronized public Set<String> getAllClsRealTime(boolean useTarget) {
		if (allCls == null) {
			/**
			 * get AllCls or
			 */
			if (GlobalVar.i().sootProcess) {
				allCls = SootProcessUtil.getInstance().getJarsClasses(this.getJarFilePaths(useTarget));
			}
			/**
			 * get allCls with process
			 */
			else {
				allCls = SootUtil.getInstance().getJarsClasses(this.getJarFilePaths(useTarget)); // TODO
			}
		}
		//System.out.println("allCls:" + allCls);
		return allCls;
	}
	synchronized public Set<String> getAllClsWithBuffer(boolean useTarget) {
		if (allCls == null) {
			if (isHost()) {
				return getAllClsRealTime(true);
			}
			if (!GlobalVar.i().updateAllClsBuffer && isBufferValidByExist(/*getJarFilePath(), */getAllClsBufferFilePath())) {
				try {
					allCls = new Gson().fromJson(FileUtils.readFileToString(this.getAllClsBufferFile()),
							new TypeToken<HashSet<String>>(){}.getType());
					if (allCls == null) {
						allCls = getAllClsRealTime(true);
						if (!isHost()) {
							FileUtil.getInstance().writeGson(this.getAllClsBufferFile(), allCls);
						}
						MavenUtil.getInstance().getLog().error("Invalid allcls buffer file");
					}
				}
				catch (Exception e) { System.err.println("Caught Exception!");
					allCls = getAllClsRealTime(true);
					System.err.println("AllClsBufferFile null" + this.getAllClsBufferFile());
					if (!isHost()) {
						FileUtil.getInstance().writeGson(this.getAllClsBufferFile(), allCls);
					}
					MavenUtil.getInstance().getLog().error("get all class buffer failed.");
					e.printStackTrace();
				}
			}
			else {
				allCls = getAllClsRealTime(true);
				if (!isHost()) {
					FileUtil.getInstance().writeGson(this.getAllClsBufferFile(), allCls);
				}
			}
		}
		return allCls;
	}
	/** get refed classes directly from jarFiles*/
	private Map<String, Collection<String>> getRefedClsRealTime(){
		Map<String, Collection<String>> ret = new HashMap<>();
		try {
			ClassPool pool = new ClassPool();
			pool.appendClassPath(this.getJarFilePath());
			for (String cls : this.getAllCls(true)) {
				if (pool.getOrNull(cls) != null) {
					if (!ret.containsKey(cls)) {
						ret.put(cls, new LinkedList<>());
					}
					for (String refedCls : pool.get(cls).getRefClasses()) {
						if (!SootUtil.getInstance().isJavaLibraryClass(refedCls)) {
							ret.get(cls).add(refedCls);
						}
					}
				} else {
					MavenUtil.getInstance().getLog().warn("can't find " + cls + " in pool when form reference.");
				}
			}
		}
		catch (Exception e) { System.err.println("Caught Exception!");
			MavenUtil.getInstance().getLog().error("get refed classes error for jar:" + this.getSig() + e);
		}
		return ret;
	}
	class RefedClsVO {
		String cN;// className. class total name. neu.lab.A.B......
		Collection<String> rC; // refed classes	`
		public void setClassName(String className) {
			this.cN = className;
		}
		public void setRefedClasses(Collection<String> refedClasses) {
			this.rC = refedClasses;
		}
		public Collection<String> getRefedClasses() {
			return rC;
		}
		public String getClassName() {
			return cN;
		}
	}
	public Map<String, Collection<String>> getRefedCls() {
		if (allRefedCls == null) {
			if (!GlobalVar.i().useRefedClsBuffer) {
				return getRefedClsRealTime();
			}
			else {
				return getRefedClsWithBuffer();
			}
		}
		return allRefedCls;
	}
	/**
	 * if Test, getRefedCls may only get test-classes' refed classes.
	 * use this method to get classes' refed classes for Host DepJar
	 * @return
	 */
	public Map<String, Collection<String>> getClassesRefedClsWhenTest() {
		Map<String, Collection<String>> ret = new HashMap<>();
		try {
			ClassPool pool = new ClassPool();
			pool.appendClassPath(this.getJarFilePath().replace("test-classes", "classes"));
			List<String> tmpclasspaths = new ArrayList<>();
			tmpclasspaths.add(this.getJarFilePath().replace("test-classes", "classes"));
			for (String cls : SootUtil.getInstance().getJarsClasses(tmpclasspaths)) {
				if (pool.getOrNull(cls) != null) {
//					System.out.println();
					//ret.addAll(pool.get(cls).getRefClasses());
					if (!ret.containsKey(cls)) {
						ret.put(cls, new LinkedList<>());
					}
					for (String refedCls : pool.get(cls).getRefClasses()) {
						if (!SootUtil.getInstance().isJavaLibraryClass(refedCls)) {
							ret.get(cls).add(refedCls);
						}
					}
					//pool.get(cls).getM
				} else {
					MavenUtil.getInstance().getLog().warn("can't find " + cls + " in pool when form reference.");
				}
			}
		}
		catch (Exception e) { System.err.println("Caught Exception!");
			MavenUtil.getInstance().getLog().error("get refed classes error for jar:" + this.getSig() + e);
			//e.printStackTrace();
		}
		return ret;
	}
	public Map<String, Collection<String>> getRefedClsWithBuffer() {
		if (allRefedCls == null) {
			/**
			 * if our user specified refed cls buffer and this is not host
			 */
			if (this.isHost()) {
				return getRefedClsRealTime();
			}
			if (!GlobalVar.i().updateRefedClsBuffer && isBufferValidByExist(/*getJarFilePath(), */getRefedClsBufferFilePath())) {
				try {
					allRefedCls = new Gson().fromJson(FileUtils.readFileToString(this.getRefedClsBufferFile()),
							new TypeToken<Map<String, Collection<String>>>() {
							}.getType());
					if (allRefedCls == null) {
						allRefedCls = getRefedClsRealTime();
						if (!this.isHost()) {
							FileUtil.getInstance().writeGson(getRefedClsBufferFile(), allRefedCls);
						}
					}
				} catch (Exception e) { System.err.println("Caught Exception!");
					allRefedCls = getRefedClsRealTime();
					System.out.println(this.getRefedClsBufferFilePath());
					if (!this.isHost()) {
						FileUtil.getInstance().writeGson(getRefedClsBufferFile(), allRefedCls);
					}
					MavenUtil.getInstance().getLog().error("get refed clses with buffer failed. DepJar: getRefedClsesWithBuffer" + e);
					e.printStackTrace();
				}
			}
			else {
				allRefedCls = getRefedClsRealTime();
				if (!this.isHost()) {
					FileUtil.getInstance().writeGson(getRefedClsBufferFile(), allRefedCls);
				}
			}
		}
		return allRefedCls;
	}
	/**
	 * @param useTarget:
	 *            host-class-name can get from source directory(false) or target
	 *            directory(true). using source directory: advantage: get class
	 *            before maven-package disadvantage:class can't deconstruct by
	 *            soot;miss class that generated.
	 * @return
	 */
	public List<String> getJarFilePaths(boolean useTarget) {
		if (!useTarget) {// use source directory
			// if node is inner project,will return source directory(using source directory
			// can get classes before maven-package)
			if (isHost()) {
				return MavenUtil.getInstance().getSrcPaths();
			}
		}
		/*
		如果是test，那么默认路径为testclasses目录
		 */
		if (GlobalVar.i().isTest && this.isHost() && !jarFilePaths.get(0).endsWith("test-classes")) {
			List<String> ret= new ArrayList<>();
			ret.add("");
			ret.set(0, jarFilePaths.get(0).substring(0, jarFilePaths.get(0).lastIndexOf("classes")) + "test-classes");
			MavenUtil.getInstance().getLog().info("ret depjargets" + ret);
			return ret;
		}
		return jarFilePaths;
	}
	public String getJarFilePath() {
		if (GlobalVar.i().isTest && this.isHost() && !jarFilePaths.get(0).endsWith("test-classes")) {
			List<String> ret= new ArrayList<>(1);
			ret.add("");
			ret.set(0, jarFilePaths.get(0).substring(0, jarFilePaths.get(0).lastIndexOf("classes")) + "test-classes");
			MavenUtil.getInstance().getLog().info("ret depjarget" + ret);
			return ret.get(0);
		}
		return jarFilePaths.get(0);
	}
	/**
	 * 与别的jar包pair调用图缓存所在目录
	 * 如果是被测项目，那么所在目录就是classes目录下
	 * @return
	 */
	public String getCgBufferDir() {
		String ret = null;
		if (isHost()) {
			ret = getJarFilePaths(true).get(0) + File.separator + "CgBuf" + File.separator;
		}
		else {
			ret = UserConf.getInstance().cgBufRepository + this.getGroupId().replace(".", File.separator) + File.separator
					+ getArtifactId() + File.separator + getVersion() + File.separator + "CgBuf" + File.separator;
		}
		File CgBufferDir = new File(ret);
		if (!CgBufferDir.exists()) {
			CgBufferDir.mkdirs();
		}
		return ret;
	}
	public String getRefedClsBufferDir() {
		String ret = null;
		/**
		 * Not possible
		 */
		if (isHost()) {
			ret = MavenUtil.getInstance().getBaseDir() + File.separator + "refedClsBuf" + File.separator;
		}
		else {
			ret = UserConf.getInstance().refedClsBufRepository + this.getGroupId().replace(".", File.separator) + File.separator
					+ getArtifactId() + File.separator + getVersion() + File.separator + "refedClsBuf" + File.separator;
		}
		File refedClsBufferDir = new File(ret);
		if (!refedClsBufferDir.exists()) {
			refedClsBufferDir.mkdirs();
		}
		return ret;
	}
	public String getRefedClsBufferFilePath() {
		String ret = getRefedClsBufferDir() + "refedCls.json";
		return ret;
	}
	public File getRefedClsBufferFile() {
		return new File(getRefedClsBufferFilePath());
	}
	public String getFilterBufferDir() {
		String ret = null;
		/**
		 * Not possible
		 */
		if (isHost()) {
			ret = MavenUtil.getInstance().getBaseDir() + File.separator + "FilterBuf" + File.separator;
		}
		else {
			ret = UserConf.getInstance().filterBufRepository + this.getGroupId().replace(".", File.separator) + File.separator
					+ getArtifactId() + File.separator + getVersion() + File.separator + "FilterBuf" + File.separator;
		}
		File CgBufferDir = new File(ret);
		if (!CgBufferDir.exists()) {
			CgBufferDir.mkdirs();
		}
		return ret;
	}
	public String getFilterBufferFilePath() {
		String ret = getFilterBufferDir() + "filterResult.json";
		return ret;
	}
	public File getFilterBufferFile() {
		return new File(getFilterBufferFilePath());
	}
	public String getAllClsBufferDir() {
		String ret = null;
		/**
		 * Not possible
		 */
		if (isHost()) {
			ret = MavenUtil.getInstance().getBaseDir() + File.separator + "AllClsBuf" + File.separator;
		}
		else {
			ret = UserConf.getInstance().clsMthdBufRepository + this.getGroupId().replace(".", File.separator) + File.separator
					+ getArtifactId() + File.separator + getVersion() + File.separator + "AllClsBuf" + File.separator;
		}
		File allClsBufferDir = new File(ret);
		if (!allClsBufferDir.exists()) {
			allClsBufferDir.mkdirs();
		}
		return ret;
	}
	public String getAllClsBufferFilePath() {
		String ret = getAllClsBufferDir() + "allCls.json";
		return ret;
	}
	public File getAllClsBufferFile() {
		return new File(getAllClsBufferFilePath());
	}
	public String getClsTbBufferFilePath() {
		String ret = getAllClsBufferDir() + "clsTb.json";
		return ret;
	}
	public boolean isHost() {
		if (getNodeAdapters().size() == 1) {
			NodeAdapter node = getNodeAdapters().iterator().next();
			if (MavenUtil.getInstance().isInner(node)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * use this jar replace version of used-version ,then return path of
	 * all-used-jar
	 * 使用这个jar替代了旧版本，然后返回所有的旧jar的路径
	 * 说人话：就是把所有被使用jar（除去和本jar相同artifactId的），以及本jar返回
	 * @return
	 * @throws Exception
	 */
	public List<String> getRepalceCp() throws Exception {
		List<String> paths = new ArrayList<String>();
		paths.addAll(this.getJarFilePaths(true));
		boolean hasRepalce = false;
		for (DepJar usedDepJar : DepJars.i().getUsedDepJars()) {
			if (this.isSameLib(usedDepJar)) {// used depJar instead of usedDepJar.
				if (hasRepalce) {
					MavenUtil.getInstance().getLog().warn("when cg, find multiple usedLib for " + toString());	//有重复的使用路径
					throw new Exception("when cg, find multiple usedLib for " + toString());
				}
				hasRepalce = true;
			} else {
				for (String path : usedDepJar.getJarFilePaths(true)) {
					paths.add(path);
				}
			}
		}
		if (!hasRepalce) {
			MavenUtil.getInstance().getLog().warn("when cg,can't find mutiple usedLib for " + toString());
			throw new Exception("when cg,can't find mutiple usedLib for " + toString());
		}
		return paths;
	}
	/**
	 *
	 */
	public List<DepJar> getRepalceJarList() throws Exception {
		List<DepJar> depJars = new ArrayList<>();
		depJars.add(this);
		boolean hasRepalce = false;
		for (DepJar usedDepJar : DepJars.i().getUsedDepJars()) {
			if (this.isSameLib(usedDepJar)) {// used depJar instead of usedDepJar.
				if (hasRepalce) {
					MavenUtil.getInstance().getLog().warn("when cg, find multiple usedLib for " + toString());	//有重复的使用路径
					throw new Exception("when cg, find multiple usedLib for " + toString());
				}
				hasRepalce = true;
			} else {
				depJars.add(usedDepJar);
			}
		}
		if (!hasRepalce) {
			MavenUtil.getInstance().getLog().warn("when cg,can't find mutiple usedLib for " + toString());
			throw new Exception("when cg,can't find mutiple usedLib for " + toString());
		}
		return depJars;
	}
	/**
	 * get only father jar class paths, used in pruning
	 * 只获取父节点，剪枝时使用
	 * @param includeSelf : include self
	 * @return Set<String> fatherJarCps
	 */
	public Set<String> getOnlyFatherJarCps(boolean includeSelf) {
		Set<String> fatherJarCps = new HashSet<String>();
		for (NodeAdapter node : this.nodeAdapters) {
			fatherJarCps.addAll(node.getImmediateAncestorJarCps(includeSelf));
		}
		return fatherJarCps;
	}
	/**
	 * get only father jar class paths, used in pruning
	 * 只获取父节点，剪枝时使用
	 * @param includeSelf : include self
	 * @return Set<String> fatherJarCps
	 */
	public Set<DepJar> getOnlyFatherJars(boolean includeSelf) {
		Set<DepJar> fatherJars = new HashSet<>();
		for (NodeAdapter node : this.nodeAdapters) {
			fatherJars.addAll(node.getImmediateAncestorJars(includeSelf));
		}
		return fatherJars;
	}
	/**
	 * get only father jar class paths, used in pruning
	 * 只获取父节点，剪枝时使用
	 * @param includeSelf : include self
	 * @return Set<String> fatherJarCps
	 */
	public Set<String> getOnlyFatherJarCps4Scene6(boolean includeSelf) {
		Set<String> fatherJarCps = new HashSet<String>();
		for (NodeAdapter node : this.nodeAdapters) {
			fatherJarCps.addAll(node.getImmediateAncestorJarCps4Scene6(includeSelf));
		}
		return fatherJarCps;
	}
	public Set<DepJar> getOnlyFatherJars4Scene6(boolean includeSelf) {
		Set<DepJar> fatherJars = new HashSet<>();
		for (NodeAdapter node : this.nodeAdapters) {
			fatherJars.addAll(node.getImmediateAncestorJars4Scene6(includeSelf));
		}
		return fatherJars;
	}
	/**
	 * @return scope
	 */
	public String getScope() {
		String scope = null;
		for (NodeAdapter node : nodeAdapters) {
			scope = node.getScope();
			if (scope != null) {
				break;
			}
		}
		return scope;
	}
	/**
	 * @return priority
	 */
	public int getPriority() {
		return priority;
	}
	public Collection<String> getPrcDirPaths() {
		List<String> classpaths = new ArrayList<String>();
		MavenUtil.getInstance().getLog().info("not add all jar to process");
		try {
			classpaths.addAll(this.getJarFilePaths(true));
			classpaths.addAll(this.getOnlyFatherJarCps(true));
		} catch(NullPointerException e) {System.err.println("Caught Exception!");
			classpaths = new ArrayList<String>();
		}
		classpaths = SootUtil.getInstance().invalidClassPreprocess(classpaths);
		return classpaths;
	}
	public Collection<String> getPrcDirPaths(DepJar depJar) {
		List<String> classpaths = new ArrayList<String>();
		MavenUtil.getInstance().getLog().info("not add all jar to process");
		try {
			classpaths.addAll(this.getJarFilePaths(true));
			classpaths.addAll(depJar.getOnlyFatherJarCps(false));
		} catch(NullPointerException e) {System.err.println("Caught Exception!");
			classpaths = new ArrayList<String>();
		}
		classpaths = SootUtil.getInstance().invalidClassPreprocess(classpaths);
		return classpaths;
	}
}
