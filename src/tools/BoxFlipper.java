package tools;

import exceptions.UnmovableFixedBoxException;

public class BoxFlipper extends SpecialTool {
    @Override
    public void useTool(game.BoxPuzzle puzzle, int row, int col) throws UnmovableFixedBoxException {
        puzzle.getGrid().flipBoxVertically(row, col);
    }
}
