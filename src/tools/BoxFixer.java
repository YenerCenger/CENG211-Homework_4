package tools;

import exceptions.BoxAlreadyFixedException;

public class BoxFixer extends SpecialTool {
    @Override
    public void useTool(game.BoxPuzzle puzzle, int row, int col) throws BoxAlreadyFixedException {
        puzzle.getGrid().convertToFixedBox(row, col);
    }
}
