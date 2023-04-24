package nju.lab.DSchecker.analyze;

import neu.lab.conflict.container.DepJars;

public class ClassSmell implements BaseSmell {
    public static  ClassSmell instance;
    private ClassSmell() {
    }
    public static ClassSmell i() {
        if (instance == null) {
            instance = new ClassSmell();
        }
        return instance;
    }

    @Override
    public void detect(){
        DepJars.i().getAllDepJar().forEach(depJar -> {
//                depJar.initClsTbRealTime();
                depJar.getClsTb().forEach((clssig,classVO) -> {
                    System.out.println(clssig);
                });
            }
        );
    }

}
