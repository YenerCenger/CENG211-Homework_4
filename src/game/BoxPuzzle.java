package game;

import boxes.Box;
import boxes.FixedBox;
import exceptions.EmptyBoxException;
import exceptions.UnmovableFixedBoxException;
import grid.BoxGrid;
import grid.BoxLocation;
import grid.Direction;
import java.util.Scanner;
import tools.SpecialTool;

/**
 * Main game class that manages the BoxPuzzle game.
 * Contains the game logic and the Menu inner class for user interaction.
 */
public class BoxPuzzle {

    private final BoxGrid grid;
    private final char targetLetter;

    /**
     * Initializes a new BoxPuzzle game with a random grid and target letter.
     * Starts the game menu immediately after initialization.
     */
    public BoxPuzzle() {
        this.grid = new BoxGrid();
        this.targetLetter = grid.randomTargetLetter();
        new Menu().run();
    }

    /**
     * Returns the target letter that players need to maximize on box tops.
     * 
     * @return The target letter (A-H)
     */
    public char getTargetLetter() {
        return targetLetter;
    }

    /**
     * Returns the game grid.
     * 
     * @return The BoxGrid instance
     */
    public BoxGrid getGrid() {
        return grid;
    }

    /**
     * Generic method to acquire and use a SpecialTool.
     * 
     * @param <T>  Type extending SpecialTool
     * @param tool The tool to use
     * @param row  Target row for the tool
     * @param col  Target column for the tool
     */
    public <T extends SpecialTool> void acquireAndUseTool(T tool, int row, int col)
            throws exceptions.UnmovableFixedBoxException, exceptions.BoxAlreadyFixedException {
        tool.useTool(this, row, col);
    }

    /**
     * Inner class that handles all menu operations and user interactions.
     * Manages the game loop, input parsing, and display functions.
     */
    private class Menu {

        private final Scanner scanner;
        private static final int MAX_TURNS = 5;
        private int currentTurn = 1;

        /**
         * Initializes the Menu with a Scanner for user input.
         */
        public Menu() {
            this.scanner = new Scanner(System.in);
        }

        /**
         * Main game loop. Runs for up to 5 turns or until no valid moves remain.
         */
        public void run() {
            int turn = 0;

            // Display welcome message and initial grid
            printWelcome();

            // Main game loop - 5 turns maximum
            while (turn < MAX_TURNS) {
                currentTurn = turn + 1;

                System.out.println("\n=====> TURN " + currentTurn + ":");

                // Check if any valid moves exist (edge boxes that are not all FixedBox)
                if (!hasValidEdgeBox()) {
                    System.out.println("\nFAILURE: No available moves. All edge boxes are fixed.");
                    break;
                }

                // Observe Box ability at the start of each turn
                handleObserveBoxAbility();

                System.out.println("Continuing to the first stage...");

                // STAGE 1: Roll Operation
                boolean rollSuccess = handleStage1Roll();

                if (!rollSuccess) {
                    // Turn was wasted due to FixedBox selection
                    turn++;
                    continue;
                }

                // STAGE 2: Open Box and Use Tool
                boolean openSuccess = handleStage2Open();

                if (!openSuccess) {
                    // Turn was wasted due to empty box
                    turn++;
                    continue;
                }

                // Turn completed successfully
                turn++;
            }

            // Game End
            printGameEnd();
            scanner.close();
        }

        /**
         * Prints the welcome message and game instructions.
         */
        private void printWelcome() {
            System.out.println("Welcome to Box Top Side Matching Puzzle App. An 8x8 box grid is being generated.");
            System.out.println(
                    "\nYour goal is to maximize the letter \"" + targetLetter + "\" on the top sides of the boxes.");
            System.out.println("\nThe initial state of the box grid:\n");
            printGrid();
        }

        /**
         * Prints the current state of the grid.
         */
        private void printGrid() {
            System.out.println(grid.toString());
        }

        /**
         * Prompts the user for input and returns the response.
         * 
         * @param prompt The message to display to the user
         * @return The user's input string
         */
        private String getInput(String prompt) {
            System.out.print(prompt);
            return scanner.nextLine().trim();
        }

