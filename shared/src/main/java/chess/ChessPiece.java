package chess;

import chess.ChessGame.TeamColor;

import java.util.Collection;
import java.util.stream.Stream;

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
        return this.moveStream(board, myPosition).toList();
    }

    public Stream<ChessMove> moveStream(ChessBoard board, ChessPosition myPosition) {
        var helper = new MoveHelper(board, myPosition, this.pieceColor);
        return helper.moves(this.type);
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

