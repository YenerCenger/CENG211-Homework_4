package tools;

import exceptions.BoxAlreadyFixedException;
import exceptions.UnmovableFixedBoxException;
import java.util.Random;

public abstract class SpecialTool {
    public abstract void useTool(game.BoxPuzzle puzzle, int row, int col)
            throws UnmovableFixedBoxException, BoxAlreadyFixedException;

    public static SpecialTool randomTool(grid.BoxGrid grid, Random rng, double eachProbability) {
        double p = rng.nextDouble();
        double step = eachProbability;
        if (p < step)
            return new PlusShapeStamp();
        if (p < 2 * step)
            return new MassRowStamp();
        if (p < 3 * step)
            return new MassColumnStamp();
        if (p < 4 * step)
            return new BoxFlipper();
        return new BoxFixer();
    }
}
