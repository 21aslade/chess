package chess;

import chess.ChessPiece.PieceType;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor turn = TeamColor.WHITE;
    private ChessPosition whiteKingPosition;
    private ChessPosition blackKingPosition;

    public ChessGame() {
        var board = new ChessBoard();
        board.resetBoard();
        this.setBoard(board);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.turn = team;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        var kingPosition = teamColor == TeamColor.WHITE ? this.whiteKingPosition : this.blackKingPosition;
        if (kingPosition == null) {
            return false;
        }

        return this.board.isTargeted(kingPosition, teamColor);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        this.whiteKingPosition =
            board.piecePositions(p -> p.type() == PieceType.KING && p.pieceColor() == TeamColor.WHITE)
                .findAny().orElse(null);
        this.blackKingPosition =
            board.piecePositions(p -> p.type() == PieceType.KING && p.pieceColor() == TeamColor.BLACK)
                .findAny().orElse(null);
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK;

        public TeamColor opposite() {
            return switch (this) {
                case WHITE -> TeamColor.BLACK;
                case BLACK -> TeamColor.WHITE;
            };
        }
    }
}
