package tools;

public class MassRowStamp extends SpecialTool {
    @Override
    public void useTool(game.BoxPuzzle puzzle, int row, int col) {
        puzzle.getGrid().stampRow(row, puzzle.getTargetLetter());
    }
}
