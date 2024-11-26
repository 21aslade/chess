package websocket;

import chess.ChessGame.TeamColor;
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

        try {
            var game = Service.getGame(command.getGameID(), command.getAuthToken(), data);
            var user = Service.getUser(command.getAuthToken(), data);
            switch (command.getCommandType()) {
                case CONNECT -> connect(connections, connection, game, user);
                case MAKE_MOVE -> move(connections, (MakeMoveCommand) command, data, user);
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

        var team = getTeam(game, user.username());
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