        /**
         * Parses user input in format "R1-C1" or "r1-c1" to a Coordinate.
         * Case-insensitive parsing.
         * 
         * @param input The user input string
         * @return A Coordinate object with 0-based row and col, or null if invalid
         */
        private Coordinate parseInput(String input) {
            try {
                BoxLocation loc = BoxLocation.parse(input);
                return new Coordinate(loc.row(), loc.col());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        /**
         * Checks if the given coordinate is on the edge of the grid.
         * 
         * @param row 0-based row index
         * @param col 0-based column index
         * @return true if the coordinate is on an edge
         */
        private boolean isEdge(int row, int col) {
            return row == 0 || row == BoxGrid.SIZE - 1 ||
                    col == 0 || col == BoxGrid.SIZE - 1;
        }

        /**
         * Checks if the given coordinate is a corner.
         * 
         * @param row 0-based row index
         * @param col 0-based column index
         * @return true if the coordinate is a corner
         */
        private boolean isCorner(int row, int col) {
            boolean isTopOrBottom = (row == 0 || row == BoxGrid.SIZE - 1);
            boolean isLeftOrRight = (col == 0 || col == BoxGrid.SIZE - 1);
            return isTopOrBottom && isLeftOrRight;
        }

        /**
         * Returns the valid direction(s) for rolling from an edge position.
         * Non-corner edges have only one valid direction (inward).
         * Corners can go in two directions.
         * 
         * @param row 0-based row index
         * @param col 0-based column index
         * @return Array of valid directions
         */
        private Direction[] getValidDirections(int row, int col) {
            boolean top = row == 0;
            boolean bottom = row == BoxGrid.SIZE - 1;
            boolean left = col == 0;
            boolean right = col == BoxGrid.SIZE - 1;

            // Corners have two options - order matches example output
            if (top && left)
                return new Direction[] { Direction.RIGHT, Direction.DOWN };
            if (top && right)
                return new Direction[] { Direction.LEFT, Direction.DOWN };
            if (bottom && left)
                return new Direction[] { Direction.RIGHT, Direction.UP };
            if (bottom && right)
                return new Direction[] { Direction.LEFT, Direction.UP };

            // Non-corner edges have one option
            if (top)
                return new Direction[] { Direction.DOWN };
            if (bottom)
                return new Direction[] { Direction.UP };
            if (left)
                return new Direction[] { Direction.RIGHT };
            if (right)
                return new Direction[] { Direction.LEFT };

            return new Direction[] {}; // Should not reach here for valid edge
        }

        /**
         * Checks if there is at least one valid edge box (non-FixedBox) to select.
         * 
         * @return true if at least one valid move exists
         */
        private boolean hasValidEdgeBox() {
            // Check top and bottom rows
            for (int c = 0; c < BoxGrid.SIZE; c++) {
                if (!(grid.getBoxAt(0, c) instanceof FixedBox))
                    return true;
                if (!(grid.getBoxAt(BoxGrid.SIZE - 1, c) instanceof FixedBox))
                    return true;
            }
            // Check left and right columns (excluding corners already checked)
            for (int r = 1; r < BoxGrid.SIZE - 1; r++) {
                if (!(grid.getBoxAt(r, 0) instanceof FixedBox))
                    return true;
                if (!(grid.getBoxAt(r, BoxGrid.SIZE - 1) instanceof FixedBox))
                    return true;
            }
            return false;
        }

        /**
         * Handles the "observe box" ability at the start of each turn.
         * Player can choose to see all 6 faces of a box in a cube diagram.
         */
        private void handleObserveBoxAbility() {
            while (true) {
                String choice = getInput("---> Do you want to view all surfaces of a box? [1] Yes or [2] No? ");

                if (choice.equals("2") || choice.equalsIgnoreCase("no") || choice.equalsIgnoreCase("n")) {
                    break;
                } else if (choice.equals("1") || choice.equalsIgnoreCase("yes") || choice.equalsIgnoreCase("y")) {
                    while (true) {
                        String locInput = getInput("Please enter the location of the box you want to view: ");
                        Coordinate coord = parseInput(locInput);

                        if (coord == null || !isValidCoordinate(coord.row, coord.col)) {
                            System.out.println("Invalid location. Please try again.");
                            continue;
                        }

                        displayBoxCube(coord.row, coord.col);
                        break;
                    }
                    break; // Only one observe per turn
                } else {
                    System.out.println("Please enter 1 or 2.");
                }
            }
        }

        /**
         * Checks if the coordinate is within grid bounds.
         */
        private boolean isValidCoordinate(int row, int col) {
            return row >= 0 && row < BoxGrid.SIZE && col >= 0 && col < BoxGrid.SIZE;
        }

        /**
         * Displays a box in cube diagram format showing all 6 faces.
         * Format matches the example output exactly.
         */
        private void displayBoxCube(int row, int col) {
            Box box = grid.getBoxAt(row, col);
            char[][] layout = box.getFlatLayout();

            System.out.println("    -----");
            System.out.println("    | " + layout[0][1] + " |"); // Front (top of diagram)
            System.out.println("-------------");
            System.out.println("| " + layout[1][0] + " | " + layout[1][1] + " | " + layout[1][2] + " |"); // Left, Top,
                                                                                                          // Right
            System.out.println("-------------");
            System.out.println("    | " + layout[2][1] + " |"); // Bottom
            System.out.println("    -----");
            System.out.println("    | " + layout[3][1] + " |"); // Back
            System.out.println("    -----\n");
        }

        /**
         * Handles Stage 1: Edge Box Selection and Rolling.
         * 
         * @return true if roll was successful, false if turn was wasted
         */
        private boolean handleStage1Roll() {
            System.out.println("---> TURN " + currentTurn + " - FIRST STAGE:");

            while (true) {
                String input = getInput("Please enter the location of the edge box you want to roll: ");
                Coordinate coord = parseInput(input);

                // Validate coordinate format
                if (coord == null) {
                    System.out.println("INCORRECT INPUT: Invalid format. Please reenter the location: ");
                    continue;
                }

                // Validate coordinate bounds
                if (!isValidCoordinate(coord.row, coord.col)) {
                    System.out.println("INCORRECT INPUT: Location out of bounds. Please reenter the location: ");
                    continue;
                }

                // Check if it's an edge
                if (!isEdge(coord.row, coord.col)) {
                    System.out.println(
                            "INCORRECT INPUT: The chosen box is not on any of the edges. Please reenter the location: ");
                    continue;
                }

                // Get the box and check if it's a FixedBox
                Box selectedBox = grid.getBoxAt(coord.row, coord.col);

                if (selectedBox instanceof FixedBox) {
                    System.out.println("HOWEVER, IT IS FIXED BOX AND CANNOT BE MOVED. Continuing to the next turn...");
                    return false;
                }

                // Determine roll direction
                Direction rollDir = selectRollDirection(coord.row, coord.col);

                if (rollDir == null) {
                    continue; // User provided invalid direction, ask again
                }

                // Execute the roll
                try {
                    boolean hitFixedBox = grid.rollBox(coord.row, coord.col, rollDir);
                    printRollMessage(rollDir, hitFixedBox);
                    printGrid();
                    return true;
                } catch (UnmovableFixedBoxException e) {
                    // This happens when the starting box itself is a FixedBox
                    // (Already handled above, but keep as safety)
                    System.out.println("HOWEVER, IT IS FIXED BOX AND CANNOT BE MOVED. Continuing to the next turn...");
                    return false;
                }
            }
        }

        /**
         * Prints a message describing the roll action.
         */
        private void printRollMessage(Direction dir, boolean hitFixedBox) {
            String dirWord = getDirectionWord(dir);
            if (hitFixedBox) {
                System.out.println("The chosen box and any box on its path have been rolled to " + dirWord
                        + " until a FixedBox has been reached. The new state of the box grid:\n");
            } else {
                System.out.println("The chosen box and any box on its path have been rolled to " + dirWord
                        + ". The new state of the box grid:\n");
            }
        }

        /**
         * Converts Direction enum to readable word.
         */
        private String getDirectionWord(Direction dir) {
            switch (dir) {
                case UP:
                    return "upwards";
                case DOWN:
                    return "downwards";
                case LEFT:
                    return "left";
                case RIGHT:
                    return "right";
                default:
                    return dir.toString().toLowerCase();
            }
        }

        /**
         * Asks the user to select a roll direction if the box is at a corner.
         * For non-corner edges, automatically returns the only valid direction.
         * 
         * @param row 0-based row index
         * @param col 0-based column index
         * @return The selected Direction, or null if invalid input
         */
        private Direction selectRollDirection(int row, int col) {
            Direction[] validDirs = getValidDirections(row, col);

            if (validDirs.length == 1) {
                // Non-corner edge: only one direction, print automatic message
                String dirWord = getDirectionWord(validDirs[0]);
                System.out.println("The chosen box is automatically rolled to " + dirWord + ".");
                return validDirs[0];
            }

            // Corner: ask user to choose
            String dir1Word = getDirectionWord(validDirs[0]);
            String dir2Word = getDirectionWord(validDirs[1]);

            while (true) {
                String choice = getInput(
                        "The chosen box can be rolled to either [1] " + dir1Word + " or [2] " + dir2Word + ": ");

                if (choice.equals("1")) {
                    return validDirs[0];
                } else if (choice.equals("2")) {
                    return validDirs[1];
                } else {
                    System.out.println("Invalid choice. Please enter 1 or 2.");
                }
            }
        }

        /**
         * Handles Stage 2: Opening a box and using the tool inside.
         * 
         * @return true if stage was successful, false if turn was wasted
         */
        private boolean handleStage2Open() {
            System.out.println("---> TURN " + currentTurn + " - SECOND STAGE:");

            while (true) {
                String input = getInput("Please enter the location of the box you want to open: ");
                Coordinate coord = parseInput(input);

                // Validate coordinate format
                if (coord == null) {
                    System.out.println("INCORRECT INPUT: Invalid format. Please reenter the location: ");
                    continue;
                }

                // Validate coordinate bounds
                if (!isValidCoordinate(coord.row, coord.col)) {
                    System.out.println("INCORRECT INPUT: Location out of bounds. Please reenter the location: ");
                    continue;
                }

                // Check if this box was rolled this turn
                if (!grid.wasRolledThisTurn(coord.row, coord.col)) {
                    System.out.println(
                            "INCORRECT INPUT: The chosen box was not rolled during the first stage. Please reenter the location: ");
                    continue;
                }

                // Get the box
                Box box = grid.getBoxAt(coord.row, coord.col);

                // Check if already opened
                if (box.isOpen()) {
                    System.out.println(
                            "INCORRECT INPUT: This box has already been opened. Please reenter the location: ");
                    continue;
                }

                // Open the box and try to get the tool
                try {
                    SpecialTool tool = box.open();
                    String toolName = tool.getClass().getSimpleName();
                    System.out.println("\nThe box on location R" + (coord.row + 1) + "-C" + (coord.col + 1)
                            + " is opened. It contains a SpecialTool --> " + toolName);

                    // Use the tool
                    boolean toolSuccess = handleToolUsage(tool, coord.row, coord.col);
                    return toolSuccess;

                } catch (EmptyBoxException e) {
                    System.out.println("\nBOX IS EMPTY! Continuing to the next turn...\n");
                    return false;
                }
            }
        }

        /**
         * Handles the usage of a SpecialTool by getting target coordinates from user.
         * 
         * @param tool      The tool to use
         * @param openedRow The row where the box was opened (unused, kept for potential
         *                  future use)
         * @param openedCol The column where the box was opened (unused, kept for
         *                  potential future use)
         * @return true if tool was used successfully, false if turn was wasted due to
         *         exception
         */
        @SuppressWarnings("unused")
        private boolean handleToolUsage(SpecialTool tool, int openedRow, int openedCol) {
            // Get target coordinates based on tool type
            if (tool instanceof tools.MassRowStamp) {
                // MassRowStamp needs only row
                while (true) {
                    String input = getInput("Please enter the row to use this SpecialTool (e.g., 1 or R1): ");
                    try {
                        // "R" veya "r" harfini temizleyip sadece sayıyı alıyoruz
                        String cleanInput = input.toUpperCase().replace("R", "").trim();
                        int row = Integer.parseInt(cleanInput) - 1;

                        if (row >= 0 && row < BoxGrid.SIZE) {
                            try {
                                acquireAndUseTool(tool, row, 0);
                                System.out.println("\nAll boxes in row " + (row + 1) + " have been stamped to letter \""
                                        + targetLetter + "\". The new state of the box grid:\n");
                                printGrid();
                                return true;
                            } catch (exceptions.UnmovableFixedBoxException | exceptions.BoxAlreadyFixedException e) {
                                System.out.println("\n" + e.getMessage() + " Turn wasted.");
                                return false;
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Hata mesajına devam et
                    }
                    System.out.println("Invalid row. Please enter a valid row number (1-8).");
                }

            } else if (tool instanceof tools.MassColumnStamp) {
                // MassColumnStamp needs only column
                while (true) {
                    String input = getInput("Please enter the column to use this SpecialTool (e.g., 1 or C1): ");
                    try {
                        // "C" veya "c" harfini temizleyip sadece sayıyı alıyoruz
                        String cleanInput = input.toUpperCase().replace("C", "").trim();
                        int col = Integer.parseInt(cleanInput) - 1;

                        if (col >= 0 && col < BoxGrid.SIZE) {
                            try {
                                acquireAndUseTool(tool, 0, col);
                                System.out.println(
                                        "\nAll boxes in column " + (col + 1) + " have been stamped to letter \""
                                                + targetLetter + "\". The new state of the box grid:\n");
                                printGrid();
                                return true;
                            } catch (exceptions.UnmovableFixedBoxException | exceptions.BoxAlreadyFixedException e) {
                                System.out.println("\n" + e.getMessage() + " Turn wasted.");
                                return false;
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Hata mesajına devam et
                    }
                    System.out.println("Invalid column. Please enter a valid column number (1-8).");
                }

            } else if (tool instanceof tools.PlusShapeStamp) {
                // PlusShapeStamp needs row and column
                while (true) {
                    String input = getInput("Please enter the location of the box to use this SpecialTool: ");
                    Coordinate coord = parseInput(input);

                    if (coord != null && isValidCoordinate(coord.row, coord.col)) {
                        try {
                            acquireAndUseTool(tool, coord.row, coord.col);
                            System.out.println("\nTop sides of the chosen box (R" + (coord.row + 1) + "-C"
                                    + (coord.col + 1) + ") and its surrounding boxes have been stamped to letter \""
                                    + targetLetter + "\". The new state of the box grid:\n");
                            printGrid();
                            return true;
                        } catch (exceptions.UnmovableFixedBoxException | exceptions.BoxAlreadyFixedException e) {
                            System.out.println("\n" + e.getMessage() + " Turn wasted.");
                            return false;
                        }
                    }
                    System.out.println("Invalid location. Please use R#-C# format.");
                }

            } else if (tool instanceof tools.BoxFlipper) {
                // BoxFlipper needs row and column
                while (true) {
                    String input = getInput("Please enter the location of the box to use this SpecialTool: ");
                    Coordinate coord = parseInput(input);

                    if (coord != null && isValidCoordinate(coord.row, coord.col)) {
                        try {
                            acquireAndUseTool(tool, coord.row, coord.col);
                            System.out
                                    .println("\nThe chosen box on location R" + (coord.row + 1) + "-C" + (coord.col + 1)
                                            + " has been flipped upside down. The new state of the box grid:\n");
                            printGrid();
                            return true;
                        } catch (exceptions.UnmovableFixedBoxException | exceptions.BoxAlreadyFixedException e) {
                            System.out.println("\n" + e.getMessage() + " Turn wasted.");
                            return false;
                        }
                    }
                    System.out.println("Invalid location. Please use R#-C# format.");
                }

            } else if (tool instanceof tools.BoxFixer) {
                // BoxFixer needs row and column
                while (true) {
                    String input = getInput("Please enter the location of the box to use this SpecialTool: ");
                    Coordinate coord = parseInput(input);

                    if (coord != null && isValidCoordinate(coord.row, coord.col)) {
                        try {
                            acquireAndUseTool(tool, coord.row, coord.col);
                            System.out
                                    .println("\nThe chosen box on location R" + (coord.row + 1) + "-C" + (coord.col + 1)
                                            + " has been replaced with a FixedBox. The new state of the box grid:\n");
                            printGrid();
                            return true;
                        } catch (exceptions.UnmovableFixedBoxException | exceptions.BoxAlreadyFixedException e) {
                            System.out.println("\n" + e.getMessage() + " Turn wasted.");
                            return false;
                        }
                    }
                    System.out.println("Invalid location. Please use R#-C# format.");
                }
            }
            // This should never be reached, but required for compiler
            return false;
        }

        /**
         * Prints the end game results and score.
         */
        private void printGameEnd() {
            System.out.println("\n******* GAME OVER *******\n");
            System.out.println("The final state of the box grid:\n");

            printGrid();

            int score = grid.calculateScore(targetLetter);
            System.out
                    .println("THE TOTAL NUMBER OF TARGET LETTER \"" + targetLetter + "\" IN THE BOX GRID --> " + score);
            System.out.println("\nThe game has been SUCCESSFULLY completed!");
        }

        /**
         * Simple inner class to represent a coordinate pair.
         * Used for parsing and passing row/col values.
         */
        private class Coordinate {
            final int row;
            final int col;

            Coordinate(int row, int col) {
                this.row = row;
                this.col = col;
            }
        }
    }
}
