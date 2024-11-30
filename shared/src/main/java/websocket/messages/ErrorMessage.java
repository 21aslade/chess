package websocket.messages;

public final class ErrorMessage extends ServerMessage {
    final String errorMessage;

    public ErrorMessage(String message) {
        super(ServerMessageType.ERROR);
        this.errorMessage = message;
    }

    public String message() {
        return this.errorMessage;
    }
}
