package boxes;

import exceptions.UnmovableFixedBoxException;
import grid.Direction;
import tools.SpecialTool;

public class UnchangingBox extends Box {

    private final char initialTop;

    public UnchangingBox(grid.BoxGrid grid, SpecialTool tool) {
        super(BoxFactory.randomFaces(grid.rng()), tool);
        this.initialTop = faces[Face.TOP.ordinal()];
    }

    @Override
    public void roll(Direction direction) throws UnmovableFixedBoxException {
        BoxRotations.applyRoll(faces, direction);
        faces[Face.TOP.ordinal()] = initialTop;
    }

    @Override
    public void stampTop(char targetLetter) {
        faces[Face.TOP.ordinal()] = initialTop;
    }

    @Override
    public char getTypeMarker() {
        return 'U';
    }
}
