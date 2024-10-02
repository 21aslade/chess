package chess;

import chess.ChessPiece.PieceType;

import java.util.stream.Stream;

public interface ImmutableBoard {
    ChessPiece getPiece(ChessPosition position);

    boolean contains(ChessPosition position);

    default boolean isTargeted(ChessPosition pos) {
        var team = this.getPiece(pos).pieceColor();
        var moveHelper = new MoveHelper(this, pos, team);

        return Stream.of(PieceType.KING, PieceType.PAWN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)
            .anyMatch(moveHelper::targetedBy);
    }
}
