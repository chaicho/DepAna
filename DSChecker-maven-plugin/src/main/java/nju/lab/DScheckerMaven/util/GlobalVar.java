package nju.lab.DScheckerMaven.util;



import java.io.File;
import java.util.*;

public class GlobalVar {
	public static GlobalVar instance;
	public boolean isTest = false;

	private GlobalVar() {
	}
	public static GlobalVar i() {
		if (instance == null) {
			instance = new GlobalVar();
		}
		return instance;
	}
	public boolean updateAllClsBuffer;
	public boolean updateClsTbBuffer;
	public long runTime = 0;
	public long time2cg = 0;//
	public long time2runDog = 0;//
	public long branchTime = 0;//
	public long time2calRef = 0;//
	public long time2filterRiskMthd = 0;//
	public int scenario = 2;
	public long calFatherDefTime = 0;
	public boolean useAllJar ;
	public boolean prune2 ;
	public boolean useAllJarTwoThree;
	public boolean useAllJarFour;
	public boolean filterSuper;
	public boolean filterMethod;
	public String filterListPath;
	public List<String> filterMthds;
	public boolean useFilterBuffer;
	public boolean writeFilterBuffer;
	public boolean canUseFilterBuffer;
	public boolean useClsTbBuffer;
	public boolean useReverse;
	public boolean combineClsTbWithPhantom;
	public boolean genFilterBuffer;
	public boolean useAllClsBuffer;
	public boolean useRefedClsBuffer;
	public boolean useAllJarFilter2;
	public boolean detectScenario5;
	public boolean preGenFilterBuffer;
}
