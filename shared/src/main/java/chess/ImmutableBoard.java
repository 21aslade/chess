package chess;

import chess.ChessPiece.PieceType;

import java.util.stream.Stream;

public interface ImmutableBoard {
    ChessPiece getPiece(ChessPosition position);

    boolean contains(ChessPosition position);

    default boolean isTargeted(ChessPosition pos, ChessGame.TeamColor team) {
        var moveHelper = new MoveHelper(this, pos, team);

        var king = moveHelper.kingMoves()
            .flatMap(m -> Stream.ofNullable(this.getPiece(m.endPosition())))
            .filter(p -> p.type() == PieceType.KING);

        var pawn = moveHelper.pawnMoves()
            .flatMap(m -> Stream.ofNullable(this.getPiece(m.endPosition())))
            .filter(p -> p.type() == PieceType.PAWN);

        var rook = moveHelper.rookMoves()
            .flatMap(m -> Stream.ofNullable(this.getPiece(m.endPosition())))
            .filter(p -> p.type() == PieceType.ROOK || p.type() == PieceType.QUEEN);

        var bishop = moveHelper.bishopMoves()
            .flatMap(m -> Stream.ofNullable(this.getPiece(m.endPosition())))
            .filter(p -> p.type() == PieceType.BISHOP || p.type() == PieceType.QUEEN);

        var knight = moveHelper.knightMoves()
            .flatMap(m -> Stream.ofNullable(this.getPiece(m.endPosition())))
            .filter(p -> p.type() == PieceType.KNIGHT);

        return Stream.of(king, pawn, rook, bishop, knight)
            .flatMap(i -> i)
            .findAny().isPresent();
    }
}
