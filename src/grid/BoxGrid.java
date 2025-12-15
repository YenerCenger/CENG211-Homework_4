package grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import boxes.Box;
import boxes.FixedBox;
import boxes.RegularBox;
import boxes.UnchangingBox;
import tools.SpecialTool;

/*
ANSWER TO COLLECTIONS QUESTION:
I used an ArrayList<Box> to store the 8x8 grid in a contiguous 1D structure (size 64).
This provides O(1) access via index = row*8 + col, is memory-efficient, and simplifies
row/column traversals needed for the domino-effect rolling operations.
*/

public class BoxGrid {

    public static final int SIZE = 8;
    private final List<Box> boxes;
    private final Random rng;

    public BoxGrid() {
        this.rng = new Random();
        this.boxes = new ArrayList<>(SIZE * SIZE);
        generateInitialGrid();
    }

    public Box getBoxAt(int row, int col) {
        return boxes.get(row * SIZE + col);
    }

    public void setBoxAt(int row, int col, Box box) {
        boxes.set(row * SIZE + col, box);
    }

    public Random rng() {
        return rng;
    }

    public char randomTargetLetter() {
        return (char) ('A' + rng.nextInt(8));
    }

    private void generateInitialGrid() {
        for (int i = 0; i < SIZE * SIZE; i++) {
            boxes.add(generateRandomBox());
        }
    }

    private Box generateRandomBox() {
        double p = rng.nextDouble();
        if (p < 0.05) {
            return new FixedBox(this, null);
        } else if (p < 0.15) {
            return new UnchangingBox(this, generateRandomToolForUnchanging());
        } else {
            return new RegularBox(this, generateRandomToolForRegular());
        }
    }

    private SpecialTool generateRandomToolForRegular() {
        // 75% tool, 25% empty; each tool 15% overall
        double p = rng.nextDouble();
        if (p >= 0.75) return null;
        return SpecialTool.randomTool(this, rng, 0.15);
    }

    private SpecialTool generateRandomToolForUnchanging() {
        // guaranteed tool; each tool 20%
        return SpecialTool.randomTool(this, rng, 0.20);
    }

    // TODO: implement printing, rolling (domino effect), rolled-this-turn tracking, edge/corner checks.
}
