package boxes;

public class RegularBox extends Box {

    public RegularBox() {
        super(); // Initialize with random faces
    }

    @Override
    public char getTypeMarker() {
        return 'R';
    }
}