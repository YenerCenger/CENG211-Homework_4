package grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import boxes.Box;
import boxes.FixedBox;
import boxes.RegularBox;
import boxes.UnchangingBox;
import exceptions.UnmovableFixedBoxException;
import exceptions.BoxAlreadyFixedException;
import tools.SpecialTool;

/*
ANSWER TO COLLECTIONS QUESTION:
I used an ArrayList<Box> to store the 8x8 grid in a contiguous 1D structure (size 64).
This provides O(1) access via index = row*8 + col. Since the game requires frequent
index-based access (getting neighbors, checking specific coordinates) and row/column
traversals for the domino effect, an ArrayList is more performant and easier to manage
than a LinkedList or a raw 2D array.
*/

public class BoxGrid {

    public static final int SIZE = 8;
    private final List<Box> boxes;
    private final Random rng;
    private final List<BoxLocation> rolledThisTurnLocations; 

    public BoxGrid() {
        this.rng = new Random();
        this.boxes = new ArrayList<>(SIZE * SIZE);
        this.rolledThisTurnLocations = new ArrayList<>();
        generateInitialGrid();
    }

    public Box getBoxAt(int row, int col) {
        if (!isValid(row, col)) return null;
        return boxes.get(row * SIZE + col);
    }
    
    public void setBoxAt(int row, int col, Box box) {
        if (isValid(row, col)) {
            boxes.set(row * SIZE + col, box);
        }
    }

    public Random rng() {
        return rng;
    }

    public char randomTargetLetter() {
        return (char) ('A' + rng.nextInt(8));
    }

