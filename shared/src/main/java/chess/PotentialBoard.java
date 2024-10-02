package chess;

public record PotentialBoard(ImmutableBoard board, ChessMove move, ChessPiece replacement) implements ImmutableBoard {
    @Override
    public ChessPiece getPiece(ChessPosition position) {
        if (position.equals(move.startPosition())) {
            return replacement;
        } else if (position.equals(move.endPosition())) {
            return board.getPiece(move.startPosition());
        } else {
            return board.getPiece(position);
        }
    }

    @Override
    public boolean contains(ChessPosition position) {
        return board.contains(position);
    }
}
