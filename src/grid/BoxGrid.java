package grid;

import boxes.Box;
import boxes.FixedBox;
import boxes.RegularBox;
import boxes.UnchangingBox;
import exceptions.BoxAlreadyFixedException;
import exceptions.UnmovableFixedBoxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import tools.SpecialTool;

/*
 * ANSWER TO COLLECTIONS QUESTION:
 * I used an ArrayList<Box> to store the 8x8 grid in a contiguous 1D structure (size 64).
 * This provides O(1) access via index = row * 8 + col. Since the game requires frequent
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

    /**
     * Checks if the coordinates are within grid boundaries.
     */
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
     * Probabilities:
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
            return new FixedBox(); 
        } else if (p < 0.15) {
            // 10% chance: UnchangingBox (Guaranteed tool, faces don't change)
            UnchangingBox uBox = new UnchangingBox();
            uBox.setTool(generateRandomToolForUnchanging());
            return uBox;
        } else {
            // 85% chance: RegularBox
            RegularBox rBox = new RegularBox();
            rBox.setTool(generateRandomToolForRegular());
            return rBox;
        }
    }

    // FIXED METHOD
    private SpecialTool generateRandomToolForRegular() {
        // Regular boxes have 75% chance of containing a tool.
        // If we are in the 75% bracket, we must distribute the 5 tools evenly.
        // Target: 15% absolute chance per tool.
        // Calculation: 15% / 75% = 20% (0.20) conditional chance.
        
        if (rng.nextDouble() >= 0.75) return null;
        
        // CORRECTION: Change 0.15 to 0.20
        return SpecialTool.randomTool(this, rng, 0.20);
    }

    // This method remains UNCHANGED as UnchangingBox has 100% tool chance
    // and requires 20% per tool (5 * 20 = 100). 
    private SpecialTool generateRandomToolForUnchanging() {
        return SpecialTool.randomTool(this, rng, 0.20);
    }

    // =============================================================
    // DOMINO EFFECT & PHYSICS ENGINE
    // =============================================================

    /**
     * Executes the main mechanics of the game's "First Stage".
     * Rolls a row or column starting from an edge, propagating the movement
     * until a FixedBox is encountered (Domino Effect).
     * * @param row The row index of the selected edge box.
     * @param col The column index of the selected edge box.
     * @param dir The direction to push the box (UP, DOWN, LEFT, RIGHT).
     * @return true if a FixedBox was encountered on the path, false otherwise.
     * @throws UnmovableFixedBoxException If the user tries to push a FixedBox directly.
     */
    public boolean rollBox(int row, int col, Direction dir) throws UnmovableFixedBoxException {
        // Validation: Movement must start from a valid edge inward.
        if (!isValidMoveFromEdge(row, col, dir)) return false;

        Box startBox = getBoxAt(row, col);
        
        // Rule Check: A FixedBox on the edge cannot be moved.
        if (startBox instanceof FixedBox) {
            throw new UnmovableFixedBoxException();
        }

        // Clear previous turn's tracking data.
        rolledThisTurnLocations.clear();

        // Delegate to specific directional logic
        switch (dir) {
            case RIGHT: return shiftRowRight(row);
            case LEFT:  return shiftRowLeft(row);
            case DOWN:  return shiftColDown(col);
            case UP:    return shiftColUp(col);
        }
        return false;
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
     * @return true if a FixedBox was encountered, false otherwise.
     */
    private boolean shiftRowRight(int row) throws UnmovableFixedBoxException {
        // 1. Find the limit (FixedBox)
        int limitCol = SIZE;
        boolean foundFixedBox = false;
        for (int c = 0; c < SIZE; c++) {
            if (getBoxAt(row, c) instanceof FixedBox) {
                limitCol = c;
                foundFixedBox = true;
                break;
            }
        }

        // 2 & 3. Shift Logic (Iterate backwards to prevent overwriting data we still need)
        // We move boxes from [0...limitCol-2] into [1...limitCol-1].
        // The box at [limitCol-1] is effectively overwritten by [limitCol-2].
        for (int c = limitCol - 1; c > 0; c--) {
            Box movingBox = getBoxAt(row, c - 1);
            movingBox.roll(Direction.RIGHT); // Apply physical rotation
            setBoxAt(row, c, movingBox);
        }
        
        // 4. Insert new box at the start (Index 0)
        setBoxAt(row, 0, createRandomBox());
        
        // Track moved locations AFTER shift - these are the NEW positions
        // Boxes that were at 0..limitCol-2 are now at 1..limitCol-1
        // Also include position 0 since a new box entered there
        for (int c = 0; c < limitCol; c++) {
            rolledThisTurnLocations.add(new BoxLocation(row, c));
        }
        
        return foundFixedBox;
    }

    /**
     * @return true if a FixedBox was encountered, false otherwise.
     */
    private boolean shiftRowLeft(int row) throws UnmovableFixedBoxException {
        // Find obstacle from right to left
        int limitCol = -1;
        boolean foundFixedBox = false;
        for (int c = SIZE - 1; c >= 0; c--) {
            if (getBoxAt(row, c) instanceof FixedBox) {
                limitCol = c;
                foundFixedBox = true;
                break;
            }
        }

        // Shift boxes leftwards
        for (int c = limitCol + 1; c < SIZE - 1; c++) {
            Box movingBox = getBoxAt(row, c + 1);
            movingBox.roll(Direction.LEFT);
            setBoxAt(row, c, movingBox);
        }
        
        // Insert new box at the rightmost end
        setBoxAt(row, SIZE - 1, createRandomBox());
        
        // Track moved locations AFTER shift
        for (int c = SIZE - 1; c > limitCol; c--) {
             rolledThisTurnLocations.add(new BoxLocation(row, c));
        }
        
        return foundFixedBox;
    }

    /**
     * @return true if a FixedBox was encountered, false otherwise.
     */
    private boolean shiftColDown(int col) throws UnmovableFixedBoxException {
        // Find obstacle from top to bottom
        int limitRow = SIZE;
        boolean foundFixedBox = false;
        for (int r = 0; r < SIZE; r++) {
            if (getBoxAt(r, col) instanceof FixedBox) {
                limitRow = r;
                foundFixedBox = true;
                break;
            }
        }

        // Shift boxes downwards
        for (int r = limitRow - 1; r > 0; r--) {
            Box movingBox = getBoxAt(r - 1, col);
            movingBox.roll(Direction.DOWN);
            setBoxAt(r, col, movingBox);
        }
        
        // Insert new box at the top
        setBoxAt(0, col, createRandomBox());
        
        // Track moved locations AFTER shift
        for (int r = 0; r < limitRow; r++) {
            rolledThisTurnLocations.add(new BoxLocation(r, col));
        }
        
        return foundFixedBox;
    }

    /**
     * @return true if a FixedBox was encountered, false otherwise.
     */
    private boolean shiftColUp(int col) throws UnmovableFixedBoxException {
        // Find obstacle from bottom to top
        int limitRow = -1;
        boolean foundFixedBox = false;
        for (int r = SIZE - 1; r >= 0; r--) {
            if (getBoxAt(r, col) instanceof FixedBox) {
                limitRow = r;
                foundFixedBox = true;
                break;
            }
        }

        // Shift boxes upwards
        for (int r = limitRow + 1; r < SIZE - 1; r++) {
            Box movingBox = getBoxAt(r + 1, col);
            movingBox.roll(Direction.UP);
            setBoxAt(r, col, movingBox);
        }
        
        // Insert new box at the bottom
        setBoxAt(SIZE - 1, col, createRandomBox());
        
        // Track moved locations AFTER shift
        for (int r = SIZE - 1; r > limitRow; r--) {
            rolledThisTurnLocations.add(new BoxLocation(r, col));
        }
        
        return foundFixedBox;
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
        FixedBox newFixed = new FixedBox(currentBox);
        setBoxAt(row, col, newFixed);
    }

    /**
     * Helper method to stamp a single box. Used by multiple Stamp tools.
     */
    public void stampBox(int row, int col, char targetLetter) {
        if (!isValid(row, col)) return;
        // Using 'setTop' as defined in Yener's Box class
        getBoxAt(row, col).setTop(targetLetter);
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
            // Using 'getTop' as defined in Yener's Box class
            if (box.getTop() == targetLetter) score++;
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
        // Column Headers - each box is 9 chars wide: "| X-Y-Z |"
        // Row label is "R1  " = 4 chars, so header starts with 4 spaces
        sb.append("    ");
        for (int c = 1; c <= SIZE; c++) {
            // Each header cell should be 9 chars to match box width
            // "   C1    " = 9 chars (4 spaces + C + digit + 3 spaces for C1-C8)
            // But C1-C9 have different widths, so use format
            String header = String.format("   C%-5d", c);
            sb.append(header);
        }
        sb.append("\n");
        
        // Separator line (4 leading + 9*8 = 76 chars)
        sb.append("    ");
        for (int i = 0; i < SIZE * 9; i++) {
            sb.append("-");
        }
        sb.append("\n");

        // Rows
        for (int r = 0; r < SIZE; r++) {
            sb.append("R").append(r + 1).append("  ");
            for (int c = 0; c < SIZE; c++) {
                Box b = getBoxAt(r, c);
                // Box Format: | Type-Letter-Status |
                sb.append("| ").append(b.getTypeMarker()).append("-")
                  .append(b.getTop()).append("-");
                
                // Determine Status Marker 
                // O = Open/Empty or Fixed
                // M = Mystery (Closed)
                if (b instanceof FixedBox || b.isOpen()) {
                    sb.append("O");
                } else {
                    sb.append("M");
                }
                sb.append(" |");
            }
            sb.append("\n");
            // Separator after each row
            sb.append("    ");
            for (int i = 0; i < SIZE * 9; i++) {
                sb.append("-");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}