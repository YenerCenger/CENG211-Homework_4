package boxes;

import exceptions.UnmovableFixedBoxException;
import grid.Direction;
import tools.SpecialTool;

public class FixedBox extends Box {

    public FixedBox() {
        super();
    }

    @Override
    public char getTypeMarker() {
        char c = 'X';
        return c; // R for Regular Box
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