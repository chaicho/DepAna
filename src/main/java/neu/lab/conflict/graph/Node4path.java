package neu.lab.conflict.graph;

import neu.lab.conflict.vo.GlobalVar;

import java.util.*;
/**
 * 描述一个方法的数据结构
 * 包括方法签名，是否是被测项目（Host项目）定义的方法，是否是风险方法等。类似于Node4distance
 * @author guoruijie
 */
public class Node4path implements INode {
	private String name;
	private boolean isHost;
	private boolean isRisk;
	private Set<String> outs;
	//	private Set<String> ins;
	/**
	 *  初始化 节点信息，包括方法签名，是否是主项目的方法
	 * @param name 方法签名
	 * @param isHost 是不是Host项目定义的方法
	 * @param isRisk 是不是风险方法（也就是因冲突问题未被加载的方法）
	 */
	public Node4path(String name, boolean isHost, boolean isRisk) {
		super();
		this.name = name;
		this.isHost = isHost;
		this.isRisk = isRisk;
		if (GlobalVar.i().useTreeSet) {
			outs = new TreeSet<String>();
		} else {
			outs = new HashSet<String>();
		}
		//		ins = new TreeSet<String>();
	}
	/**
	 * toString
	 * @return
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("name : ").append(name);
		sb.append(" isHost : ").append(isHost);
		sb.append(" isRisk : ").append(isRisk);
		sb.append(" out : ");
		for(String out : outs){
			sb.append(out).append(" ");
		}
		return sb.toString();
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
	 * 获取这个方法所有的出节点
	 * @return
	 */
	@Override
	public Collection<String> getNexts() {
		return outs;
	}
	/**
	 * 获取这个方法被Host项目的所有可达性信息
	 * @return
	 */
	@Override
	public IBook getBook() {
		return new Book4path(this);
	}
	/**
	 * 生成一条新的可达性信息
	 * @return
	 */
	@Override
	public IRecord formNewRecord() {
		return new Record4path(this.name, this.name, 1, this.name);
	}
	/**
	 * 生成一条出节点（出边）
	 * @param tgt
	 */
	public void addOutNd(String tgt) {
		outs.add(tgt);
	}
	/**
	 * 是否是风险方法
	 * @return
	 */
	public boolean  isRisk() {
		return isRisk;
	}
	/**
	 * 是否是主项目定义的方法
	 * @return
	 */
	public boolean isHostNode() {
		return isHost;
	}
	//	public void addInNd(String src) {
	//		ins.add(src);
	//
	//	}
	//	public int getInCnt() {
	//		return ins.size();
	//	}
	/**
	 * 检测内涵所用，果去除一些边
	 * @param nds2remain
	 */
	public void filterEdge(Set<String> nds2remain) {
		Iterator<String> ite = outs.iterator();
		while (ite.hasNext()) {
			String node = ite.next();
			if (!nds2remain.contains(node)) {
				ite.remove();
			}
		}
		//		ite = ins.iterator();
		//		while(ite.hasNext()) {
		//			String node = ite.next();
		//			if(!nds2remain.contains(node)) {
		//				ite.remove();
		//			}
		//		}
	}
}
