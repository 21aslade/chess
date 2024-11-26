package websocket;

import chess.ChessGame.TeamColor;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.Service;
import service.ServiceException;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import model.GameData;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private final DataAccess data;
    private final Gson gson = new Gson();
    private final ConcurrentHashMap<Integer, ConnectionManager> managers = new ConcurrentHashMap<>();

    public WebSocketHandler(DataAccess data) {
        this.data = data;
    }

    @OnWebSocketMessage
    public void onCommand(Session session, String commandText) throws IOException {
        var command = deserializeCommand(commandText);
        var connection = new Connection(command.getAuthToken(), session);

        var connections = managers.get(command.getGameID());
        if (connections == null) {
            connections = new ConnectionManager();
            managers.put(command.getGameID(), connections);
        }

        try {
            var game = Service.getGame(command.getGameID(), command.getAuthToken(), data);
            var user = Service.getUser(command.getAuthToken(), data);
            switch (command.getCommandType()) {
                case CONNECT -> connect(connections, connection, game, user);
                case MAKE_MOVE -> {}
                case LEAVE -> {}
                case RESIGN -> {}
            }
        } catch (IOException | DataAccessException e) {
            var message = new ErrorMessage("Error: an unexpected error has occurred");
            connection.send(message);
        } catch (ServiceException e) {
            var messageText = switch (e.kind()) {
                case DoesNotExist -> "Error: game does not exist";
                case Unauthorized, LoginFail -> "Error: unauthorized";
                case NullInput -> "Error: bad request";
                default -> "Error: an unexpected error has occurred";
            };

            connection.send(new ErrorMessage(messageText));
        }
    }

    private UserGameCommand deserializeCommand(String messageText) {
        var command = gson.fromJson(messageText, UserGameCommand.class);
        return switch (command.getCommandType()) {
            case CONNECT, LEAVE, RESIGN -> command;
            case MAKE_MOVE -> gson.fromJson(messageText, MakeMoveCommand.class);
        };
    }

    private void connect(ConnectionManager connections, Connection connection, GameData game, UserData user)
        throws IOException {
        connection.send(new LoadGameMessage(game.game()));

        var team = getTeam(game, user.username());
        var message = team != null ? "player " + user.username() + " joined as " + team : user.username() +
            " is now observing the game";
        connections.add(connection);
        connections.broadcast(connection.id, new NotificationMessage(message));
    }

    private TeamColor getTeam(GameData game, String username) {
        if (username.equals(game.whiteUsername())) {
            return TeamColor.WHITE;
        } else if (username.equals(game.blackUsername())) {
            return TeamColor.BLACK;
        } else {
            return null;
        }
    }
}
