package neu.lab.conflict.graph;

import neu.lab.conflict.util.Conf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
/**
 * 一种用于方法可达性分析的数据结构，最好勿动
 * @author guoruijie
 *
 */
public class Book4path extends IBook {
	/**
	 * record father who copy book from this node,when all father has visited,this book can be deleted.
	 */
	Set<String> visitedFathes;
	public Book4path(Node4path node) {
		super(node);
		this.records = new ArrayList<IRecord>();
		visitedFathes = new HashSet<String>();
	}
	/**
	 * 检测内核，用于在后续无节点后，将该节点可达性信息保存
	 */
	@Override
	public void afterAddAllChildren() {
		if (getNode().isRisk()) {
			this.records.add(getNode().formNewRecord());
		}
	}
	private Node4path getNode() {
		return (Node4path) this.node;
	}
	/**
	 * 检测内涵，用于将所有children添加到record
	 */
	@Override
	public void addChild(IBook doneChildBook) {
		for (IRecord iRecord : doneChildBook.getRecords()) {
			Record4path record = (Record4path) iRecord;
			addRecord(record.getRiskMthd(), this.getNodeName() + "\n" + record.getPathStr(), record.getPathlen() + 1);
		}
	}
	private void addRecord(String riskMthd, String pathStr, int length) {
		if (Conf.getInstance().findAllpath) {
			this.records.add(new Record4path(riskMthd, pathStr, length));
		} else {//find shortest path
			for (IRecord iRecord : this.records) {
				Record4path record = (Record4path) iRecord;
				if (riskMthd.equals(record.getRiskMthd())) {
					if (length < record.getPathlen()) {
						record.setPathStr(pathStr);
						record.setPathlen(length);
					}
					return;
				}
			}
			this.records.add(new Record4path(riskMthd, pathStr, length, this.getNodeName()));
		}
	}
}
