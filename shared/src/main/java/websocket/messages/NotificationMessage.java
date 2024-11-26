package websocket.messages;

public final class NotificationMessage extends ServerMessage {
    final String message;

    public NotificationMessage(String message) {
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
    }
}
