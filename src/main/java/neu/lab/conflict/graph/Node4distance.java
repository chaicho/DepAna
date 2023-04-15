package neu.lab.conflict.graph;

import neu.lab.conflict.vo.GlobalVar;
import neu.lab.conflict.util.SootUtil;

import java.util.*;
/**
 * 描述一个方法的数据结构
 * 包括方法签名，是否是被测项目（Host项目）定义的方法，是否是风险方法等。
 * @author guoruijie
 */
public class Node4distance implements INode {
	private String name;
	private boolean isAccessHost;//not private
	private boolean isRisk;
	private int cfgBranch;
	private Integer branch;// cfgBranch + polyBranch
	private Set<String> outs;
	/**
	 * 初始化 节点信息，包括方法签名，是否是主项目的方法
	 * @param name 方法签名
	 * @param isAccessHost 是不是Host项目定义的方法
	 * @param isRisk 是不是风险方法（也就是因冲突问题未被加载的方法）
	 * @param cfgBranch
	 */
	public Node4distance(String name, boolean isAccessHost, boolean isRisk, int cfgBranch) {
		super();
		this.name = name;
		this.isAccessHost = isAccessHost;
		this.isRisk = isRisk;
		this.cfgBranch = cfgBranch;
		if (GlobalVar.i().useTreeSet) {
			outs = new TreeSet<String>();
		} else {
			outs = new HashSet<String>();
		}
	}
	/**
	 * 一种用于检测内核获取枝条的函数
	 * @return
	 */
	public Integer getBranch() {
		if (branch == null) {
			Map<String, Integer> tgt2cnt = new HashMap<String, Integer>(0);
			// traverse all out
			for (String tgtNd : outs) {
				String tgtName = SootUtil.getInstance().mthdSig2name(tgtNd);
				Integer cnt = tgt2cnt.get(tgtName);
				if (cnt == null) {
					tgt2cnt.put(tgtName, 1);
				} else {
					tgt2cnt.put(tgtName, cnt + 1);
				}
			}
			// get polymorphic
			int polyBranch = 0;
			for (String tgtName : tgt2cnt.keySet()) {
				if (tgt2cnt.get(tgtName) > 1) {
					polyBranch++;
				}
			}
			branch = cfgBranch + polyBranch;
		}
		return branch;
	}
	/**
	 * 是否是风险方法
	 * @return
	 */
	public boolean isRisk() {
		return isRisk;
	}
	/**
	 * 获取方法签名
	 * @return
	 */
	@Override
	public String getName() {
		return name;
	}
	/**
	 * 获取本方法调用的所有方法节点
	 * @return
	 */
	@Override
	public Collection<String> getNexts() {
		return outs;
	}
	/**
	 * 获取本方法的可达性分析信息
	 * @return
	 */
	@Override
	public IBook getBook() {
		return new Book4distance(this);
	}
	/**
	 * 生成一条新的可达性信息记录
	 * @return
	 */
	@Override
	public IRecord formNewRecord() {
		return new Record4distance(name, 0, 0);
	}
	/**
	 * hashCode
	 * @return
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	/**
	 * equals
	 * @param obj
	 * @return
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Node4distance other = (Node4distance) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
	/**
	 * 是否是Host项目定义的方法（待测项目）
	 * @return
	 */
	public boolean isHostNode() {
		return isAccessHost;
	}
	/**
	 * 添加一条出边
	 * @param tgt
	 */
	public void addOutNd(String tgt) {
		outs.add(tgt);
	}
}
