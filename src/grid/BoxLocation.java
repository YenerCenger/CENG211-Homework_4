package grid;

import java.util.Locale;

public final class BoxLocation {
    private final int row; // 0..7
    private final int col; // 0..7

    public BoxLocation(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int row() { return row; }
    public int col() { return col; }

    @Override
    public String toString() {
        return "R" + (row + 1) + "-C" + (col + 1);
    }

    public static BoxLocation parse(String input) {
        if (input == null) throw new IllegalArgumentException("null input");
        String s = input.trim().toUpperCase(Locale.ROOT);

        // Accept "R2-C4"
        if (s.matches("R\d\s*-\s*C\d")) {
            s = s.replace(" ", "");
            int r = Integer.parseInt(s.substring(1, 2)) - 1;
            int c = Integer.parseInt(s.substring(s.indexOf('C') + 1, s.indexOf('C') + 2)) - 1;
            return new BoxLocation(r, c);
        }

        // Accept "2-4"
        if (s.matches("\d\s*-\s*\d")) {
            s = s.replace(" ", "");
            String[] parts = s.split("-");
            int r = Integer.parseInt(parts[0]) - 1;
            int c = Integer.parseInt(parts[1]) - 1;
            return new BoxLocation(r, c);
        }

        throw new IllegalArgumentException("Invalid location format: " + input);
    }
}
