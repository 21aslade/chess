package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;
import java.util.UUID;

public class Service {
    public static AuthData registerUser(UserData user, DataAccess data) throws DataAccessException, ServiceException {
        var current = data.getUser(user.username());
        if (current != null) {
            throw new ServiceException(ServiceException.ErrorKind.AlreadyExists);
        }

        data.putUser(user);

        return createSession(user.username(), data);
    }

    public static AuthData login(String username, String password, DataAccess data) throws
        DataAccessException,
        ServiceException {
        var dbUser = data.getUser(username);
        if (dbUser == null) {
            throw new ServiceException(ServiceException.ErrorKind.DoesNotExist);
        }

        if (!dbUser.password().equals(password)) {
            throw new ServiceException(ServiceException.ErrorKind.AuthenticationFailure);
        }

        return createSession(username, data);
    }

    public static void logout(String authToken, DataAccess data) throws DataAccessException, ServiceException {
        if (data.getAuth(authToken) == null) {
            throw new ServiceException(ServiceException.ErrorKind.DoesNotExist);
        }
        data.deleteAuth(authToken);
    }

    public static GameData createGame(String gameName, String authToken, DataAccess data) throws
        DataAccessException,
        ServiceException {
        if (data.getAuth(authToken) == null) {
            throw new ServiceException(ServiceException.ErrorKind.AuthenticationFailure);
        }

        var gameId = data.gameCount();
        var game = new GameData(gameId, null, null, gameName, new ChessGame());
        data.putGame(game);

        return game;
    }

    public static List<GameData> listGames(String authToken, DataAccess data) throws
        DataAccessException,
        ServiceException {
        if (data.getAuth(authToken) == null) {
            throw new ServiceException(ServiceException.ErrorKind.AuthenticationFailure);
        }
        return data.getGames();
    }

    private static AuthData createSession(String username, DataAccess data) throws DataAccessException {
        var uuid = UUID.randomUUID().toString();
        var authData = new AuthData(uuid, username);
        data.putAuth(authData);

        return authData;
    }
}
