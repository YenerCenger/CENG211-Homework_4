package tools;

public class MassColumnStamp extends SpecialTool {
    @Override
    public void useTool(game.BoxPuzzle puzzle, int row, int col) {
        puzzle.getGrid().stampColumn(col, puzzle.getTargetLetter());
    }
}
