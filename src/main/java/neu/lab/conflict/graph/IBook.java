package neu.lab.conflict.graph;
import java.util.Collection;
/**
 * 存放方法可达性信息的接口类（抽象类）
 * @author SUNJUNYAN
 */
public abstract class IBook {
	protected INode node;
	protected Collection<IRecord> records;
	/**
	 * 初始化，以一个方法节点数据结构初始化方法被调用信息。
	 * @param node
	 */
	public IBook(INode node) {
		this.node = node;
	}
	/**
	 * when dog is back,add self information to book.
	 */
	public abstract void afterAddAllChildren();
	/**
	 * add child book path to self.
	 * @param doneChildBook
	 */
	public abstract void addChild(IBook doneChildBook);
	/**
	 * 获取方法签名
	 * @return
	 */
	public String getNodeName() {
		return node.getName();
	}
	/**
	 * 获取该方法可达性信息（如Host项目到该方法的调用路径）
	 * @return
	 */
	public Collection<IRecord> getRecords() {
		return records;
	}
	/**
	 * toString
	 * @return
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("book for " + this.getNodeName() + "\n");
		for (IRecord recordI : this.getRecords()) {
			sb.append(recordI.toString() + "\n");
		}
		return sb.toString();
	}
}