    private boolean isValid(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    // --- GRID GENERATION ---
    private void generateInitialGrid() {
        for (int i = 0; i < SIZE * SIZE; i++) {
            boxes.add(generateRandomBox());
        }
    }

    public Box createRandomBox() {
        return generateRandomBox();
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
        if (rng.nextDouble() >= 0.75) return null;
        return SpecialTool.randomTool(this, rng, 0.15);
    }

    private SpecialTool generateRandomToolForUnchanging() {
        return SpecialTool.randomTool(this, rng, 0.20);
    }

    // --- DOMINO LOGIC & ROLLING ---

    public void rollBox(int row, int col, Direction dir) throws UnmovableFixedBoxException {
        if (!isValidMoveFromEdge(row, col, dir)) return;

        Box startBox = getBoxAt(row, col);
        if (startBox instanceof FixedBox) {
            throw new UnmovableFixedBoxException(); // [cite: 53]
        }

        rolledThisTurnLocations.clear();

        switch (dir) {
            case RIGHT: shiftRowRight(row); break;
            case LEFT:  shiftRowLeft(row); break;
            case DOWN:  shiftColDown(col); break;
            case UP:    shiftColUp(col); break;
        }
    }

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

    private void shiftRowRight(int row) throws UnmovableFixedBoxException {
        int limitCol = SIZE;
        for (int c = 0; c < SIZE; c++) {
            if (getBoxAt(row, c) instanceof FixedBox) {
                limitCol = c;
                break;
            }
        }
        
        // Hareket edenleri kaydet (FixedBox HARİÇ)
        // limitCol=3 ise (FixedBox index 3), hareket 0, 1, 2'ye etki eder.
        for (int c = 0; c < limitCol; c++) {
            rolledThisTurnLocations.add(new BoxLocation(row, c));
        }

        // Domino Kaydırma: limitCol-1'den 1'e kadar.
        // limitCol-1'deki kutu "Box at c-1" (limitCol-2) tarafından ezilir.
        // En sağdaki (FixedBox'ın yanındaki) kutu yok olur (PDF'teki 'eating' mantığı)[cite: 160].
        for (int c = limitCol - 1; c > 0; c--) {
            Box movingBox = getBoxAt(row, c - 1);
            movingBox.roll(Direction.RIGHT); // [cite: 78]
            setBoxAt(row, c, movingBox);
        }
        
        // En başa yeni kutu ekle (Döngü 0->1 işlemini yaptı, 0 boşaldı gibi düşünülür)
        setBoxAt(row, 0, createRandomBox());
    }

    private void shiftRowLeft(int row) throws UnmovableFixedBoxException {
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

        // limitCol+1'den SIZE-2'ye kadar olanlar sola kayar.
        for (int c = limitCol + 1; c < SIZE - 1; c++) {
            Box movingBox = getBoxAt(row, c + 1);
            movingBox.roll(Direction.LEFT);
            setBoxAt(row, c, movingBox);
        }
        setBoxAt(row, SIZE - 1, createRandomBox());
    }

    private void shiftColDown(int col) throws UnmovableFixedBoxException {
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

        for (int r = limitRow - 1; r > 0; r--) {
            Box movingBox = getBoxAt(r - 1, col);
            movingBox.roll(Direction.DOWN);
            setBoxAt(r, col, movingBox);
        }
        setBoxAt(0, col, createRandomBox());
    }

    private void shiftColUp(int col) throws UnmovableFixedBoxException {
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

        for (int r = limitRow + 1; r < SIZE - 1; r++) {
            Box movingBox = getBoxAt(r + 1, col);
            movingBox.roll(Direction.UP);
            setBoxAt(r, col, movingBox);
        }
        setBoxAt(SIZE - 1, col, createRandomBox());
    }

    public boolean wasRolledThisTurn(int row, int col) {
        for (BoxLocation loc : rolledThisTurnLocations) {
            if (loc.row() == row && loc.col() == col) return true;
        }
        return false;
    }

    // --- TOOL SUPPORT METHODS ---

    public void flipBoxVertically(int row, int col) throws UnmovableFixedBoxException {
        if (!isValid(row, col)) return;
        Box box = getBoxAt(row, col);
        if (box instanceof FixedBox) throw new UnmovableFixedBoxException(); // [cite: 88]
        
        // Flip = 2x UP Roll
        box.roll(Direction.UP);
        box.roll(Direction.UP);
    }

    public void convertToFixedBox(int row, int col) throws exceptions.BoxAlreadyFixedException {
        if (!isValid(row, col)) return;
        Box currentBox = getBoxAt(row, col);
        if (currentBox instanceof FixedBox) throw new exceptions.BoxAlreadyFixedException(); // [cite: 89]

        // "Identical FixedBox copy" requirement 
        // UYARI: Box sınıfında 'getFaces()' olmadığı için şu an random yaratıyoruz.
        // Yener'in Box sınıfına 'public char[] getFaces()' eklemesi lazım.
        // Şimdilik bu şekilde bırakıyorum:
        FixedBox newFixed = new FixedBox(this, null);
        
        boxes.set(row * SIZE + col, newFixed);
    }

    public void stampBox(int row, int col, char targetLetter) {
        if (!isValid(row, col)) return;
        getBoxAt(row, col).stampTop(targetLetter);
    }

    public void stampRow(int row, char targetLetter) {
        if (row < 0 || row >= SIZE) return;
        for (int c = 0; c < SIZE; c++) stampBox(row, c, targetLetter);
    }

    public void stampColumn(int col, char targetLetter) {
        if (col < 0 || col >= SIZE) return;
        for (int r = 0; r < SIZE; r++) stampBox(r, col, targetLetter);
    }

    public void stampPlusShape(int row, int col, char targetLetter) {
        if (!isValid(row, col)) return;
        stampBox(row, col, targetLetter);
        stampBox(row - 1, col, targetLetter);
        stampBox(row + 1, col, targetLetter);
        stampBox(row, col - 1, targetLetter);
        stampBox(row, col + 1, targetLetter);
    }
    
    public int calculateScore(char targetLetter) {
        int score = 0;
        for (Box box : boxes) {
            if (box.getTopLetter() == targetLetter) score++;
        }
        return score;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("      ");
        for (int c = 1; c <= SIZE; c++) sb.append("C").append(c).append("      ");
        sb.append("\n");

        for (int r = 0; r < SIZE; r++) {
            sb.append("R").append(r + 1).append("  ");
            for (int c = 0; c < SIZE; c++) {
                Box b = getBoxAt(r, c);
                sb.append("| ").append(b.getTypeMarker()).append("-")
                  .append(b.getTopLetter()).append("-");
                
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