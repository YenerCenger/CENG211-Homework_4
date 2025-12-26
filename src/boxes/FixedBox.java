package boxes;

import exceptions.UnmovableFixedBoxException;
import grid.Direction;
import tools.SpecialTool;

public class FixedBox extends Box {

    public FixedBox() {
        super();
    }

    // ADDED: Constructor that copies faces from another box
    public FixedBox(Box original) {
        super(original);
    }

    @Override
    public char getTypeMarker() {
        return 'X';
    }

    @Override
    public void roll(Direction direction) throws UnmovableFixedBoxException {
        throw new UnmovableFixedBoxException();
    }

    @Override
    public void setTool(SpecialTool tool) {
        super.setTool(null); // Ne verilirse verilsin null yap.
    }

    @Override
    public void setTop(char letter) {
    }
}