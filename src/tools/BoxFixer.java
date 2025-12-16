package tools;

import exceptions.BoxAlreadyFixedException;

public class BoxFixer extends SpecialTool {
    @Override
    public void useTool(game.BoxPuzzle puzzle, int row, int col) {
        try {
            puzzle.getGrid().convertToFixedBox(row, col);
        } catch (BoxAlreadyFixedException e) {
            System.out.println("Box is already fixed. Turn wasted.");
        }
    }
}
