package boxes;

import exceptions.UnmovableFixedBoxException;
import grid.Direction;
import tools.SpecialTool;

public class FixedBox extends Box {

    public FixedBox(grid.BoxGrid grid, SpecialTool tool) {
        super(BoxFactory.randomFaces(grid.rng()), null);
        this.opened = true; // always shown as O
    }

    @Override
    public void roll(Direction direction) throws UnmovableFixedBoxException {
        throw new UnmovableFixedBoxException();
    }

    @Override
    public void stampTop(char targetLetter) {
        // fixed top; do nothing
    }

    @Override
    public char getTypeMarker() {
        return 'X';
    }
}
