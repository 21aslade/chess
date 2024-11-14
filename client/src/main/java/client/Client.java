package client;

import chess.ChessGame.TeamColor;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;

public class Client {
    private final ServerFacade server;
    private AuthData session;
    private GameData game;
    private List<GameData> games;

    public enum State {
        LOGGED_OUT,
        LOGGED_IN,
        PLAYING
    }

    public Client(ServerFacade server) {
        this.server = server;
    }

    public State state() {
        if (session == null) {
            return State.LOGGED_OUT;
        } else if (game == null) {
            return State.LOGGED_IN;
        } else {
            return State.PLAYING;
        }
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
    }

    public void quit() {
        if (session != null) {
            logout();
        }
    }
}
