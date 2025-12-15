package boxes;

public class RegularBox extends Box {

    public RegularBox() {
        super(); // Random nums
    }

    @Override
    public char getTypeMarker() {
        char c = 'R';
        return c; // R for Regular Box
    }
}