package chess;

import chess.ChessGame.TeamColor;
import chess.ChessPiece.PieceType;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    static final int boardSize = 8;
    private ChessPiece[][] pieces = new ChessPiece[boardSize][boardSize];

    public ChessBoard() {
    }

    private static ChessPiece[] pawnRow(TeamColor color) {
        var row = new ChessPiece[boardSize];
        Arrays.fill(row, new ChessPiece(color, PieceType.PAWN));
        return row;
    }

    private static ChessPiece[] backRow(TeamColor color) {
        var pieces = new PieceType[] {
            PieceType.ROOK,
            PieceType.KNIGHT,
            PieceType.BISHOP,
            PieceType.QUEEN,
            PieceType.KING,
            PieceType.BISHOP,
            PieceType.KNIGHT,
            PieceType.ROOK
        };
        return Arrays.stream(pieces).map((p) -> new ChessPiece(color, p)).toArray(ChessPiece[]::new);
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        this.pieces[position.row() - 1][position.col() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return this.pieces[position.row() - 1][position.col() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        this.pieces = new ChessPiece[boardSize][boardSize];
        this.pieces[0] = ChessBoard.backRow(TeamColor.WHITE);
        this.pieces[1] = ChessBoard.pawnRow(TeamColor.WHITE);
        this.pieces[boardSize - 2] = ChessBoard.pawnRow(TeamColor.BLACK);
        this.pieces[boardSize - 1] = ChessBoard.backRow(TeamColor.BLACK);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(pieces);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessBoard that)) {
            return false;
        }
        return Objects.deepEquals(pieces, that.pieces);
    }

    @Override
    public String toString() {
        return "ChessBoard{" +
            "pieces=" + Arrays.toString(pieces) +
            '}';
    }

    public static boolean contains(ChessPosition position) {
        return position.row() <= boardSize && position.col() <= boardSize
            && position.row() >= 1 && position.col() >= 1;
    }
}
