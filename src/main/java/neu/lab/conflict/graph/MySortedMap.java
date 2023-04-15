package neu.lab.conflict.graph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

/**
 * 可达性分析内核自定义的Map类
 * @param <K>
 * @param <V>
 */
public class MySortedMap<K, V> {
	private TreeMap<K, Collection<V>> container;
	public MySortedMap() {
		container = new TreeMap<K, Collection<V>>();
	}
	/**
	 * Map添加KV
	 * @param k
	 * @param v
	 */
	public void add(K k, V v) {
		Collection<V> set = container.get(k);
		if (set == null) {
			set = new ArrayList<V>();
			container.put(k, set);
		}
		set.add(v);
	}
	/**
	 * 获取Map大小
	 * @return
	 */
	public int size() {
		return container.size();
	}
	/**
	 * 扁平化map
	 * @return
	 */
	public List<V> flat() {
		List<V> result = new ArrayList<V>();
		for (K k : container.keySet()) {
			result.addAll(container.get(k));
		}
		return result;
	}
}
