package websocket.messages;

public final class NotificationMessage extends ServerMessage {
    final String message;
    final boolean error;

    public NotificationMessage(String message, boolean error) {
        super(error ? ServerMessageType.ERROR : ServerMessageType.NOTIFICATION);
        this.message = message;
        this.error = error;
    }
}
