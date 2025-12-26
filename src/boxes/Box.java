package boxes;

import exceptions.*;
import grid.Direction;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import tools.SpecialTool;

public abstract class Box {
    private boolean isOpen;

    private SpecialTool tool; // null means empty

    private char front, back, left, right, top, bottom;

    private static final Random RAND = new Random();

    // ADDED: Protected constructor for copying faces
    protected Box(Box other) {
        this.front = other.front;
        this.back = other.back;
        this.left = other.left;
        this.right = other.right;
        this.top = other.top;
        this.bottom = other.bottom;
        this.isOpen = false; // FixedBox should start closed/reset
        this.tool = null;    // FixedBox cannot have tools
    }

    public Box() {
        this.isOpen = false;
        assignRandomLetters();
    }

    public char getFront() {
        return front;
    }

    public char getBack() {
        return back;
    }

    public char getLeft() {
        return left;
    }

    public char getRight() {
        return right;
    }

    public char getTop() {
        return top;
    }

    public char getBottom() {
        return bottom;
    }

    public void setTop(char letter) {
        this.top = letter;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public SpecialTool open() throws EmptyBoxException {
        this.isOpen = true;
        if (tool == null) {
            throw new EmptyBoxException();
        }
        SpecialTool temp = tool;
        tool = null;
        return temp;
    }

    private void assignRandomLetters() {
        // assign 6 random letters to the box A to H
        char[] letters = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };
        // How many of each letter are in the box?
        Map<Character, Integer> counts = new HashMap<>();
        // Choose 6 letters from the array letters
        char[] chosen = new char[6];

        for (int i = 0; i < 6; i++) {
            char candidate;
            // RULE: Each letter can appear at most twice on a box [cite: 14]
            // Keep searching until we find a suitable letter
            while (true) {
                candidate = letters[RAND.nextInt(letters.length)];
                int currentCount = counts.getOrDefault(candidate, 0);
                if (currentCount < 2) {
                    counts.put(candidate, currentCount + 1);
                    break; // Found a suitable letter, exit loop
                }
            }
            chosen[i] = candidate;
        }

        this.front = chosen[0];
        this.back = chosen[1];
        this.left = chosen[2];
        this.right = chosen[3];
        this.top = chosen[4];
        this.bottom = chosen[5];
    }

    public void setTool(SpecialTool tool) {
        this.tool = tool;
    }

    public SpecialTool getTool() {
        return this.tool;
    }

    // decendent classes must override this method
    public abstract char getTypeMarker();

    public void roll(Direction direction) throws UnmovableFixedBoxException {
        char temp;
        switch (direction) {
            case LEFT:
                temp = bottom;
                bottom = left;
                left = top;
                top = right;
                right = temp;
                break;
            case RIGHT:
                temp = bottom;
                bottom = right;
                right = top;
                top = left;
                left = temp;
                break;
            case UP:
                temp = bottom;
                bottom = front;
                front = top;
                top = back;
                back = temp;
                break;
            case DOWN:
                temp = bottom;
                bottom = back;
                back = top;
                top = front;
                front = temp;
                break;
        }
    }

    public String getStatusString() {
        // M: Mystery (Kapalı), O: Open (Açık/Boş)
        // Eğer kutu açıksa "O", kapalıysa "M"
        String status = this.isOpen ? "O" : "M";

        // Format: | TYPE - TOP_CHAR - STATUS |
        return "| " + getTypeMarker() + "-" + top + "-" + status + " |";
    }

    public char[][] getFlatLayout() {
        return new char[][] {
                { ' ', front, ' ' },
                { left, top, right },
                { ' ', bottom, ' ' },
                { ' ', back, ' ' }
        };
    }

}