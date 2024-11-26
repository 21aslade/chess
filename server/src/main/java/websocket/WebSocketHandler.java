package websocket;

import chess.InvalidMoveException;
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

        var auth = command.getAuthToken();

        try {
            var game = Service.getGame(command.getGameID(), auth, data);
            var user = Service.getUser(auth, data);
            switch (command.getCommandType()) {
                case CONNECT -> connect(connections, connection, game, user);
                case MAKE_MOVE -> move(connections, (MakeMoveCommand) command, data, user);
                case LEAVE -> leave(connections, auth, data, game.gameID(), user);
                case RESIGN -> resign(connections, auth, data, game.gameID(), user);
            }
        } catch (IOException | DataAccessException e) {
            var message = new ErrorMessage("Error: an unexpected error has occurred");
            connection.send(message);
        } catch (ServiceException e) {
            var messageText = switch (e.kind()) {
                case AlreadyExists -> "Error: cannot resign twice";
                case DoesNotExist -> "Error: game does not exist";
                case Unauthorized, LoginFail -> "Error: unauthorized";
                case NullInput -> "Error: bad request";
            };

            connection.send(new ErrorMessage(messageText));
        } catch (InvalidMoveException e) {
            connection.send(new ErrorMessage("Error: invalid move"));
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

        var team = game.userTeam(user.username());
        var message = team != null ? "player " + user.username() + " joined as " + team : user.username() +
            " is now observing the game";
        connections.add(connection);
        connections.broadcast(connection.id, new NotificationMessage(message));
    }

    private void move(ConnectionManager connections, MakeMoveCommand move, DataAccess data, UserData user)
        throws ServiceException, DataAccessException, InvalidMoveException, IOException {
        var game = Service.makeMove(move.getGameID(), move.getAuthToken(), move.move(), data);
        connections.broadcast(null, new LoadGameMessage(game));

        var moveMessage = user.username() + " made move " + move;
        connections.broadcast(move.getAuthToken(), new NotificationMessage(moveMessage));

        var message = switch (game.status()) {
            case CHECK -> game.getTeamTurn() + " is in check!";
            case CHECKMATE -> game.getTeamTurn().opposite() + " wins!";
            case STALEMATE -> game.getTeamTurn() + " can't move. Stalemate!";
            default -> null;
        };

        if (message != null) {
            connections.broadcast(null, new NotificationMessage(message));
        }
    }

    private void leave(ConnectionManager connections, String authToken, DataAccess data, int game, UserData user)
        throws ServiceException, DataAccessException, IOException {
        Service.leaveGame(game, authToken, data);
        var message = user.username() + " left the game";
        connections.remove(authToken);
        connections.broadcast(authToken, new NotificationMessage(message));
    }

    private void resign(ConnectionManager connections, String authToken, DataAccess data, int game, UserData user)
        throws ServiceException, DataAccessException, IOException {
        Service.resignGame(game, authToken, data);
        var message = user.username() + " has resigned.";
        connections.broadcast(null, new NotificationMessage(message));
    }
}
