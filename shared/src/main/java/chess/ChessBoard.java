package chess;

import chess.ChessGame.TeamColor;
import chess.ChessPiece.PieceType;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard implements ImmutableBoard {
    static final int BOARD_SIZE = 8;
    private ChessPiece[][] pieces = new ChessPiece[BOARD_SIZE][BOARD_SIZE];

    public ChessBoard() {}

    private static ChessPiece[] pawnRow(TeamColor color) {
        var row = new ChessPiece[BOARD_SIZE];
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
    @Override
    public ChessPiece getPiece(ChessPosition position) {
        return this.pieces[position.row() - 1][position.col() - 1];
    }

    /**
     * Make the given move, only checking that the start position has a piece and the end position is on the board.
     *
     * @param move the move to make
     * @throws InvalidMoveException If the start or end position of the move is off the board, or if there is no piece at the start position
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        var start = move.startPosition();
        var end = move.endPosition();

        if (!this.contains(start) || !this.contains(end)) {
            throw new InvalidMoveException();
        }

        var moved = this.getPiece(start);
        if (moved == null) {
            throw new InvalidMoveException();
        }

        this.pieces[start.row() - 1][start.col() - 1] = null;

        if (move.promotionPiece() == null) {
            this.pieces[end.row() - 1][end.col() - 1] = moved;
        } else {
            this.pieces[end.row() - 1][end.col() - 1] = new ChessPiece(moved.pieceColor(), move.promotionPiece());
        }
    }

    public Stream<ChessMove> movesFrom(ChessPosition pos) {
        return this.getPiece(pos).moveStream(this, pos);
    }

    public Stream<ChessPosition> piecePositions(Predicate<ChessPiece> pred) {
        return IntStream.range(1, ChessBoard.BOARD_SIZE + 1)
            .mapToObj(a -> IntStream.range(1, ChessBoard.BOARD_SIZE + 1).mapToObj(b -> new ChessPosition(a, b)))
            .flatMap(i -> i)
            .filter(p -> {
                var piece = this.getPiece(p);
                return piece != null && pred.test(piece);
            });
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        this.pieces = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
        this.pieces[0] = ChessBoard.backRow(TeamColor.WHITE);
        this.pieces[1] = ChessBoard.pawnRow(TeamColor.WHITE);
        this.pieces[BOARD_SIZE - 2] = ChessBoard.pawnRow(TeamColor.BLACK);
        this.pieces[BOARD_SIZE - 1] = ChessBoard.backRow(TeamColor.BLACK);
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

    @Override
    public boolean contains(ChessPosition position) {
        return position.row() <= BOARD_SIZE && position.col() <= BOARD_SIZE
            && position.row() >= 1 && position.col() >= 1;
    }
}
