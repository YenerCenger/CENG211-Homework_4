package boxes;

import exceptions.EmptyBoxException;
import exceptions.UnmovableFixedBoxException;
import grid.Direction;
import tools.SpecialTool;

public abstract class Box {

    protected final char[] faces; // Face.ordinal() indexing
    protected SpecialTool tool;   // null means empty
    protected boolean opened;     // affects M/O marker

    protected Box(char[] faces, SpecialTool tool) {
        this.faces = faces;
        this.tool = tool;
        this.opened = false;
    }

    public char getTopLetter() {
        return faces[Face.TOP.ordinal()];
    }

    public boolean isOpened() {
        return opened;
    }

    public boolean isEmpty() {
        return tool == null;
    }

    public SpecialTool open() throws EmptyBoxException {
        opened = true;
        if (tool == null) throw new EmptyBoxException();
        SpecialTool out = tool;
        tool = null;
        return out;
    }

    public abstract void roll(Direction direction) throws UnmovableFixedBoxException;

    public abstract void stampTop(char targetLetter);

    public abstract char getTypeMarker(); // R/U/X
}
