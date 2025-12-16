package game;

import grid.BoxGrid;
import tools.SpecialTool;

public class BoxPuzzle {

    private final BoxGrid grid;
    private final char targetLetter;

    public BoxPuzzle() {
        this.grid = new BoxGrid();
        this.targetLetter = grid.randomTargetLetter();
        new Menu().run();
    }

    public char getTargetLetter() {
        return targetLetter;
    }

    public BoxGrid getGrid() {
        return grid;
    }

    public <T extends SpecialTool> void acquireAndUseTool(T tool, int row, int col) {
        tool.useTool(this, row, col);
    }

    public class Menu {
        public void run() {
            // TODO: Implement game loop (max 5 turns), menus, input parsing, exception handling.
        }
    }
}
