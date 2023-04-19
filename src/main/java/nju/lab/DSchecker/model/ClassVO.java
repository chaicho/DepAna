package nju.lab.DSchecker.model;

import neu.lab.conflict.vo.MethodVO;

import java.util.HashSet;
import java.util.Set;
/**
 * 每个jar包中的类数据结构
 * @author wangchao
 *
 */
public class ClassVO {
    private String clsSig;//类标记
    private Set<MethodVO> mthds;// methods in class
    private DepJar depJar;//所属的jar
    /**
     * 构造函数
     * @param clsSig 类全限定名
     */
    public ClassVO(String clsSig) {
        this.clsSig = clsSig;
        mthds = new HashSet<MethodVO>();
    }
    /**
     * get
     * @return
     */
    public DepJar getDepJar() {
        return depJar;
    }
    /**
     * set
     * @param depJar
     */
    public void setDepJar(DepJar depJar) {
        this.depJar = depJar;
    }
    /**
     * 为这个类添加一个其定义的方法
     * @param mthd
     * @return
     */
    public boolean addMethod(MethodVO mthd) {
        return mthds.add(mthd);
    }
    /**
     * if contains method called mthdSig(may not same method object)
     * 是否包含相同方法（可能不是同一个对象）
     * @param mthdSig2
     * @return
     */
    public boolean hasMethod(String mthdSig2) {
        for (MethodVO mthd : mthds) {
            if (mthd.isSameName(mthdSig2)) {
                return true;
            }
        }
        return false;
    }
    /**
     * 是否是同一个类，通过类全限定名来判断
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClassVO) {
            ClassVO classVO = (ClassVO) obj;
            return clsSig.equals(classVO.getClsSig());
        } else {
            return false;
        }
    }
    /**
     * 通过类名来判断hashCode
     * @return
     */
    @Override
    public int hashCode() {
        return this.clsSig.hashCode();
    }
    /**
     * 获取类全限定名
     * @return
     */
    public String getClsSig() {
        return clsSig;
    }
    /**
     * 设置类全限定名
     * @param clsSig 类全限定名
     */
    public void setClsSig(String clsSig) {
        this.clsSig = clsSig;
    }
    /**
     * 获取这个类所有定义的方法
     * @return
     */
    public Set<MethodVO> getMthds() {
        return mthds;
    }
    /**
     * 设置这个类所有定义的方法
     * @param mthds 方法集合
     */
    public void setMthds(Set<MethodVO> mthds) {
        this.mthds = mthds;
    }
    /**
     * toString
     * @return
     */
    @Override
    public String toString() {
        return "ClassVO{" +
                "clsSig='" + clsSig + '\'' +
                ", mthds=" + mthds +
                '}';
    }
}
