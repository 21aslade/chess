package client;

import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.commands.UserGameCommand.CommandType;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import javax.websocket.MessageHandler.Whole;
import java.io.IOException;
import java.net.URI;

public class WsFacade extends Endpoint {
    Session session;
    Gson gson = new Gson();

    public interface Handler {
        void handleMessage(ServerMessage message);
    }

    public WsFacade(String url, Handler handler) {
        var wsUrl = url.replace("http", "ws");
        var uri = URI.create(wsUrl + "/ws");

        var container = ContainerProvider.getWebSocketContainer();

        try {
            this.session = container.connectToServer(this, uri);
            this.session.addMessageHandler(
                String.class, messageText -> {
                    var message = deserializeMessage(messageText);
                    handler.handleMessage(message);
                }
            );
        } catch (DeploymentException | IOException e) {
            throw new ServerException(e.getMessage());
        }
    }

    public ServerMessage deserializeMessage(String text) {
        var message = gson.fromJson(text, ServerMessage.class);
        return switch (message.getServerMessageType()) {
            case LOAD_GAME -> gson.fromJson(text, LoadGameMessage.class);
            case ERROR -> gson.fromJson(text, ErrorMessage.class);
            case NOTIFICATION -> gson.fromJson(text, NotificationMessage.class);
        };
    }

    public void connect(String auth, int gameId) throws ServerException {
        this.sendCommand(new UserGameCommand(CommandType.CONNECT, auth, gameId));
    }

    public void move(String auth, int gameId, ChessMove move) throws ServerException {
        this.sendCommand(new MakeMoveCommand(auth, gameId, move));
    }

    private void sendCommand(UserGameCommand command) throws ServerException {
        try {
            this.session.getBasicRemote().sendText(gson.toJson(command));
        } catch (IOException e) {
            throw new ServerException(e.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {}
}
