package websocket.messages;

import chess.ChessGame;

public final class LoadGameMessage extends ServerMessage {
    private final ChessGame game;

    public LoadGameMessage(ChessGame game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }

    public ChessGame game() {
        return this.game;
    }
}
