package nju.lab.DSchecker.util.soot;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IHostProjectInfo;

import java.io.File;
import java.util.*;

/**
 * add args which soot need to use
 * @author buriedpot
 * @author chaicho
 */
@Slf4j
public abstract class AbstractSootAna {

	IHostProjectInfo hostProjectInfo;
	public void setHostProjectInfo(IHostProjectInfo hostProjectInfo){
		this.hostProjectInfo = hostProjectInfo;
	}
	static List<String> excludeList;
	private static Collection<String> excludeList()
	{
		if(excludeList==null)
		{
			excludeList = new ArrayList<>();

			excludeList.add("java.*");
			excludeList.add("java.lang.*");
			excludeList.add("javax.*");
			excludeList.add("sun.*");
			excludeList.add("sunw.*");
			excludeList.add("com.sun.*");
			excludeList.add("com.ibm.*");
			excludeList.add("com.apple.*");
			excludeList.add("apple.awt.*");
			excludeList.add("java.util.*");
			excludeList.add("jdk.*");
			excludeList.add("com.google.common.*");
		}
		return excludeList;
	}
	protected List<String> getArgs(String[] jarFilePaths) {
		List<String> argsList = new ArrayList<String>();
		addProcessDir(argsList, jarFilePaths);
		//this class can't analysis
		if(argsList.size()==0) {
			return argsList;
		}
		addGenArg(argsList);
		addCgArgs(argsList);
		addIgrArgs(argsList);
		return argsList;
	}



	public List<String> getArgsWithHost(List<String> jarFilePaths) {
		List<String> argsList = new ArrayList<String>();
		addClassPath(argsList, jarFilePaths);
		//this class can't analysis
		if(argsList.size()==0) {
			return argsList;
		}
		addHostArg(argsList);
		addGenArg(argsList);
		addCgArgs(argsList);
		addIgrArgs(argsList);
		return argsList;
	}

	public List<String> getArgsWithHostNoCg(List<String> jarFilePaths) {
		List<String> argsList = new ArrayList<String>();
		addClassPath(argsList, jarFilePaths);
		//this class can't analysis
		if(argsList.size()==0) {
			return argsList;
		}
		addHostArg(argsList);
		addGenArg(argsList);
		addIgrArgs(argsList);
		argsList.add("-p");
		argsList.add("cg");
		argsList.add("off");
		return argsList;
	}

	private void addHostArg(List<String> argsList) {
//		for (String srcDir : HostProjectInfo.i().getCompileSrcDirs()) {
//			argsList.add("-process-dir");
//			argsList.add(srcDir);
//		}
		argsList.add("-process-dir");
    System.out.println("hostProjectInfo.getBuildCp() = " + hostProjectInfo.getBuildCp());
		argsList.add(hostProjectInfo.getBuildCp());
	}

	private void addTestOutputArg(List<String> argsList) {
		argsList.add("-process-dir");
		argsList.add(hostProjectInfo.getBuildTestCp());
	}

	/**
	 * addCgArgs
	 * @param argsList
	 */
	protected abstract void addCgArgs(List<String> argsList);

	/* Add all jarFilePaths into -process-dir. */
	private void addProcessDir(List<String> argsList, String[] jarFilePaths) {
		for (String jarFilePath : jarFilePaths) {
			if (new File(jarFilePath).exists()) {
				if (canAna(jarFilePath)) {
					argsList.add("-process-dir");
					argsList.add(jarFilePath);
				}else {
					log.warn("add classpath error:can't analysis file " + jarFilePath);
				}
			} else {
				log.warn("add classpath error:doesn't exist file " + jarFilePath);
			}
		}
	}

	/* Add jarFilePaths to classPath. */
	protected void addClassPath(List<String> argsList, List<String> jarFilePaths) {

		String jredir = System.getProperty("java.home")+"/lib/rt.jar";
//		String jce = System.getProperty("java.home")+"/lib/jce.jar";

		String classPath = hostProjectInfo.getBuildCp();

		classPath = classPath + File.pathSeparator + jredir;
		argsList.add("-cp");

		for (String jarFilePath : jarFilePaths) {
			if (new File(jarFilePath).exists()) {
				if (canAna(jarFilePath)) {
//					argsList.add("-cp");
					classPath += File.pathSeparator + jarFilePath;
//					if(File.)
				}else {
					log.warn("add classpath error:can't analysis file " + jarFilePath);
				}
			} else {
				log.warn("add classpath error:doesn't exist file " + jarFilePath);
			}
		}
		argsList.add(classPath);
//		argsList.add(classPath.substring(1));
	}
	/**
	 * @param argsList
	 */
	protected void addExcludeArgs(List<String> argsList) {
//		argsList.add("-app");
		argsList.addAll(Arrays.asList(new String[] { "-no-bodies-for-excluded", }));
		for (String excludePkg : excludeList()) {
			argsList.addAll(Arrays.asList(new String[]{"-x", excludePkg,}));
		}
	}
	/**
	 * @param argsList
	 * @param includePkgs, eg, org.*, Main
	 */
	protected void addIncludeArgs(List<String> argsList, Collection<String> includePkgs) {
		for (String includePkg : includePkgs) {
			argsList.addAll(Arrays.asList(new String[]{"-i", includePkg,}));
		}
	}
	protected boolean canAna(String jarFilePath) {
//		return true;
		String asm = "\\asm\\";
		String const6 ="6";
		if(jarFilePath.contains(asm)&&jarFilePath.contains(const6)) {
			return false;
		}
		return true;
	}
	protected void 	addGenArg(List<String> argsList) {
		argsList.add("-ire");
		/**
		 * 暂时认为-app没用
		 */
		argsList.add("-allow-phantom-refs");
		argsList.add("-w");
		argsList.add("-ignore-resolving-levels");
		argsList.add("-ignore-resolution-errors");

//		argsList.add("-pp");
	}
	protected void addIgrArgs(List<String> argsList) {
		argsList.addAll(Arrays.asList(new String[] { "-p", "wjop", "off", }));
		argsList.addAll(Arrays.asList(new String[] { "-p", "wjap", "off", }));
		argsList.addAll(Arrays.asList(new String[] { "-p", "jtp", "off", }));
		argsList.addAll(Arrays.asList(new String[] { "-p", "jop", "off", }));
		argsList.addAll(Arrays.asList(new String[] { "-p", "jap", "off", }));
		argsList.addAll(Arrays.asList(new String[] { "-p", "bb", "off", }));
		argsList.addAll(Arrays.asList(new String[] { "-p", "tag", "off", }));
		//no output
		argsList.addAll(Arrays.asList(new String[] { "-f", "n", }));
	}
}
