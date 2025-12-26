package tools;

import exceptions.BoxAlreadyFixedException;
import exceptions.UnmovableFixedBoxException;

public class MassRowStamp extends SpecialTool {
    @Override
    public void useTool(game.BoxPuzzle puzzle, int row, int col)
            throws UnmovableFixedBoxException, BoxAlreadyFixedException {
        puzzle.getGrid().stampRow(row, puzzle.getTargetLetter());
    }
}
