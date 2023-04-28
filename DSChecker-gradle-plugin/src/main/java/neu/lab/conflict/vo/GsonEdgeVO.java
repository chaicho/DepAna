package neu.lab.conflict.vo;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.Edge;
/*public class GsonEdgeVO {
    public GsonEdgeVO(String srcMthdSig, String tgtMthdSig, String srcClsName, String tgtClsName, Boolean srcPrivate, Boolean tgtPrivate, Boolean srcLibrary, Boolean tgtLibrary, Boolean srcConcrete, Boolean tgtConcrete) {
        this.srcMthdSig = srcMthdSig;
        this.tgtMthdSig = tgtMthdSig;
        this.srcClsName = srcClsName;
        this.tgtClsName = tgtClsName;
        this.srcPrivate = srcPrivate;
        this.tgtPrivate = tgtPrivate;
        this.srcLibrary = srcLibrary;
        this.tgtLibrary = tgtLibrary;
        this.srcConcrete = srcConcrete;
        this.tgtConcrete = tgtConcrete;
    }
    public GsonEdgeVO(Edge edge) {
        SootMethod source = edge.src();
        SootMethod target = edge.tgt();
        this.srcMthdSig = source.getSignature();
        this.tgtMthdSig = target.getSignature();
        this.srcClsName = source.getDeclaringClass().getName();
        this.tgtClsName = target.getDeclaringClass().getName();
        this.srcPrivate = source.isPrivate();
        this.tgtPrivate = target.isPrivate();
        this.srcLibrary = source.isJavaLibraryMethod();
        this.tgtLibrary = target.isJavaLibraryMethod();
        this.srcConcrete = source.isConcrete();
        this.tgtConcrete = target.isConcrete();
    }
    @Override
    public int hashCode() {
        return srcMthdSig.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GsonEdgeVO)) return false;
        GsonEdgeVO objGsonEdge = (GsonEdgeVO) obj;
        return srcMthdSig.equals(objGsonEdge.srcMthdSig) && tgtMthdSig.equals(objGsonEdge.tgtMthdSig);
    }
    public String getSrcMthdSig() {
        return srcMthdSig;
    }
    public void setSrcMthdName(String srcMthdSig) {
        this.srcMthdSig = srcMthdSig;
    }
    public String getTgtMthdSig() {
        return tgtMthdSig;
    }
    public void settgtMthdSig(String tgtMthdSig) {
        this.tgtMthdSig = tgtMthdSig;
    }
    public String getSrcClsName() {
        return srcClsName;
    }
    public void setSrcClsName(String srcClsName) {
        this.srcClsName = srcClsName;
    }
    public String getTgtClsName() {
        return tgtClsName;
    }
    public void setTgtClsName(String tgtClsName) {
        this.tgtClsName = tgtClsName;
    }
    public Boolean getSrcPrivate() {
        return srcPrivate;
    }
    public void setSrcPrivate(Boolean srcPrivate) {
        this.srcPrivate = srcPrivate;
    }
    public Boolean getTgtPrivate() {
        return tgtPrivate;
    }
    public void setTgtPrivate(Boolean tgtPrivate) {
        this.tgtPrivate = tgtPrivate;
    }
    public Boolean getSrcLibrary() {
        return srcLibrary;
    }
    public void setSrcLibrary(Boolean srcLibrary) {
        this.srcLibrary = srcLibrary;
    }
    public Boolean getTgtLibrary() {
        return tgtLibrary;
    }
    public void setTgtLibrary(Boolean tgtLibrary) {
        this.tgtLibrary = tgtLibrary;
    }
    public Boolean getSrcConcrete() {
        return srcConcrete;
    }
    public void setSrcConcrete(Boolean srcConcrete) {
        this.srcConcrete = srcConcrete;
    }
    public Boolean getTgtConcrete() {
        return tgtConcrete;
    }
    public void setTgtConcrete(Boolean tgtConcrete) {
        this.tgtConcrete = tgtConcrete;
    }
    String srcMthdSig;
    String tgtMthdSig;// 目标方法名
    String srcClsName;// 源方法的类名
    String tgtClsName;// 目标方法的类名
    Boolean srcPrivate;
    Boolean tgtPrivate;
    Boolean srcLibrary;
    Boolean tgtLibrary;
    Boolean srcConcrete;
    Boolean tgtConcrete;
}*/

