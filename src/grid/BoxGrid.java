package grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import boxes.Box;
import boxes.FixedBox;
import boxes.RegularBox;
import boxes.UnchangingBox;
import exceptions.BoxAlreadyFixedException;
import exceptions.UnmovableFixedBoxException;
import tools.SpecialTool;

/*
 * ANSWER TO COLLECTIONS QUESTION:
 * I used an ArrayList<Box> to store the 8x8 grid in a contiguous 1D structure (size 64).
 * This provides O(1) access via index = row*8 + col. Since the game requires frequent
 * index-based access (getting neighbors, checking specific coordinates) and row/column
 * traversals for the domino effect, an ArrayList is more performant and easier to manage
 * than a LinkedList or a raw 2D array.
 */

/**
 * Represents the 8x8 grid of boxes in the puzzle game.
 * This class handles the core game physics including the "Domino Effect" rolling mechanism,
 * grid generation, and the application of special tools.
 */
public class BoxGrid {

    /** The dimension of the grid (8x8). */
    public static final int SIZE = 8;

    /** * The 1D list storing all 64 boxes. 
     * Access formula: index = row * SIZE + col.
     */
    private final List<Box> boxes;

    /** Random number generator shared across grid operations. */
    private final Random rng;
    
    /** * Tracks the locations of boxes that moved during the current turn.
     * Required for rule validation: "Player must select a box that was rolled in the previous stage."
     */
    private final List<BoxLocation> rolledThisTurnLocations; 

    /**
     * Initializes the grid with random boxes and prepares the tracking list.
     */
    public BoxGrid() {
        this.rng = new Random();
        this.boxes = new ArrayList<>(SIZE * SIZE);
        this.rolledThisTurnLocations = new ArrayList<>();
        generateInitialGrid();
    }

    /**
     * Retrieves the box at the specified row and column.
     * @param row 0-based row index (0-7)
     * @param col 0-based column index (0-7)
     * @return The Box object, or null if coordinates are invalid.
     */
    public Box getBoxAt(int row, int col) {
        if (!isValid(row, col)) return null;
        return boxes.get(row * SIZE + col);
    }
    
    /**
     * Replaces the box at the specified coordinates.
     * Used mainly by internal shifting logic and Tool operations.
     */
    public void setBoxAt(int row, int col, Box box) {
        if (isValid(row, col)) {
            boxes.set(row * SIZE + col, box);
        }
    }

    public Random rng() {
        return rng;
    }

    /**
     * Generates a random target letter (A-H) for the game goal.
     */
    public char randomTargetLetter() {
        return (char) ('A' + rng.nextInt(8));
    }

