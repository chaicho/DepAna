package  neu.lab.conflict.model;
import java.io.File;

public class PhysicalArtifact implements Comparable<PhysicalArtifact> {
    private final Coordinates coordinates;
    private final File file;

    public PhysicalArtifact(Coordinates coordinates, File file) {
        this.coordinates = coordinates;
        this.file = file;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public File getFile() {
        return file;
    }

    @Override
    public int compareTo(PhysicalArtifact other) {
        return 1;
//        int coordinatesComparison = coordinates.compareTo(other.getCoordinates());
//        if (coordinatesComparison == 0) {
//            return file.compareTo(other.getFile());
//        } else {
//            return coordinatesComparison;
//        }
    }


}
