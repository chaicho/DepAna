package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.ICallGraph;
import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DSchecker.core.model.IDepJars;
import nju.lab.DSchecker.core.model.IHostProjectInfo;
import nju.lab.DSchecker.util.javassist.GetRefedClasses;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public abstract class BaseSmell {
    public void init(IHostProjectInfo hostProjectInfo, ICallGraph callGraph, IDepJars<? extends IDepJar> depJars) {
        this.hostProjectInfo = hostProjectInfo;
        this.callGraph = callGraph;
        this.depJars = depJars;
        this.outFile = hostProjectInfo.getOutputFile();
    }
    public IHostProjectInfo hostProjectInfo;
    public ICallGraph  callGraph;
    public IDepJars<? extends IDepJar> depJars;
    public File outFile;
    public String result = "";
    public void appendToResult(String content){
            result += content + "\n";
    }
    public boolean containsHost(Collection<IDepJar> depJars){
        for (IDepJar depJar : depJars) {
            if (depJar.isHost()) {
                return true;
            }
        }
        return false;
    }
    public boolean containsHost(Set<IDepJar> depJars){
        for (IDepJar depJar : depJars) {
            if(depJar.isHost()) {
                return true;
            }
        }
        return false;
    }
    public void outputResult(){
        try {
            FileUtils.writeStringToFile(outFile, result, "UTF-8", true);
        }
        catch (Exception e){
            log.error(e.getMessage());
        }
    }

    public boolean validClass(String className){
        if(className.contains("module-info")){
            return false;
        }
        return true;
    }
    public void detectSmell(){
        try {
            detect();
        }
        catch (Exception e) {
            log.error(e.getMessage());
            appendToResult("ERRROR");
            appendToResult("gg");
            appendToResult(e.getMessage());
            appendToResult(getClass().toString());
            // Output the stacktrace to the file
            for (StackTraceElement element : e.getStackTrace()) {
                appendToResult(element.toString());
            }
        }
    }
    public abstract void detect();
}
