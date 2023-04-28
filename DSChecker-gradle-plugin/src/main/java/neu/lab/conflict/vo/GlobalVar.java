package neu.lab.conflict.vo;

import com.google.common.hash.BloomFilter;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class GlobalVar {
    public static GlobalVar instance;
    public boolean isTest = false;
    public boolean sootProcess= false;
    public long time2calRef;
    public boolean useTreeSet = false;
    public long time2runDog = 0 ;
    public boolean useRefedClsBuffer = false;
    public boolean refReachable;
    public boolean classDetectRefReachable = false;
    public boolean useAllJar = false;
    public boolean filterLambda = false;
    public Map<String, String> riskMethodMap;
    public boolean specifyOne = true;
    public boolean testOutput = true;
    public String specifiedArtifact;
    public String newConflictFilePathJarConflict;
    public List<String> filterMthds;
    public boolean cgFast = false;
    public String cgAlgStr; //cg algorithm String(cha, spark, paddle)
    public boolean sootExcludePkg = true;
    public long time2cg = 0;
    public boolean sootExcludeLittle = true;
    public boolean addJdk=false;
    public boolean useCp;

    private GlobalVar() {
    }
    public static GlobalVar i() { 
        if (instance == null) {
            instance = new GlobalVar();
        }
        return instance;
    }
    public boolean  useAllClsBuffer = false;
}
