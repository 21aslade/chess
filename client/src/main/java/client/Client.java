package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessGame.TeamColor;
import chess.ChessMove;
import chess.InvalidMoveException;
import model.AuthData;
import model.GameData;
import model.UserData;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.List;

public class Client {
    private final String url;
    private final ServerFacade server;
    private WsFacade.Handler wsHandler;
    private WsFacade ws;
    private AuthData session;
    private GameData game;
    private TeamColor team;
    private List<GameData> games;

    public enum State {
        LOGGED_OUT,
        LOGGED_IN,
        PLAYING
    }

    public Client(String url) {
        this.url = url;
        this.server = new HttpFacade(url);
    }

    public void setWsHandler(WsFacade.Handler handler) {
        this.wsHandler = handler;
    }

    public State state() {
        if (session == null) {
            return State.LOGGED_OUT;
        } else if (ws == null) {
            return State.LOGGED_IN;
        } else {
            return State.PLAYING;
        }
    }

    public ChessGame game() {
        return this.game != null ? this.game.game() : null;
    }

    public TeamColor team() {
        return this.team;
    }

    public boolean canMove() {
        return game.game().getTeamTurn() == this.team
            && game.game().status().canPlay();
    }

    public void register(UserData user) {
        session = server.register(user);
    }

    public void login(String username, String password) {
        session = server.login(username, password);
    }

    public void logout() {
        server.logout(session.authToken());
        session = null;
    }

    public void createGame(String name) {
        server.createGame(session.authToken(), name);
    }

    public List<GameData> listGames() {
        games = server.listGames(session.authToken());
        return games;
    }

    private GameData getGame(int number) {
        if (games == null) {
            throw new ServerException("Error: list games first");
        }

        if (number < 1 || number > games.size()) {
            throw new ServerException("Error: invalid game index");
        }

        return games.get(number - 1);
    }

    private TeamColor connectToGame(GameData game) {
        var team = game.userTeam(session.username());
        this.team = team;
        this.ws = new WsFacade(url, this::handleServerMessage);

        ws.connect(session.authToken(), game.gameID());
        return team;
    }

    public TeamColor observeGame(int number) {
        var game = getGame(number);
        this.game = game;
        return connectToGame(game);
    }

    public void joinGame(int number, TeamColor team) {
        var game = getGame(number);
        this.game = game;
        server.joinGame(session.authToken(), game.gameID(), team);
        connectToGame(game.withUser(team, session.username()));
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        this.game.game().makeMove(move);
        this.ws.move(session.authToken(), game.gameID(), move);
    }

    public void leave() {
        this.ws.leave(session.authToken(), game.gameID());

        this.game = null;
        this.team = null;
        this.ws = null;
    }

    public void resign() {
        this.ws.resign(session.authToken(), game.gameID());
    }

    public void quit() {
        if (ws != null) {
            leave();
        }
        if (session != null) {
            logout();
        }
    }

    private void handleServerMessage(ServerMessage message) {
        switch (message) {
            case LoadGameMessage m -> this.game = this.game.withGame(m.game());
            case NotificationMessage m -> {
                var resign = m.resign();
                if (resign != null) {
                    this.game.game().resign(resign);
                }
            }
            default -> {}
        }
        if (this.wsHandler != null) {
            this.wsHandler.handleMessage(message);
        }
    }
}
