package neu.lab.conflict.tasks;

import neu.lab.conflict.ConflictHandler.LibrarySmell;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class LibrarySmellDetectTask extends DefaultTask {

    @TaskAction
    public void taskAction()
    {
        LibrarySmell librarySmell = LibrarySmell.getInstance();
        String jarPath1 = "D:\\Gradles\\.gradle\\caches\\modules-2\\files-2.1\\org.apache.commons\\commons-collections4\\4.4\\62ebe7544cb7164d87e0637a2a6a2bdc981395e8\\commons-collections4-4.4.jar";
        String jarPath2 = "D:\\Gradles\\.gradle\\caches\\modules-2\\files-2.1\\org.apache.commons\\commons-collections4\\4.2\\54ebea0a5b653d3c680131e73fe807bb8f78c4ed\\commons-collections4-4.2.jar";
        System.out.println("Hello World!");
        try{
//            librarySmell.analyzeJars(jarPath1, jarPath2);
        }
        catch (Exception e){
            System.out.println(e.toString());
            System.out.println(e.getCause());
            System.out.println(e.getMessage());
        }
    }
}
