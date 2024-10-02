package chess;

import java.util.stream.Stream;

public interface ImmutableBoard {
    ChessPiece getPiece(ChessPosition position);

    boolean contains(ChessPosition position);

    default boolean isTargeted(ChessPosition pos, ChessGame.TeamColor team) {
        var moveHelper = new MoveHelper(this, pos, team);
        var pawn = moveHelper.pawnMoves()
            .flatMap(m -> Stream.ofNullable(this.getPiece(m.endPosition())))
            .filter(p -> p.type() == ChessPiece.PieceType.PAWN);

        var rook = moveHelper.rookMoves()
            .flatMap(m -> Stream.ofNullable(this.getPiece(m.endPosition())))
            .filter(p -> p.type() == ChessPiece.PieceType.ROOK || p.type() == ChessPiece.PieceType.QUEEN);

        var bishop = moveHelper.bishopMoves()
            .flatMap(m -> Stream.ofNullable(this.getPiece(m.endPosition())))
            .filter(p -> p.type() == ChessPiece.PieceType.BISHOP || p.type() == ChessPiece.PieceType.QUEEN);

        var knight = moveHelper.knightMoves()
            .flatMap(m -> Stream.ofNullable(this.getPiece(m.endPosition())))
            .filter(p -> p.type() == ChessPiece.PieceType.KNIGHT);

        return Stream.of(pawn, rook, bishop, knight)
            .flatMap(i -> i)
            .findAny().isPresent();
    }
}
