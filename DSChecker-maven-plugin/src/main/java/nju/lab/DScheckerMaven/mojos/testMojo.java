package nju.lab.DScheckerMaven.mojos;


import nju.lab.DSchecker.core.model.IDepJar;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.util.List;

@Mojo(name = "testDSchecker",defaultPhase = LifecyclePhase.COMPILE)
public class testMojo extends AbstractMojo {
    @Override
    public void execute() {
        IDepJar depJar = null;
        System.out.println("Hello World1!");
    }
}