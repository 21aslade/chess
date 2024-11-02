package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;

public interface DataAccess {
    void insertUser(UserData user) throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    void clearUsers() throws DataAccessException;

    int createGame(String name, ChessGame game) throws DataAccessException;

    void putGame(GameData game) throws DataAccessException;

    GameData getGame(int gameId) throws DataAccessException;

    List<GameData> getGames() throws DataAccessException;

    void clearGames() throws DataAccessException;

    void insertAuth(AuthData auth) throws DataAccessException;

    AuthData getAuth(String token) throws DataAccessException;

    void deleteAuth(String token) throws DataAccessException;

    void clearAuth() throws DataAccessException;
}
