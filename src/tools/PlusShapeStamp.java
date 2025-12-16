package tools;

public class PlusShapeStamp extends SpecialTool {
    @Override
    public void useTool(game.BoxPuzzle puzzle, int row, int col) {
        puzzle.getGrid().stampPlusShape(row, col, puzzle.getTargetLetter());
    }
}
