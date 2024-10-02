package chess;

import chess.ChessGame.TeamColor;
import chess.Util.IntPair;
import chess.ChessPiece.PieceType;

import java.util.stream.Stream;

/**
 * A helper for determining which moves are valid for a given piece
 *
 * @param board The board
 * @param start The location of the piece to check
 * @param color The color of the piece to check
 */
record MoveHelper(ImmutableBoard board, ChessPosition start, TeamColor color) {
    enum MoveStatus {
        BLANK,
        CAPTURE,
        BLOCKED
    }

    public Stream<ChessMove> moves(PieceType type) {
        return switch (type) {
            case KING -> this.kingMoves();
            case QUEEN -> this.queenMoves();
            case BISHOP -> this.bishopMoves();
            case KNIGHT -> this.knightMoves();
            case ROOK -> this.rookMoves();
            case PAWN -> this.pawnMoves();
        };
    }

    /**
     * Returns true if this.start is targeted by a piece of the given type.
     * For convenience, if the desired type is ROOK or BISHOP, QUEENs will be included as well.
     *
     * @param type the type of piece to check for
     * @return true if any piece of that type is targeting this.start
     */
    public boolean targetedBy(PieceType type) {
        var includeQueen = type == PieceType.ROOK || type == PieceType.BISHOP;
        // This works because all moves in chess are symmetric
        // if piece A threatens piece B, and piece A and piece B are the same type, then piece B threatens piece A
        return this.moves(type)
            .anyMatch(m -> {
                var piece = this.board.getPiece(m.endPosition());
                return piece != null && (piece.type() == type || (includeQueen && piece.type() == PieceType.QUEEN));
            });
    }

    public Stream<ChessMove> rookMoves() {
        return Stream.of(IntPair.Up, IntPair.Down, IntPair.Left, IntPair.Right)
            .flatMap(this::rayCast)
            .map(p -> new ChessMove(start, p, null));
    }

    public Stream<ChessMove> bishopMoves() {
        return Stream.of(IntPair.UpLeft, IntPair.UpRight, IntPair.DownLeft, IntPair.DownRight)
            .flatMap(this::rayCast)
            .map(p -> new ChessMove(start, p, null));
    }

    public Stream<ChessMove> queenMoves() {
        return Stream.concat(rookMoves(), bishopMoves());
    }

    public Stream<ChessMove> kingMoves() {
        Stream.Builder<ChessMove> moves = Stream.builder();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                var target = new ChessPosition(start.row() + i, start.col() + j);
                if (checkTarget(target) != MoveStatus.BLOCKED) {
                    moves.add(new ChessMove(start, target, null));
                }
            }
        }

        return moves.build();
    }

    public Stream<ChessMove> knightMoves() {
        var positions = Stream.of(new IntPair(2, 1), new IntPair(1, 2))
            .flatMap(p -> Stream.of(p, new IntPair(-p.a(), p.b()))) // vertical symmetry
            .flatMap(p -> Stream.of(p, new IntPair(p.a(), -p.b()))) // horizontal symmetry
            .map(start::add);

        return positions.filter(p -> checkTarget(p) != MoveStatus.BLOCKED)
            .map(p -> new ChessMove(start, p, null));
    }

    public Stream<ChessMove> pawnMoves() {
        var promotionRow = color == TeamColor.WHITE ? ChessBoard.boardSize : 1;
        var startingRow = color == TeamColor.WHITE ? 2 : ChessBoard.boardSize - 1;
        var forward = color == TeamColor.WHITE ? IntPair.Up : IntPair.Down;

        var singleMove = start.add(forward);
        var doubleMove = start.row() == startingRow ? singleMove.add(forward) : null;
        var move = Stream.of(singleMove, doubleMove)
            .takeWhile(p -> p != null && checkTarget(p) == MoveStatus.BLANK);

        var attack = Stream.of(IntPair.Left, IntPair.Right)
            .map(side -> start.add(forward).add(side))
            .filter(p -> checkTarget(p) == MoveStatus.CAPTURE);

        return Stream.concat(move, attack)
            .flatMap(p -> moveOrPromote(p, promotionRow));
    }

    /**
     * Returns the status of the given target:
     * <ul>
     *     <li>{@link MoveStatus#BLOCKED} if the target is off the board or occupied by the same color</li>
     *     <li>{@link MoveStatus#CAPTURE} if the target is occupied by the opposite color</li>
     *     <li>{@link MoveStatus#BLANK} otherwise</li>
     * </ul>
     */
    private MoveStatus checkTarget(ChessPosition target) {
        if (!board.contains(target)) {
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

    /**
     * Turn the given target into a move, or every possible promotion if the target row equals promotionRow
     */
    private Stream<ChessMove> moveOrPromote(ChessPosition target, int promotionRow) {
        if (target.row() == promotionRow) {
            return Stream.of(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)
                .map(t -> new ChessMove(start, target, t));
        } else {
            return Stream.of(new ChessMove(start, target, null));
        }
    }

    /**
     * Returns all the positions that can be reached from start in the direction specified by offset.
     * Stops after capturing an enemy piece or being blocked by the edge of a board or a friendly piece.
     */
    private Stream<ChessPosition> rayCast(IntPair offset) {
        Stream.Builder<ChessPosition> targets = Stream.builder();
        for (int i = 1; true; i++) {
            var target = start.add(offset.mult(i));
            var status = checkTarget(target);
            switch (status) {
                case BLOCKED:
                    return targets.build();
                case CAPTURE:
                    return targets.add(target).build();
                case BLANK:
                    targets.add(target);
                    break;
            }
        }
    }
}
