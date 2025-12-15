package boxes;

import exceptions.UnmovableFixedBoxException;
import grid.Direction;
import tools.SpecialTool;

public class RegularBox extends Box {

    public RegularBox(grid.BoxGrid grid, SpecialTool tool) {
        super(BoxFactory.randomFaces(grid.rng()), tool);
    }

    @Override
    public void roll(Direction direction) throws UnmovableFixedBoxException {
        BoxRotations.applyRoll(faces, direction);
    }

    @Override
    public void stampTop(char targetLetter) {
        faces[Face.TOP.ordinal()] = targetLetter;
    }

    @Override
    public char getTypeMarker() {
        return 'R';
    }
}
