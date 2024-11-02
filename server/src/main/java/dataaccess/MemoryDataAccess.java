package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private final Map<String, AuthData> auth = new HashMap<>();

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        var current = this.users.putIfAbsent(user.username(), user);
        if (current != null) {
            throw new DataAccessException("Duplicate username");
        }
    }

    @Override
    public UserData getUser(String username) {
        return this.users.get(username);
    }

    @Override
    public void clearUsers() {
        this.users.clear();
    }

    @Override
    public int createGame(String name, ChessGame game) {
        var id = this.games.size() + 1;
        var gameData = new GameData(id, null, null, name, game);
        this.games.put(id, gameData);

        return id;
    }

    @Override
    public void putGame(GameData game) {
        this.games.put(game.gameID(), game);
    }

    @Override
    public GameData getGame(int gameId) {
        return this.games.get(gameId);
    }

    @Override
    public List<GameData> getGames() {
        return new ArrayList<>(this.games.values());
    }

    @Override
    public void clearGames() {
        this.games.clear();
    }

    @Override
    public void insertAuth(AuthData auth) throws DataAccessException {
        var current = this.auth.putIfAbsent(auth.authToken(), auth);
        if (current != null) {
            throw new DataAccessException("Duplicate authToken");
        }
    }

    @Override
    public AuthData getAuth(String token) {
        return this.auth.get(token);
    }

    @Override
    public void deleteAuth(String token) {
        this.auth.remove(token);
    }

    @Override
    public void clearAuth() {
        this.auth.clear();
    }
}
