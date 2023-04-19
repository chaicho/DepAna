package neu.lab.conflict.vo;
import nju.lab.DSchecker.model.ClassVO;

import java.util.HashSet;
import java.util.Set;
/**
 * 描述Java方法的数据结构
 */
public class MethodVO {
    /**
     * 方法签名
     */
    private String mthdSig;
    /**
     * 该方法内部定义了哪些方法
     */
    @Deprecated
    private Set<String> inMthds;
    private ClassVO cls;
    /**
     *
     * @param mthdSig 方法签名
     * @param cls 该方法所属类的全限定名
     */
    public MethodVO(String mthdSig,ClassVO cls) {
        this.mthdSig = mthdSig;
        inMthds = new HashSet<String>();
        this.cls = cls;
    }
    public ClassVO getClassVO() {
        return cls;
    }
    public String getMthdSig() {
        return mthdSig;
    }
    public void setMthdSig(String mthdSig) {
        this.mthdSig = mthdSig;
    }
    @Deprecated
    public void addInMthds(String mthdSig) {
        if (null == this.inMthds) {
            inMthds = new HashSet<String>();
        }
        inMthds.add(mthdSig);
    }
    public Set<String> getInMthds() {
        return inMthds;
    }
    public void setInMthds(Set<String> inMthds) {
        this.inMthds = inMthds;
    }
    /**
     * 方法签名是否相等
     * @param mthdSig2
     * @return
     */
    public boolean isSameName(String mthdSig2) {
        return mthdSig.equals(mthdSig2);
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodVO) {
            MethodVO method = (MethodVO) obj;
            return mthdSig.equals(method.getMthdSig());
        } else {
            return false;
        }
    }
    @Override
    public int hashCode() {
        return this.mthdSig.hashCode();
    }
    @Override
    public String toString() {
        return "MethodVO{" +
                "mthdSig='" + mthdSig + '\'' +
                ", inMthds=" + inMthds +
                '}';
    }
}
