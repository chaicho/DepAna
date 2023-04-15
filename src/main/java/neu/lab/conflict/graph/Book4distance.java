package neu.lab.conflict.graph;
import java.util.ArrayList;
/**
 * 一种用于方法可达性分析的数据结构
 * @author guoruijie
 */
public class Book4distance extends IBook {
	public Book4distance(INode node) {
		super(node);
		this.records = new ArrayList<IRecord>();
	}
	/**
	 * 检测内核，用于在后续无节点后，将该节点可达性信息保存
	 */
	@Override
	public void afterAddAllChildren() {
		if(getNode().isRisk()) {
			this.addRecord(getNodeName(), 0, 0);
		}
	}
	/**
	 * 检测内涵，用于将所有children添加到record
	 */
	@Override
	public void addChild(IBook doneChildBook) {
		int thisBranch = getNode().getBranch();
		for (IRecord recordI : doneChildBook.getRecords()) {
			Record4distance record = (Record4distance) recordI;
			addRecord(record.getName(), record.getBranch()+thisBranch, record.getDistance()+1);
		}
	}
	private Node4distance getNode() {
		return (Node4distance)this.node;
	}
	private void addRecord(String nodeName, double branch, double distance) {
		for (IRecord iRecord : this.records) {
			Record4distance record = (Record4distance) iRecord;
			if (nodeName.equals(record.getName())) {
				record.updateBranch(branch);
				record.updateDistance(distance);
				return;
			}
		}
		this.records.add(new Record4distance(nodeName, branch, distance));
	}
}
