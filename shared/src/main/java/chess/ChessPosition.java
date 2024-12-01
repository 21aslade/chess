package chess;

import chess.Util.IntPair;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public record ChessPosition(int row, int col) {
    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() { return this.row; }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return this.col;
    }

    public ChessPosition add(IntPair offset) {
        return new ChessPosition(this.row + offset.a(), this.col + offset.b());
    }

    private static final String COLUMNS = "abcdefgh";

    @Override
    public String toString() {
        if (this.col > COLUMNS.length() || this.col < 1) {
            return "ChessPosition(" + this.col + ", " + this.row + ")";
        }
        return String.format("%c%d", COLUMNS.charAt(this.col - 1), this.row);
    }

    public static ChessPosition fromString(String s) {
        if (s.length() != 2) {
            throw new IllegalArgumentException(s + " is not a valid chess position");
        }

        var col = COLUMNS.indexOf(s.charAt(0)) + 1;
        var row = Character.digit(s.charAt(1), 10);
        if (col == 0 || row == -1) {
            throw new IllegalArgumentException(s + " is not a valid chess position");
        }

        return new ChessPosition(row, col);
    }
}
