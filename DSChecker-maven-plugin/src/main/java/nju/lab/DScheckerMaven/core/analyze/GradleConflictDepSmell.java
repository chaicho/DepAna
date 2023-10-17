package nju.lab.DScheckerMaven.core.analyze;

import nju.lab.DSchecker.core.analyze.BaseSmell;
import nju.lab.DSchecker.core.model.IHostProjectInfo;

public class GradleConflictDepSmell extends BaseSmell {
    @Override
    public void detect() {
        appendToResult("========GradleConflictDepSmell========");
        hostProjectInfo
    }
}
