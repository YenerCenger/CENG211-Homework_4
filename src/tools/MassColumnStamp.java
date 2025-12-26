package tools;

import exceptions.BoxAlreadyFixedException;
import exceptions.UnmovableFixedBoxException;

public class MassColumnStamp extends SpecialTool {
    @Override
    public void useTool(game.BoxPuzzle puzzle, int row, int col)
            throws UnmovableFixedBoxException, BoxAlreadyFixedException {
        puzzle.getGrid().stampColumn(col, puzzle.getTargetLetter());
    }
}
