package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;

public interface DataAccess {
    void putUser(UserData user) throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    void clearUsers() throws DataAccessException;

    void putGame(GameData game) throws DataAccessException;

    int gameCount() throws DataAccessException;

    List<GameData> getGames() throws DataAccessException;

    void clearGames() throws DataAccessException;

    void putAuth(AuthData auth) throws DataAccessException;

    AuthData getAuth(String token) throws DataAccessException;

    void deleteAuth(String token) throws DataAccessException;

    void clearAuth() throws DataAccessException;
}
