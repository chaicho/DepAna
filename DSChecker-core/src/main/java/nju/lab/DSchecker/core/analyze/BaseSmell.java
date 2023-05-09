package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.ICallGraph;
import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DSchecker.core.model.IDepJars;
import nju.lab.DSchecker.core.model.IHostProjectInfo;
import org.apache.commons.io.FileUtils;

import java.io.File;

@Slf4j
public abstract class BaseSmell {
    public void init(IHostProjectInfo hostProjectInfo, ICallGraph callGraph, IDepJars<? extends IDepJar> depJars) {
        this.hostProjectInfo = hostProjectInfo;
        this.callGraph = callGraph;
        this.depJars = depJars;
        this.outFile = hostProjectInfo.getOutputFile();
    }
    IHostProjectInfo hostProjectInfo;
    ICallGraph  callGraph;
    IDepJars<? extends IDepJar> depJars;

    File outFile;
    public void output(String content){
        try {
            FileUtils.writeStringToFile(outFile, content+"\n", "UTF-8", true);
        } catch (Exception e) {
            log.error("Error writing to file", e);
        }
    }

    public void detectSmell(){
        try {
            detect();
        }
        catch (Exception e) {
            log.error(e.getMessage());
            output("ERRROR");
            output("gg");
            output(e.getMessage());
            output(getClass().toString());
            // Output the stacktrace to the file
            for (StackTraceElement element : e.getStackTrace()) {
                output(element.toString());
            }
        }
    }
    public abstract void detect();
}
