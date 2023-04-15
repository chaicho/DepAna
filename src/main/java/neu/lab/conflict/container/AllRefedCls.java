package neu.lab.conflict.container;

import javassist.ClassPool;
import neu.lab.conflict.util.MyLogger;
import neu.lab.conflict.vo.GlobalVar;
import neu.lab.conflict.util.GradleUtil;
//import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.util.SootUtil;
import neu.lab.conflict.vo.DepJar;
import neu.lab.conflict.vo.GlobalVar;
//import neu.lab.vo.GlobalVar;

import java.util.*;
/**
 * 所有被引用的cls。使用javassist进行
 * 两种：一种是所有被加载jar的所有引用类，一种是类引用可达性分析，分析非反射情况下可能被主项目引用到的所有类集合
 * @author wangchao
 *
 */
public class AllRefedCls {
	private static AllRefedCls instance;
	private static AllRefedCls instance_notReachable;
	private Set<String> refedClses;
	/**
	 * cls and all its refed classes
	 */
	private Map<String, Collection<String>> cls2ref;
	private Map<String, Collection<String>> reverseCls2ref;
	/**
	 * get a ref path for given refed cls
	 * @param refedCls
	 * @return
	 */
	public List<String> getRefPath(String refedCls) {
		Set<String> visited = new HashSet<>();
		Set<String> entryClses = DepJars.i().getHostDepJar().getAllCls(true);
		Map<String, String> mp = new HashMap<>();
		for (String entryCls : entryClses) {
			boolean found = getRefedPathDfs(entryCls, refedCls, visited, mp);
			if (found) {
				Stack<String> stack = new Stack<>();
				stack.add(refedCls);
				for (String location = mp.get(refedCls) ; false == location.equals(entryCls) ; location = mp.get(location)) {
					stack.push(location);
				}
				stack.push(entryCls);
				List<String> ret = new ArrayList<>(stack);
				Collections.reverse(ret);
				return ret;
			}
		}
		// return null means no path to refedCls
		return null;
	}
	private boolean getRefedPathDfs(String root, String target, Set<String> visited, Map<String, String> path) {
		boolean found = false;
		Stack<String> s = new Stack<>();
		s.push(root);
		while (!s.empty()) {
			String cur = s.pop();
			if (cur.equals(target)) {
				found = true;
				break;
			}
			if (!cls2ref.containsKey(cur)) {
				continue;
			}
			if (cls2ref.get(cur) == null) {
				continue;
			}
			for (String next : cls2ref.get(cur)) {
				if (!visited.contains(next)) {
					s.push(next);
					path.put(next, cur);
					visited.add(next);
				}
			}
		}
		// means no route
		return found;
	}
	private AllRefedCls() {
		super();
	}
	private AllRefedCls(List<String> jarFilePaths) {
		long start = System.currentTimeMillis();
		refedClses = new HashSet<String>();
		try {
			ClassPool pool = new ClassPool();
			for (String path : jarFilePaths) {
				pool.appendClassPath(path);
			}
			if (GlobalVar.i().isTest) {
				pool.appendClassPath(DepJars.i().getHostDepJar().getJarFilePath().replace("test-classes", "classes"));
			}
			for (String cls : AllCls.i().getAllCls()) {
				refedClses.add(cls);
				if (pool.getOrNull(cls) != null) {
					refedClses.addAll(pool.get(cls).getRefClasses());
				} else {
				}
			}
		} catch (Exception e) { System.err.println("Caught Exception!");
			GradleUtil.i().getLogger().error("get refedCls error:"+e.getMessage());
		}
		long runtime = (System.currentTimeMillis() - start) / 1000;
		GlobalVar.i().time2calRef+=runtime;
	}
	private AllRefedCls(boolean useBuffer) {
		if (!useBuffer) {
			long start = System.currentTimeMillis();
			refedClses = new HashSet<String>();
			ClassPool pool = new ClassPool();
			for (DepJar jar : DepJars.i().getSeqUsedDepJars()) {
				try {
					pool.appendClassPath(jar.getJarFilePath());
				}
				catch (Exception e) { System.err.println("Caught Exception!");
					MyLogger.i().error("get refedCls error: pool appendClassPath"+e.getMessage());
				}
			}
			try {
				if (GlobalVar.i().isTest) {
					pool.appendClassPath(DepJars.i().getHostDepJar().getJarFilePath().replace("test-classes", "classes"));
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			for (String cls : AllCls.i().getAllCls()) {
				refedClses.add(cls);
				if (pool.getOrNull(cls) != null) {
					try {
						refedClses.addAll(pool.get(cls).getRefClasses());
					}
					catch (Exception e) { System.err.println("Caught Exception!");
						MyLogger.i().error("get refedCls error: pool appendClassPath"+e.getMessage());
					}
				} else {
				}
			}
			long runtime = (System.currentTimeMillis() - start) / 1000;
			GlobalVar.i().time2calRef+=runtime;
		}
		else {
			long start = System.currentTimeMillis();
			refedClses = new HashSet<String>();
			Set<String> loadedClasses = new HashSet<>();
			try {
				for (DepJar jar : DepJars.i().getSeqUsedDepJars()) {
					refedClses.addAll(jar.getAllCls(true));
					Map<String, Collection<String>> jarRefedClses = jar.getRefedCls();
					for (String key : jarRefedClses.keySet()) {
						if (!loadedClasses.contains(key)) {
							refedClses.addAll(jarRefedClses.get(key));
							loadedClasses.add(key);
						}
					}
				}
			} catch (Exception e) { System.err.println("Caught Exception!");
				e.printStackTrace();
				MyLogger.i().error("get refedCls error:"+e.getMessage());
			}
			long runtime = (System.currentTimeMillis() - start) / 1000;
			GlobalVar.i().time2calRef+=runtime;
		}
	}
	/**
	 * 获取所有引用类
	 * @return
	 */
	public static AllRefedCls i() {
		if (instance == null) {
			synchronized (AllRefedCls.class) {
				instance = new AllRefedCls(GlobalVar.i().useRefedClsBuffer);
			}
		}
		return instance;
	}
	/**
	 * 直接获取所有被加载jar的引用类
	 * @return
	 */
	public static AllRefedCls iNotReachable() {
		if (instance_notReachable == null) {
			// TODO modify to efficiency
				synchronized (AllRefedCls.class) {
					instance_notReachable = new AllRefedCls(GlobalVar.i().useRefedClsBuffer);
				}
		}
		return instance_notReachable;
	}
	/**
	 * 初始化所有引用类
	 * @param useBuffer 是否使用本地缓存
	 */
	public static void init(boolean useBuffer) {
		instance = new AllRefedCls(useBuffer);
	}
	/**
	 * 建立类和其引用类集合的映射。如果cls2Ref为空就建立，否则返回
	 * @return
	 */
	public Map<String, Collection<String>> getReverseCls2refOrGen() {
		if (reverseCls2ref == null) {
			reverseCls2ref = new HashMap<>();
			for (Map.Entry<String, Collection<String>> entry : cls2ref.entrySet()) {
				for (String value : entry.getValue()) {
					if (!reverseCls2ref.containsKey(value)) {
						reverseCls2ref.put(value, new HashSet<>());
					}
					reverseCls2ref.get(value).add(entry.getKey());
				}
			}
		}
		return reverseCls2ref;
	}
	/**
	 * 建立被引用类到引用类的映射，并搜索可能引用到被引用类的所有类
	 * @param classNames
	 * @return
	 */
	public Collection<String> getReverseReachableClass(Set<String> classNames) {
		getReverseCls2refOrGen();
		Set<String> ret = new HashSet<>();//Reverse Reachable Methods
		for (String entryCls : classNames) {
			ret.add(entryCls);
			Queue<String> q = new LinkedList<>();
			q.offer(entryCls);
			while(!q.isEmpty())
			{
				String cls = q.poll();
				Collection<String> referingClses = reverseCls2ref.get(cls);
				if (referingClses == null) {
					continue;
				}
				for(String referingCls : referingClses)
				{
					if(!ret.contains(referingCls))
					{
						ret.add(referingCls);
						q.offer(referingCls);
					}
				}
			}
		}
		return ret;
	}
	/**
	 * 建立被引用类到引用类的映射
	 * @param cls2ref
	 * @return
	 */
	public static Map<String, Collection<String>> getReverseRefRelations(Map<String, Collection<String>> cls2ref) {
		Map<String, Collection<String>> reverseCls2ref = new HashMap<>();
		for (Map.Entry<String, Collection<String>> entry : cls2ref.entrySet()) {
			for (String value : entry.getValue()) {
				if (!reverseCls2ref.containsKey(value)) {
					reverseCls2ref.put(value, new HashSet<>());
				}
				reverseCls2ref.get(value).add(entry.getKey());
			}
		}
		return reverseCls2ref;
	}
	/**
	 * get Host classes firstly; then host refed Cls's refedCls; then host refed Cls's refedCls;
	 */
	public static void initReachable(boolean useBuffer) {
		instance = new AllRefedCls();
		Queue<String> workList = new LinkedList<>(); // workList = empty
		instance.refedClses = new HashSet<String>(); // ret = empty
		instance.cls2ref = new HashMap<>();
		long start = System.currentTimeMillis();
		if (!useBuffer) {
			/**
			 * Algorithm
			 * ret = empty
			 * firstly, get all defind class and their refed cls map.
			 * workList = empty
			 * add all host defined classes into worklist
			 * while worklist not empty
			 *     cls = worklist.poll
			 *     if cls
			 *     add cls' refed cls into worklist
			 *     add cls' refed cls into ret
			 */
			instance.refedClses = new HashSet<String>();
			try {
				// firstly, get all defind class and their refed cls map.
				ClassPool pool = new ClassPool();
				for (DepJar jar : DepJars.i().getSeqUsedDepJars()) {
					pool.appendClassPath(jar.getJarFilePath());
				}
				if (GlobalVar.i().isTest) {
					pool.appendClassPath(DepJars.i().getHostDepJar().getJarFilePath().replace("test-classes", "classes"));
				}
				// add all host defined classes into worklist
				workList.addAll(DepJars.i().getHostDepJar().getAllCls(true));
				while (!workList.isEmpty()) {
					String cls = workList.poll();
					if (SootUtil.getInstance().isJavaLibraryClass(cls)) {
						continue;
					}
					instance.refedClses.add(cls);
					if (pool.getOrNull(cls) != null) {
						Collection<String> refs = pool.get(cls).getRefClasses();
						// TODO for reverse
						instance.cls2ref.putIfAbsent(cls, refs);
						for (String ref : refs) {
							if (!instance.refedClses.contains(ref)) {
								workList.add(ref);
							}
						}
					}
				}
			} catch (Exception e) { System.err.println("Caught Exception!");
				MyLogger.i().error("get refedCls error:"+e.getMessage());
			}
			long runtime = (System.currentTimeMillis() - start) / 1000;
			GlobalVar.i().time2calRef+=runtime;
		}
		else {
			instance.refedClses = new HashSet<String>();
			Set<String> loadedClasses = new HashSet<>();
			Map<String, Collection<String>> pool = new HashMap<>();
			try {
				for (DepJar jar : DepJars.i().getSeqUsedDepJars()) {
					Map<String, Collection<String>> jarRefedClses = jar.getRefedCls();
					for (Map.Entry<String, Collection<String>> entry : jarRefedClses.entrySet()) {
						pool.putIfAbsent(entry.getKey(), entry.getValue());
					}
				}
				if (GlobalVar.i().isTest) {

					Map<String, Collection<String>> hostClassesRefedClses = DepJars.i().getHostDepJar().getClassesRefedClsWhenTest();
					for (Map.Entry<String, Collection<String>> entry : hostClassesRefedClses.entrySet()) {
						pool.putIfAbsent(entry.getKey(), entry.getValue());
					}
				}
				// add all host defined classes into worklist
				workList.addAll(DepJars.i().getHostDepJar().getAllCls(true));
				// while worklist not empty
				while (!workList.isEmpty()) {
					// cls = worklist.poll
					String cls = workList.poll();
					if (SootUtil.getInstance().isJavaLibraryClass(cls)) {
						continue;
					}
					instance.refedClses.add(cls);
					if (pool.get(cls) != null) {
						Collection<String> refs = pool.get(cls);
						// TODO for reverse
						instance.cls2ref.putIfAbsent(cls, refs);
						for (String ref : refs) {
							if (!instance.refedClses.contains(ref)) {
								workList.add(ref);
							}
						}
					}
				}
			} catch (Exception e) { System.err.println("Caught Exception!");
				e.printStackTrace();
				MyLogger.i().error("get refedCls error:"+e.getMessage());
			}
			long runtime = (System.currentTimeMillis() - start) / 1000;
			GlobalVar.i().time2calRef+=runtime;
		}
	}
	public static AllRefedCls i(List<String> jarFilePaths) {
		if (instance == null) {
			instance = new AllRefedCls(jarFilePaths);
		}
		return instance;
	}
	public static AllRefedCls iReachable() {
		if (instance == null) {
			initReachable(GlobalVar.i().useRefedClsBuffer);
		}
		return instance;
	}
	public boolean contains(String cls) {
		return refedClses.contains(cls);
	}
	public Set<String> getRefedClses() {
		return refedClses;
	}
}
