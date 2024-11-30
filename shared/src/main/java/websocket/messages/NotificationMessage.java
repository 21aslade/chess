package websocket.messages;

import chess.ChessGame.TeamColor;

public final class NotificationMessage extends ServerMessage {
    final String message;
    final TeamColor resign;

    public NotificationMessage(String message) {
        this(message, null);
    }

    public NotificationMessage(String message, TeamColor resign) {
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
        this.resign = resign;
    }

    public String message() {
        return this.message;
    }

    public TeamColor resign() {
        return this.resign;
    }
}
