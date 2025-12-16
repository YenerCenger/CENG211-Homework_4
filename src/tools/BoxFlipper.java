package tools;

import exceptions.UnmovableFixedBoxException;

public class BoxFlipper extends SpecialTool {
    @Override
    public void useTool(game.BoxPuzzle puzzle, int row, int col) {
        try {
            puzzle.getGrid().flipBoxVertically(row, col);
        } catch (UnmovableFixedBoxException e) {
            System.out.println("Cannot flip a FixedBox. Turn wasted.");
        }
    }
}
