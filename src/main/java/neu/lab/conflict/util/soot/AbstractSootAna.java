package neu.lab.conflict.util.soot;

import neu.lab.conflict.util.MyLogger;
import neu.lab.conflict.vo.HostProjectInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * add args which soot need to use
 * @author buriedpot
 */
public abstract class AbstractSootAna {
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



	public List<String> getArgsWithHost(String[] jarFilePaths) {
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

	private void addHostArg(List<String> argsList) {
//		for (String srcDir : HostProjectInfo.i().getCompileSrcDirs()) {
//			argsList.add("-process-dir");
//			argsList.add(srcDir);
//		}

		argsList.add("-process-dir");
		argsList.add(HostProjectInfo.i().getBuildCp());

	}

	/**
	 * addCgArgs
	 * @param argsList
	 */
	protected abstract void addCgArgs(List<String> argsList);

	private void addProcessDir(List<String> argsList, String[] jarFilePaths) {
		for (String jarFilePath : jarFilePaths) {
			if (new File(jarFilePath).exists()) {
				if (canAna(jarFilePath)) {
					argsList.add("-process-dir");
					argsList.add(jarFilePath);
				}else {
					MyLogger.i().warn("add classpath error:can't analysis file " + jarFilePath);
				}
			} else {
				MyLogger.i().warn("add classpath error:doesn't exist file " + jarFilePath);
			}
		}
	}
	protected void addClassPath(List<String> argsList, String[] jarFilePaths) {
//		String classPath = HostProjectInfo.i().getCompileSrcCp();
		String classPath = HostProjectInfo.i().getBuildCp();
//		String classPath = "D:\\Pathtodoc\\dependency-graph-as-task-inputs\\app\\build\\classes\\java\\main";
		argsList.add("-cp");

		for (String jarFilePath : jarFilePaths) {
			if (new File(jarFilePath).exists()) {
				if (canAna(jarFilePath)) {
//					argsList.add("-cp");
					classPath += File.pathSeparator + jarFilePath;
//					if(File.)
				}else {
					MyLogger.i().warn("add classpath error:can't analysis file " + jarFilePath);
				}
			} else {
				MyLogger.i().warn("add classpath error:doesn't exist file " + jarFilePath);
			}
		}
		argsList.add(classPath);
//		argsList.add(classPath.substring(1));
	}
	/**
	 * @param argsList
	 * @param excludePkgs, eg, org.*, Main
	 */
	protected void addExcludeArgs(List<String> argsList, Collection<String> excludePkgs) {
		argsList.addAll(Arrays.asList(new String[] { "-no-bodies-for-excluded", }));
		for (String excludePkg : excludePkgs) {
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
	protected void addGenArg(List<String> argsList) {
		argsList.add("-ire");
		/**
		 * 暂时认为-app没用
		 */
		argsList.add("-allow-phantom-refs");
		argsList.add("-w");
		argsList.add("-ignore-resolving-levels");
		argsList.add("-ignore-resolution-errors");

		argsList.add("-pp");
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
