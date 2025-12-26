package tools;

import exceptions.BoxAlreadyFixedException;
import exceptions.UnmovableFixedBoxException;

public class PlusShapeStamp extends SpecialTool {
    @Override
    public void useTool(game.BoxPuzzle puzzle, int row, int col)
            throws UnmovableFixedBoxException, BoxAlreadyFixedException {
        puzzle.getGrid().stampPlusShape(row, col, puzzle.getTargetLetter());
    }
}
