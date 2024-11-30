package chess;

import chess.ChessPiece.PieceType;

import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public record ChessMove(ChessPosition startPosition, ChessPosition endPosition, PieceType promotionPiece) {
    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() { return this.startPosition; }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return this.endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public PieceType getPromotionPiece() { return this.promotionPiece; }

    @Override
    public String toString() {
        return this.startPosition.toString() + " to " + this.endPosition.toString();
    }
}
