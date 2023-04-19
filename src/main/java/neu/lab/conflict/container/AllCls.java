package neu.lab.conflict.container;

import lombok.Getter;
import nju.lab.DSchecker.model.DepJar;
//import nju.lab.DSchecker.model.DepJar;

import java.util.HashSet;
import java.util.Set;
/**
 * @author asus
 *FinalClasses is set of ClassVO,but AllCls is set of class signature.
 *FinalClasses是ClassVO的集合，但AllCls是类签名的集合。
 */
@Getter
public class AllCls {
	private static AllCls instance =null;
	private Set<String> clses;

	/**
	 * 计算所有jar包中的所有类名
	 * @param depJars jar包
	 */
	public static void init(DepJars depJars) {
		if (instance == null) {
			instance = new AllCls(depJars, false);
		}
	}
	/**
	 * 初始化的时候用其他depJar
	 * @param depJars
	 * @param depJar
	 */
	public static void init(DepJars depJars, DepJar depJar) {
			instance = new AllCls(depJars, depJar);
	}
	public static AllCls i() {
		return instance;
	}
	private AllCls(DepJars depJars, boolean useBuffer){
		clses = new HashSet<>();

		for (DepJar depJar : depJars.getAllDepJar()) {
				if (depJar.isSelected()) {
					//得到depJar中所有的类
					clses.addAll(depJar.getAllCls(true));
				}
			}

	}
	/**
	 * 重构方法，使初始化方法有默认参数
	 */
	private AllCls(DepJars depJars, DepJar usedDepJar) {
		clses = new HashSet<>();
		for (DepJar depJar : depJars.getAllDepJar()) {
			if (depJar.isSelected()) {
				//得到depJar中所有的类
				if (depJar.isSameLib(usedDepJar)) {
					clses.addAll(usedDepJar.getAllCls(true));
				} else {
					clses.addAll(depJar.getAllCls(true));
				}
			}
		}
	}
	/**
	 * 获取这个项目中所有被加载的jar包中的所有类名集合
	 * @return
	 */
	public Set<String> getAllCls() {
		return clses;
	}
	/**
	 * clses是否包含sls
	 * @param cls
	 * @return
	 */
	public boolean contains(String cls) {
		return clses.contains(cls);
	}
}
