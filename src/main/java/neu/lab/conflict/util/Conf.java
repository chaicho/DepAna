package neu.lab.conflict.util;

import neu.lab.conflict.vo.NodeAdapter;
import org.apache.maven.shared.dependency.tree.DependencyNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户配置
 */
public class Conf {
	private static class ConfHolder {
		private static final Conf INSTANCE = new Conf();
	}
	private Conf() {
	}
	public static Conf getInstance() {
		return ConfHolder.INSTANCE;
	}
	//TODO path depth
	public int DOG_DEP_FOR_DIS ;//final path may be larger than PATH_DEP when child book is existed.
	public int DOG_DEP_FOR_PATH ;//final path may be larger than PATH_DEP when child book is existed.
	public String callConflict;
	public String callConflicts;
	public boolean findAllpath;
	public boolean ONLY_GET_SIMPLE = false;
	public boolean DEL_LONGTIME = true;
	public boolean DEL_OPTIONAL = false;
	public List<String> compileSourceRoots;
	/**
	 * 检测是不是存在类丢失的情况
	 */
	public boolean CLASS_MISSING = false;
	/**
	 * 有目标的检测jar包
	 */
    public String targetJar = null;
//	for test, record conflict dependency node count when use the new pruning method
	public Map<String, Map<String, Integer>> purEditionMap = new HashMap<>();
    //dependency node record used String
    public List<List<String>> list = new ArrayList<>();
    //dependency node record used DependencyNode
    public List<List<DependencyNode>> dependencyList = new ArrayList<>();
    // to record exclude
    public Map<String, List<NodeAdapter>> dependencyMap = new HashMap<>();//key是依赖名，value是依赖树中所有把key排除的依赖节点。
    // first level
    public List<NodeAdapter> needAddNodeList = new ArrayList<>();
    // second level
	public List<NodeAdapter> firstLevelNeedAddNodeList = new ArrayList<>();
	// second level runtime
	public List<Long> secondLevelRunTime = new ArrayList<>();
	// circular dependency node list
	public List<DependencyNode> circularNodeList = new ArrayList<>();
}
