package neu.lab.conflict.graph;

import neu.lab.conflict.util.MyLogger;
import neu.lab.conflict.vo.GlobalVar;
import neu.lab.conflict.util.GradleUtil;

import java.util.*;
/**
 * 计算调用图方法可达性分析的工具类，涉及方法可达性分析内核，勿改动
 * @author guoruijie
 *
 */
public class Dog {
	/**
	 * RESET_BOOK, NOT_RESET_BOOK
	 */
	public enum Strategy {
		RESET_BOOK, NOT_RESET_BOOK
	}
	private IGraph graph;
	protected String pos;
	protected List<String> route;
	protected Map<String, Cross> graphMap = new HashMap<String, Cross>();
	protected Map<String, List<String>> circleMap = new HashMap<String, List<String>>();
	protected Map<String, IBook> entryBooks;
	protected Map<String, IBook> books;
	/**
	 * books for nodes in current route.
	 */
	protected Map<String, IBook> tempBooks = new HashMap<String, IBook>();
	/**
	 * 初始化
	 * @param graph
	 */
	public Dog(IGraph graph) {
		this.graph = graph;
	}
	protected IBook buyNodeBook(String nodeName) {
		return graph.getNode(nodeName).getBook();
	}
	/**
	 * 从entrys出发，搜索所有逆向可达方法集合
	 * @param entrys 入口方法集合
	 * @param maxDep 最大深度
	 * @param strategyType 策略
	 * @return
	 */
	public Map<String, IBook> findRlt(Collection<String> entrys, int maxDep, Strategy strategyType) {
		//		maxDep = Integer.MAX_VALUE;
		MyLogger.i().info("dog starts running with depth " + maxDep);
		Set<String> sortedEntrys = new TreeSet<String>();
		//TODO filter entry that don't exist in graph.
		for (String entry : entrys) {
			if (graph.getAllNode().contains(entry)) {
				sortedEntrys.add(entry);
			}
		}
		Set<String> linkedEntrys = new LinkedHashSet<String>();
		//TODO
		//		if (entrys.contains("<com.rackspacecloud.blueflood.service.ZKShardLockManager: void shutdownUnsafe()>")) {
		//			linkedEntrys.add("<com.rackspacecloud.blueflood.service.ZKShardLockManager: void shutdownUnsafe()>");
		//		}
		String tmpSig1 = "<org.wisdom.test.internals.ChameleonExecutor: void deployApplication()>";
		if (entrys.contains(tmpSig1)) {
			linkedEntrys.add(tmpSig1);
		} else {
			linkedEntrys.addAll(sortedEntrys);
		}
		if (Strategy.NOT_RESET_BOOK.equals(strategyType)) {
			return findRlt1(linkedEntrys, maxDep);
		} else {
			return findRlt2(linkedEntrys, maxDep);
		}
	}
	private Map<String, IBook> findRlt1(Collection<String> entrys, int maxDep) {
		//不重置已完成的book
		MyLogger.i().info("dog won't reset doneBook.");
		books = new HashMap<String, IBook>(0);
		long start = System.currentTimeMillis();
		for (String mthd : entrys) {
			route = new ArrayList<String>();
			if (books.containsKey(mthd)) {
				continue;
			} else {
				forward(mthd);
				while (pos != null) {
					if (needChildBook(maxDep)) {
						String frontNode = graphMap.get(pos).getBranch();
						getChildBook(frontNode);
					} else {
						back();
					}
				}
			}
		}
		long runtime = (System.currentTimeMillis() - start) / 1000;
		MyLogger.i().info("dog finishes running.");
		MyLogger.i().info("dog run time:" + runtime);
		GlobalVar.i().time2runDog += runtime;
		return this.books;
	}
	/**reset doneBook for each entry to guarantee depth.
	 * depth may short than configuration because of doneBook in each search.
	 * @param entrys
	 * @param maxDep
	 * @return
	 */
	private Map<String, IBook> findRlt2(Collection<String> entrys, int maxDep) {
		MyLogger.i().info("dog will reset doneBook.");
		entryBooks = new HashMap<String, IBook>(0);
		long start = System.currentTimeMillis();
		for (String mthd : entrys) {
			route = new ArrayList<String>();
			books = new HashMap<String, IBook>(0);
			forward(mthd);
			while (pos != null) {
				if (needChildBook(maxDep)) {
					String frontNode = graphMap.get(pos).getBranch();
					getChildBook(frontNode);
				} else {
					back();
				}
			}
			entryBooks.put(mthd, books.get(mthd));
		}
		long runtime = (System.currentTimeMillis() - start) / 1000;
		MyLogger.i().info("dog finishes running.");
		MyLogger.i().info("dog run time:" + runtime);
		GlobalVar.i().time2runDog += runtime;
		MyLogger.i().info("this.entryBooks:" + this.entryBooks.size());
		return this.entryBooks;
	}
	public boolean needChildBook(int maxDep) {
		return graphMap.get(pos).hasBranch() && route.size() < maxDep;
		// return graphMap.get(pos).hasBranch();
	}
	//	private boolean isNodeInPath() {
	//		String latsNode = route.get(route.size()-1);
	//		if(last)
	//	}
	private void getChildBook(String frontNode) {
		if (books.containsKey(frontNode)) {
			addChildBookInfo(frontNode, pos);
		} else {
			forward(frontNode);
		}
	}
	/**
	 * frontNode是一个手册没有完成的节点，需要为这个节点建立手册
	 *
	 * @param frontNode
	 */
	private void forward(String frontNode) {
		//TODO debug dog
		//DebugUtil.print(UserConf.getInstance().getOutDir4Mac() + "tdogTrace.txt", frontNode + " " + route.size());
		// System.out.println("forward to " + frontNode);
		INode node = graph.getNode(frontNode);
		if (node != null) {
			if (!route.contains(frontNode)) {
				pos = frontNode;
				route.add(pos);
				IBook nodeRch = buyNodeBook(frontNode);
				this.tempBooks.put(frontNode, nodeRch);
				graphMap.put(pos, new Cross(node));
			} else {
				List<String> circle = new ArrayList<String>();
				int index = route.indexOf(frontNode) + 1;
				while (index < route.size()) {
					circle.add(route.get(index));
					index++;
				}
				this.circleMap.put(frontNode, circle);
			}
		}
	}
	private void back() {
		String donePos = route.get(route.size() - 1);
		// System.out.println("back from " + donePos);
		graphMap.remove(donePos);
		IBook book = this.tempBooks.get(donePos);
		book.afterAddAllChildren();
		this.tempBooks.remove(donePos);
		this.books.put(donePos, book);
		if (circleMap.containsKey(donePos)) {
			dealLoopNd(donePos);
			circleMap.remove(donePos);
		}
		route.remove(route.size() - 1);
		if (route.size() == 0) {
			pos = null;
		} else {
			pos = route.get(route.size() - 1);
			addChildBookInfo(donePos, pos);
		}
	}
	private void addChildBookInfo(String donePos, String pos) {
		IBook doneBook = this.books.get(donePos);
		IBook doingBook = this.tempBooks.get(pos);
		doingBook.addChild(doneBook);
	}
	protected void dealLoopNd(String donePos) {
	}
}
