//package neu.lab.conflict.statistics;
//
//import neu.lab.conflict.container.DepJars;
//import neu.lab.conflict.statistics.ClassDup;
//import neu.lab.conflict.util.risk.DepJarJRisk;
//import neu.lab.conflict.vo.DepJar;
//
//import java.util.*;
//
//public class ClassDups{
//    private Map<String, ClassDup> container;
//    private List<ClassDup> allClsDup;
//
//    public ClassDups(DepJars depJars) {
//        container = new HashMap<>();
//        for (DepJar depJar : depJars.getAllDepJar()) {
//            if (depJar.isSelected()) {
//                for (String cls : depJar.getAllCls(false)) {
//                    /**
//                     *
//                     */
//                    addCls(cls, depJar);
//                }
//            }
//        }
//        Iterator<Map.Entry<String, ClassDup>> ite = container.entrySet().iterator();
//        while (ite.hasNext()) {
//            ClassDup conflict = ite.next().getValue();
//            if (!conflict.isDup()) {// delete conflict if there is only one version
//                ite.remove();
//            }
//        }
//    }
//
//    public ClassDups(DepJars depJars, DepJarJRisk depJarJRisk) {
//        container = new HashMap<>();
////        Set<String> thrownClasses = depJarJRisk.getThrownClasses();
////        for (String cls : thrownClasses) {
////            addCls(cls, depJarJRisk.getConflictJar());
////        }
//        DepJar nowJar = depJarJRisk.getConflictJar();
//        for (String cls : nowJar.getAllCls(false)) {
//            addCls(cls, nowJar);
//        }
//        for (DepJar depJar : depJars.getAllDepJar()) {
//
//            if (depJar.isSelected() && !depJar.isSameLib(depJarJRisk.getUsedDepJar()) && !depJar.isSameJarIgnoreClassifier(nowJar)) {
//                for (String cls : depJar.getAllCls(false)) {
//                    addCls(cls, depJar);
//                }
//            }
//        }
//        Iterator<Map.Entry<String, ClassDup>> ite = container.entrySet().iterator();
//        while (ite.hasNext()) {
//            ClassDup conflict = ite.next().getValue();
//            if (!conflict.isDup()) {// delete conflict if there is only one version
//                ite.remove();
//            } else if (!conflict.getDepJars().contains(depJarJRisk.getConflictJar())) {
//                ite.remove();
//            } else if (conflict.isAllTestScope()) {
//                ite.remove();
//            }
//        }
//    }
//
//
//    /**
//     * set ArrayList -> Set
//     * @return
//     */
//    public List<ClassDup> getAllClsDup() {
//        if (allClsDup == null) {
//            allClsDup = new ArrayList<>();
//            allClsDup.addAll(container.values());
//        }
//        return allClsDup;
//    }
//
//    private void addCls(String classSig, DepJar depJar) {
//        ClassDup clsDup = null;
//        if (container.containsKey(classSig)) {
//            clsDup = container.get(classSig);
//        }
//        if (null == clsDup) {
//            clsDup = new ClassDup(classSig);
//            container.put(classSig, clsDup);
//        }
//        clsDup.addDepJar(depJar);
//    }
//
//}