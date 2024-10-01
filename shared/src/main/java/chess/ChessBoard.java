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

    public ChessBoard() {}

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
     * Make the given move, only checking that the start position has a piece and the end position is on the board.
     *
     * @param move the move to make
     * @return The piece that was captured by the moving piece, if any.
     * @throws InvalidMoveException If the start or end position of the move is off the board, or if there is no piece at the start position
     */
    public ChessPiece movePiece(ChessMove move) throws InvalidMoveException {
        var start = move.startPosition();
        var end = move.endPosition();

        if (!ChessBoard.contains(start) || !ChessBoard.contains(end)) {
            throw new InvalidMoveException();
        }

        var moved = this.getPiece(start);
        if (moved == null) {
            throw new InvalidMoveException();
        }

        this.pieces[start.row() - 1][start.col() - 1] = null;

        var captured = this.getPiece(end);
        if (move.promotionPiece() == null) {
            this.pieces[end.row() - 1][end.col() - 1] = moved;
        } else {
            this.pieces[end.row() - 1][end.col() - 1] = new ChessPiece(moved.pieceColor(), moved.type());
        }

        return captured;
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