/**
 * 用于读取缓存的调用关系数据结构
 */
@Deprecated
public class GsonEdgeVO {
    public GsonEdgeVO(String srcMthdSig, String tgtMthdSig, String srcClsName, String tgtClsName, Boolean srcPrivate, Boolean tgtPrivate, Boolean srcLibrary, Boolean tgtLibrary, Boolean srcConcrete, Boolean tgtConcrete) {
        this.srcMthdSig = srcMthdSig;
        this.tgtMthdSig = tgtMthdSig;
        this.srcClsName = srcClsName;
        this.tgtClsName = tgtClsName;
        this.srcPrivate = srcPrivate;
        this.tgtPrivate = tgtPrivate;
        this.srcLibrary = srcLibrary;
        this.tgtLibrary = tgtLibrary;
        this.srcConcrete = srcConcrete;
        this.tgtConcrete = tgtConcrete;
    }
    public GsonEdgeVO(Edge edge) {
        SootMethod source = edge.src();
        SootMethod target = edge.tgt();
        this.srcMthdSig = source.getSignature();
        this.tgtMthdSig = target.getSignature();
        this.srcClsName = source.getDeclaringClass().getName();
        this.tgtClsName = target.getDeclaringClass().getName();
        this.srcPrivate = source.isPrivate();
        this.tgtPrivate = target.isPrivate();
        this.srcLibrary = source.isJavaLibraryMethod();
        this.tgtLibrary = target.isJavaLibraryMethod();
        this.srcConcrete = source.isConcrete();
        this.tgtConcrete = target.isConcrete();
    }
    @Override
    public int hashCode() {
        return srcMthdSig.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GsonEdgeVO)) {
            return false;
        }
        GsonEdgeVO objGsonEdge = (GsonEdgeVO) obj;
        return srcMthdSig.equals(objGsonEdge.srcMthdSig) && tgtMthdSig.equals(objGsonEdge.tgtMthdSig);
    }
    public String getSrcMthdSig() {
        return srcMthdSig;
    }
    public void setSrcMthdName(String srcMthdSig) {
        this.srcMthdSig = srcMthdSig;
    }
    public String getTgtMthdSig() {
        return tgtMthdSig;
    }
    public void settgtMthdSig(String tgtMthdSig) {
        this.tgtMthdSig = tgtMthdSig;
    }
    public String getSrcClsName() {
        return srcClsName;
    }
    public void setSrcClsName(String srcClsName) {
        this.srcClsName = srcClsName;
    }
    public String getTgtClsName() {
        return tgtClsName;
    }
    public void setTgtClsName(String tgtClsName) {
        this.tgtClsName = tgtClsName;
    }
    public Boolean getSrcPrivate() {
        return srcPrivate;
    }
    public void setSrcPrivate(Boolean srcPrivate) {
        this.srcPrivate = srcPrivate;
    }
    public Boolean getTgtPrivate() {
        return tgtPrivate;
    }
    public void setTgtPrivate(Boolean tgtPrivate) {
        this.tgtPrivate = tgtPrivate;
    }
    public Boolean getSrcLibrary() {
        return srcLibrary;
    }
    public void setSrcLibrary(Boolean srcLibrary) {
        this.srcLibrary = srcLibrary;
    }
    public Boolean getTgtLibrary() {
        return tgtLibrary;
    }
    public void setTgtLibrary(Boolean tgtLibrary) {
        this.tgtLibrary = tgtLibrary;
    }
    public Boolean getSrcConcrete() {
        return srcConcrete;
    }
    public void setSrcConcrete(Boolean srcConcrete) {
        this.srcConcrete = srcConcrete;
    }
    public Boolean getTgtConcrete() {
        return tgtConcrete;
    }
    public void setTgtConcrete(Boolean tgtConcrete) {
        this.tgtConcrete = tgtConcrete;
    }
    @Override
    public String toString() {
        return "GsonEdgeVO{" + srcMthdSig + "==>" + tgtMthdSig +"}";
    }
    String srcMthdSig;
    String tgtMthdSig;// 目标方法名
    String srcClsName;// 源方法的类名
    String tgtClsName;// 目标方法的类名
    Boolean srcPrivate;
    Boolean tgtPrivate;
    Boolean srcLibrary;
    Boolean tgtLibrary;
    Boolean srcConcrete;
    Boolean tgtConcrete;
}
