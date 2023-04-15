package neu.lab.conflict.util.vo;
public class RefedMthdDetail {
    int type;
    String internalTypeName;
    String name;
    String descriptor;
    String ownerInternalName;
    public RefedMthdDetail(int type, String internalTypeName, String name, String descriptor, String ownerInternalName) {
        this.type = type;
        this.internalTypeName = internalTypeName;
        this.name = name;
        this.descriptor = descriptor;
        this.ownerInternalName = ownerInternalName;
    }
    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }
    public void setInternalTypeName(String internalTypeName) {
        this.internalTypeName = internalTypeName;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getInternalTypeName() {
        return internalTypeName;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescriptor() {
        return descriptor;
    }
    public String getOwnerInternalName() {
        return ownerInternalName;
    }
    public void setOwnerInternalName(String ownerInternalName) {
        this.ownerInternalName = ownerInternalName;
    }
}
