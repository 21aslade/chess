package client;

import chess.ChessBoard;
import chess.ChessGame.TeamColor;
import model.AuthData;
import model.GameData;
import model.UserData;
import websocket.messages.LoadGameMessage;
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

    public ChessBoard board() {
        return this.game != null ? this.game.game().getBoard() : null;
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

    public void observeGame(int number) {
        if (games == null) {
            throw new ServerException("Error: list games first");
        }

        if (number < 1 || number > games.size()) {
            throw new ServerException("Error: invalid game index");
        }
    }

    public void joinGame(int number, TeamColor team) {
        if (games == null) {
            throw new ServerException("Error: list games first");
        }

        if (number < 1 || number > games.size()) {
            throw new ServerException("Error: invalid game index");
        }

        var game = games.get(number - 1);
        server.joinGame(session.authToken(), game.gameID(), team);

        this.game = game;
        this.team = game.userTeam(this.session.username());
        this.ws = new WsFacade(url, this::handleServerMessage);

        ws.connect(this.session.authToken(), game.gameID());
    }

    public void quit() {
        if (session != null) {
            logout();
        }
    }

    private void handleServerMessage(ServerMessage message) {
        if (message instanceof LoadGameMessage m) {
            this.game = this.game.withGame(m.game());
        }
        if (this.wsHandler != null) {
            this.wsHandler.handleMessage(message);
        }
    }
}
