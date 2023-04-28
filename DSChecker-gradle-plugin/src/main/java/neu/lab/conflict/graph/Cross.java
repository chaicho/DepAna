package neu.lab.conflict.graph;
import java.util.Collection;
import java.util.Iterator;
/**
 * 一种描述调用边数据结构，用于检测内核
 * @author guoruijie
 *
 */
public class Cross {
	Iterator<String> cross;
	/**
	 * 指定inode，初始化该inode所有入边迭代器，将来会迭代使用（涉及检测内核，无需更改）
	 * @param node 指定inode
	 */
	public Cross(INode node) {
		Collection<String> inMthds = node.getNexts();
		if (null != inMthds) {
			this.cross = inMthds.iterator();
		} else {
			this.cross = null;
		}
	}
	/**
	 * 入边迭代器是否还有内容
	 * @return
	 */
	public boolean hasBranch() {
		if (null == cross) {
			return false;
		}
		return cross.hasNext();
	}
	/**
	 * 获取该节点下一条入边
	 * @return
	 */
	public String getBranch() {
		return cross.next();
	}
}
