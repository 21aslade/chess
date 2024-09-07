package chess;

import chess.ChessGame.TeamColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import chess.Util.IntPair;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public record ChessPiece(TeamColor pieceColor, PieceType type) {
    /**
     * @return Which team this chess piece belongs to
     */
    public TeamColor getTeamColor() {
        return this.pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        var helper = new MoveHelper(board, myPosition, this.pieceColor);
        return switch (this.type) {
            case KING -> helper.kingMoves();
            case QUEEN -> null;
            case BISHOP -> null;
            case KNIGHT -> helper.knightMoves();
            case ROOK -> null;
            case PAWN -> null;
        };
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }
}

record MoveHelper(ChessBoard board, ChessPosition start, TeamColor color) {
    enum MoveStatus {
        BLANK,
        CAPTURE,
        BLOCKED
    }

    private MoveStatus checkTarget(ChessPosition target) {
        if (!ChessBoard.contains(target)) {
            return MoveStatus.BLOCKED;
        }

        var targetPiece = board.getPiece(target);
        if (targetPiece == null) {
            return MoveStatus.BLANK;
        } else if (targetPiece.pieceColor() == color) {
            return MoveStatus.BLOCKED;
        } else {
            return MoveStatus.CAPTURE;
        }
    }

    public List<ChessMove> kingMoves() {
        var moves = new ArrayList<ChessMove>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                var target = new ChessPosition(start.row() + i, start.col() + j);
                if (checkTarget(target) != MoveStatus.BLOCKED) {
                    moves.add(new ChessMove(start, target, null));
                }
            }
        }

        return moves;
    }

    public List<ChessMove> knightMoves() {
        var positions = Stream.of(new IntPair(2, 1), new IntPair(1, 2))
            .flatMap(p -> Stream.of(p, new IntPair(-p.a(), p.b()))) // vertical symmetry
            .flatMap(p -> Stream.of(p, new IntPair(p.a(), -p.b()))) // horizontal symmetry
            .map(start::add);

        return positions.filter(p -> checkTarget(p) != MoveStatus.BLOCKED)
            .map(p -> new ChessMove(start, p, null))
            .toList();
    }
}