    private boolean isValid(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    // =============================================================
    // GRID GENERATION LOGIC
    // =============================================================

    /**
     * Fills the grid with 64 randomly generated boxes according to probability rules.
     */
    private void generateInitialGrid() {
        for (int i = 0; i < SIZE * SIZE; i++) {
            boxes.add(generateRandomBox());
        }
    }

    /**
     * Creates a single new random box. Used during initialization and when
     * new boxes enter the grid from edges during rolling.
     * * Probabilities:
     * - FixedBox: 5%
     * - UnchangingBox: 10%
     * - RegularBox: 85%
     */
    public Box createRandomBox() {
        return generateRandomBox();
    }

    private Box generateRandomBox() {
        double p = rng.nextDouble();
        if (p < 0.05) {
            // 5% chance: FixedBox (Always empty, cannot move)
            return new FixedBox(this, null);
        } else if (p < 0.15) {
            // 10% chance: UnchangingBox (Guaranteed tool, faces don't change)
            return new UnchangingBox(this, generateRandomToolForUnchanging());
        } else {
            // 85% chance: RegularBox
            return new RegularBox(this, generateRandomToolForRegular());
        }
    }

    private SpecialTool generateRandomToolForRegular() {
        // Regular boxes have 75% chance of containing a tool.
        if (rng.nextDouble() >= 0.75) return null;
        return SpecialTool.randomTool(this, rng, 0.15);
    }

    private SpecialTool generateRandomToolForUnchanging() {
        // Unchanging boxes are guaranteed to have a tool.
        return SpecialTool.randomTool(this, rng, 0.20);
    }

    // =============================================================
    // DOMINO EFFECT & PHYSICS ENGINE
    // =============================================================

    /**
     * Executes the main mechanics of the game's "First Stage".
     * Rolls a row or column starting from an edge, propagating the movement
     * until a FixedBox is encountered.
     * * @param row The row index of the selected edge box.
     * @param col The column index of the selected edge box.
     * @param dir The direction to push the box (UP, DOWN, LEFT, RIGHT).
     * @throws UnmovableFixedBoxException If the user tries to push a FixedBox directly.
     */
    public void rollBox(int row, int col, Direction dir) throws UnmovableFixedBoxException {
        // Validation: Movement must start from a valid edge inward.
        if (!isValidMoveFromEdge(row, col, dir)) return;

        Box startBox = getBoxAt(row, col);
        
        // Rule Check: A FixedBox on the edge cannot be moved.
        if (startBox instanceof FixedBox) {
            throw new UnmovableFixedBoxException();
        }

        // Clear previous turn's tracking data.
        rolledThisTurnLocations.clear();

        // Delegate to specific directional logic
        switch (dir) {
            case RIGHT: shiftRowRight(row); break;
            case LEFT:  shiftRowLeft(row); break;
            case DOWN:  shiftColDown(col); break;
            case UP:    shiftColUp(col); break;
        }
    }

    /**
     * Checks if the move is initiated from a valid edge and directed inwards.
     */
    private boolean isValidMoveFromEdge(int r, int c, Direction dir) {
        boolean top = (r == 0);
        boolean bottom = (r == SIZE - 1);
        boolean left = (c == 0);
        boolean right = (c == SIZE - 1);

        if (top && dir == Direction.DOWN) return true;
        if (bottom && dir == Direction.UP) return true;
        if (left && dir == Direction.RIGHT) return true;
        if (right && dir == Direction.LEFT) return true;
        
        return false;
    }

    /**
     * Handles the logic for pushing a row to the RIGHT.
     * Mechanism:
     * 1. Find the first FixedBox (obstacle) from left to right.
     * 2. Shift all boxes between the start and the obstacle one step right.
     * 3. The box immediately to the left of the FixedBox is "crushed/eaten" (overwritten).
     * 4. A new random box enters at the start of the row.
     */
    private void shiftRowRight(int row) throws UnmovableFixedBoxException {
        // 1. Find the limit (FixedBox)
        int limitCol = SIZE;
        for (int c = 0; c < SIZE; c++) {
            if (getBoxAt(row, c) instanceof FixedBox) {
                limitCol = c;
                break;
            }
        }
        
        // Track moved locations for the "Open Box" stage
        for (int c = 0; c < limitCol; c++) {
            // Boxes from 0 to limitCol-1 will move to 1 to limitCol.
            // We track their NEW positions.
            rolledThisTurnLocations.add(new BoxLocation(row, c));
        }

        // 2 & 3. Shift Logic (Iterate backwards to prevent overwriting data we still need)
        // We move boxes from [0...limitCol-2] into [1...limitCol-1].
        // The box at [limitCol-1] is effectively overwritten by [limitCol-2], so it disappears.
        for (int c = limitCol - 1; c > 0; c--) {
            Box movingBox = getBoxAt(row, c - 1);
            movingBox.roll(Direction.RIGHT); // Apply physical rotation
            setBoxAt(row, c, movingBox);
        }
        
        // 4. Insert new box at the start (Index 0)
        // Note: The loop handled c=1 (taking from c=0). So c=0 is now free to receive a new box.
        setBoxAt(row, 0, createRandomBox());
    }

    private void shiftRowLeft(int row) throws UnmovableFixedBoxException {
        // Find obstacle from right to left
        int limitCol = -1;
        for (int c = SIZE - 1; c >= 0; c--) {
            if (getBoxAt(row, c) instanceof FixedBox) {
                limitCol = c;
                break;
            }
        }

        for (int c = SIZE - 1; c > limitCol; c--) {
             rolledThisTurnLocations.add(new BoxLocation(row, c));
        }

        // Shift boxes leftwards
        for (int c = limitCol + 1; c < SIZE - 1; c++) {
            Box movingBox = getBoxAt(row, c + 1);
            movingBox.roll(Direction.LEFT);
            setBoxAt(row, c, movingBox);
        }
        
        // Insert new box at the rightmost end
        setBoxAt(row, SIZE - 1, createRandomBox());
    }

    private void shiftColDown(int col) throws UnmovableFixedBoxException {
        // Find obstacle from top to bottom
        int limitRow = SIZE;
        for (int r = 0; r < SIZE; r++) {
            if (getBoxAt(r, col) instanceof FixedBox) {
                limitRow = r;
                break;
            }
        }
        
        for (int r = 0; r < limitRow; r++) {
            rolledThisTurnLocations.add(new BoxLocation(r, col));
        }

        // Shift boxes downwards
        for (int r = limitRow - 1; r > 0; r--) {
            Box movingBox = getBoxAt(r - 1, col);
            movingBox.roll(Direction.DOWN);
            setBoxAt(r, col, movingBox);
        }
        
        // Insert new box at the top
        setBoxAt(0, col, createRandomBox());
    }

    private void shiftColUp(int col) throws UnmovableFixedBoxException {
        // Find obstacle from bottom to top
        int limitRow = -1;
        for (int r = SIZE - 1; r >= 0; r--) {
            if (getBoxAt(r, col) instanceof FixedBox) {
                limitRow = r;
                break;
            }
        }

        for (int r = SIZE - 1; r > limitRow; r--) {
            rolledThisTurnLocations.add(new BoxLocation(r, col));
        }

        // Shift boxes upwards
        for (int r = limitRow + 1; r < SIZE - 1; r++) {
            Box movingBox = getBoxAt(r + 1, col);
            movingBox.roll(Direction.UP);
            setBoxAt(r, col, movingBox);
        }
        
        // Insert new box at the bottom
        setBoxAt(SIZE - 1, col, createRandomBox());
    }

    /**
     * Checks if a box at specific coordinates was moved during the last roll operation.
     * This is used to validate if a player is allowed to open this box.
     */
    public boolean wasRolledThisTurn(int row, int col) {
        for (BoxLocation loc : rolledThisTurnLocations) {
            if (loc.row() == row && loc.col() == col) return true;
        }
        return false;
    }

    // =============================================================
    // TOOL SUPPORT METHODS
    // =============================================================

    /**
     * Called by BoxFlipper tool. Flips the box vertically (Top becomes Bottom).
     * @throws UnmovableFixedBoxException if the target is a FixedBox.
     */
    public void flipBoxVertically(int row, int col) throws UnmovableFixedBoxException {
        if (!isValid(row, col)) return;
        Box box = getBoxAt(row, col);
        
        if (box instanceof FixedBox) {
            throw new UnmovableFixedBoxException();
        }
        
        // Simulating a vertical flip by rolling UP twice.
        box.roll(Direction.UP);
        box.roll(Direction.UP);
    }

    /**
     * Called by BoxFixer tool. Replaces the current box with a new FixedBox.
     * @throws BoxAlreadyFixedException if the box is already fixed.
     */
    public void convertToFixedBox(int row, int col) throws BoxAlreadyFixedException {
        if (!isValid(row, col)) return;
        Box currentBox = getBoxAt(row, col);
        
        if (currentBox instanceof FixedBox) {
            throw new BoxAlreadyFixedException();
        }

        // Replace with a new FixedBox (effectively removing any tool inside the old box)
        // Ideally, we should copy the faces, but since we don't have access to faces array,
        // we create a new random FixedBox as a fallback.
        FixedBox newFixed = new FixedBox(this, null);
        setBoxAt(row, col, newFixed);
    }

    /**
     * Helper method to stamp a single box. Used by multiple Stamp tools.
     */
    public void stampBox(int row, int col, char targetLetter) {
        if (!isValid(row, col)) return;
        getBoxAt(row, col).stampTop(targetLetter);
    }

    /** Called by MassRowStamp tool. */
    public void stampRow(int row, char targetLetter) {
        if (row < 0 || row >= SIZE) return;
        for (int c = 0; c < SIZE; c++) stampBox(row, c, targetLetter);
    }

    /** Called by MassColumnStamp tool. */
    public void stampColumn(int col, char targetLetter) {
        if (col < 0 || col >= SIZE) return;
        for (int r = 0; r < SIZE; r++) stampBox(r, col, targetLetter);
    }

    /** Called by PlusShapeStamp tool. */
    public void stampPlusShape(int row, int col, char targetLetter) {
        if (!isValid(row, col)) return;
        stampBox(row, col, targetLetter);       // Center
        stampBox(row - 1, col, targetLetter);   // Top
        stampBox(row + 1, col, targetLetter);   // Bottom
        stampBox(row, col - 1, targetLetter);   // Left
        stampBox(row, col + 1, targetLetter);   // Right
    }
    
    /**
     * Calculates the final score by counting how many boxes have the target letter on top.
     */
    public int calculateScore(char targetLetter) {
        int score = 0;
        for (Box box : boxes) {
            if (box.getTopLetter() == targetLetter) score++;
        }
        return score;
    }

    /**
     * Generates the string representation of the grid for the console UI.
     * Format example: | R-E-M | (Regular - Top is E - Mystery/Unopened)
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // Header
        sb.append("      ");
        for (int c = 1; c <= SIZE; c++) sb.append("C").append(c).append("      ");
        sb.append("\n");

        // Rows
        for (int r = 0; r < SIZE; r++) {
            sb.append("R").append(r + 1).append("  ");
            for (int c = 0; c < SIZE; c++) {
                Box b = getBoxAt(r, c);
                // Box Format: | Type-Letter-Status |
                sb.append("| ").append(b.getTypeMarker()).append("-")
                  .append(b.getTopLetter()).append("-");
                
                // Determine Status Marker (O=Open/Empty, M=Mystery, S=SpecialTool found)
                if (b instanceof FixedBox || (b.isOpened() && b.isEmpty())) {
                    sb.append("O");
                } else if (b.isOpened() && !b.isEmpty()) {
                     sb.append("S"); 
                } else {
                    sb.append("M");
                }
                sb.append(" | ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}