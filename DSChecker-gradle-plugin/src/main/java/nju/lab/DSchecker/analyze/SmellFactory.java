package nju.lab.DSchecker.analyze;

import java.util.ArrayList;
import java.util.List;

public class SmellFactory {

    /**
     * Creates a new instance of each smell implementation.
     *
     * @return a list of BaseSmell objects
     */
    public static List<BaseSmell> createSmells() {
        List<BaseSmell> smells = new ArrayList<>();
        smells.add(new BloatedSmell());
        smells.add(new ClassConflictSmell());
        smells.add(new LibraryConflictSmell());
        smells.add(new HostClassSmell());
        smells.add(new LibraryConflictSmell());
        smells.add(new LibraryScopeSmell());
        smells.add(new UnDeclaredSmell());
        smells.add(new BloatedSmell());
        return smells;
    }

    /**
     * Detects all smells for the specified list of BaseSmell objects.
     *
     * */
    public static void detectAll() {
        List<BaseSmell> smells = createSmells();
        for (BaseSmell smell : smells) {
            smell.detect();
        }
    }
}
