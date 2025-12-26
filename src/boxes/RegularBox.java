package boxes;

public class RegularBox extends Box {

    public RegularBox() {
        super(); // Random nums
    }

    @Override
    public char getTypeMarker() {
        return 'R';
    }
}