package ui;

import chess.ChessBoard;
import chess.ChessGame.TeamColor;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.List;
import java.util.stream.IntStream;

import static ui.EscapeSequences.*;

public class PrintBoard {
    private static final String HEADER_BG_COLOR = SET_BG_COLOR_DARK_GREY;
    private static final String HEADER_FG_COLOR = SET_TEXT_COLOR_WHITE;
    private static final String WHITE_BG_COLOR = SET_BG_COLOR_WHITE;
    private static final String BLACK_BG_COLOR = SET_BG_COLOR_BLACK;

    private static final String WHITE_FG_COLOR = SET_TEXT_COLOR_RED;
    private static final String BLACK_FG_COLOR = SET_TEXT_COLOR_BLUE;

    private static final List<String> COLUMN_HEADERS = List.of(" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h ");
    private static final List<String> ROW_HEADERS = List.of(" 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " 8 ");

    public static String printBoard(ChessBoard board, TeamColor team) {
        var result = new StringBuilder();
        var columnHeaders = columnHeaders(team);
        result.append(columnHeaders);

        var rowIndexes = IntStream.range(1, ChessBoard.BOARD_SIZE + 1);
        var rows = team == TeamColor.WHITE ? rowIndexes.map(r -> ChessBoard.BOARD_SIZE - r + 1) : rowIndexes;
        rows.forEach((r) -> result.append(row(board, r, team)));

        result.append(columnHeaders);
        return result.toString();
    }

    private static String columnHeaders(TeamColor team) {
        var headers = team == TeamColor.WHITE ? COLUMN_HEADERS : COLUMN_HEADERS.reversed();

        var result = new StringBuilder().append(HEADER_BG_COLOR).append(HEADER_FG_COLOR).append(EMPTY);
        headers.forEach(result::append);
        result.append(EMPTY).append(RESET_BG_COLOR).append(RESET_TEXT_COLOR).append("\n");
        return result.toString();
    }

    private static String row(ChessBoard board, int row, TeamColor team) {
        var header = HEADER_BG_COLOR + HEADER_FG_COLOR + ROW_HEADERS.get(row - 1) + RESET_BG_COLOR + RESET_TEXT_COLOR;
        var result = new StringBuilder().append(header);

        var colIndexes = IntStream.range(1, ChessBoard.BOARD_SIZE + 1);
        var cols = team == TeamColor.WHITE ? colIndexes : colIndexes.map(c -> ChessBoard.BOARD_SIZE - c + 1);
        cols.forEach((col) -> {
            var bg = (row + col) % 2 == 1 ? WHITE_BG_COLOR : BLACK_BG_COLOR;
            result.append(bg);
            var piece = board.getPiece(new ChessPosition(row, col));
            result.append(piece(piece));
        });

        result.append(header).append("\n");
        return result.toString();
    }

    private static String piece(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }

        var white = piece.pieceColor() == TeamColor.WHITE;
        var color = white ? WHITE_FG_COLOR : BLACK_FG_COLOR;
        var text = switch (piece.type()) {
            case KING -> white ? WHITE_KING : BLACK_KING;
            case QUEEN -> white ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP -> white ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> white ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK -> white ? WHITE_ROOK : BLACK_ROOK;
            case PAWN -> white ? WHITE_PAWN : BLACK_PAWN;
            case null -> EMPTY;
        };

        return color + text;
    }
}
