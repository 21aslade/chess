package service;

import chess.ChessGame;
import chess.ChessGame.TeamColor;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import service.ServiceException.ErrorKind;

import java.util.List;
import java.util.UUID;

public class Service {
    public static AuthData registerUser(UserData user, DataAccess data) throws DataAccessException, ServiceException {
        var current = data.getUser(user.username());
        if (current != null) {
            throw new ServiceException(ErrorKind.AlreadyExists);
        }

        data.putUser(user);

        return createSession(user.username(), data);
    }

    public static AuthData login(String username, String password, DataAccess data) throws
        DataAccessException,
        ServiceException {
        var dbUser = data.getUser(username);
        if (dbUser == null) {
            throw new ServiceException(ErrorKind.DoesNotExist);
        }

        if (!dbUser.password().equals(password)) {
            throw new ServiceException(ErrorKind.Unauthorized);
        }

        return createSession(username, data);
    }

    public static void logout(String authToken, DataAccess data) throws DataAccessException, ServiceException {
        if (data.getAuth(authToken) == null) {
            throw new ServiceException(ErrorKind.DoesNotExist);
        }
        data.deleteAuth(authToken);
    }

    public static GameData createGame(String gameName, String authToken, DataAccess data) throws
        DataAccessException,
        ServiceException {
        if (data.getAuth(authToken) == null) {
            throw new ServiceException(ErrorKind.Unauthorized);
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
            throw new ServiceException(ErrorKind.Unauthorized);
        }
        return data.getGames();
    }

    public static void joinGame(int gameId, TeamColor team, String authToken, DataAccess data) throws
        DataAccessException,
        ServiceException {
        var auth = data.getAuth(authToken);
        if (auth == null) {
            throw new ServiceException(ErrorKind.Unauthorized);
        }

        var game = data.getGame(gameId);
        if (game == null) {
            throw new ServiceException(ErrorKind.DoesNotExist);
        }

        if (team == null) {
            // Not joining either team, nothing to do :/
            return;
        }

        var username = game.user(team);
        if (username != null) {
            if (username.equals(auth.username())) {
                // Already joined, nothing to do
                return;
            } else {
                throw new ServiceException(ErrorKind.AlreadyExists);
            }
        }

        data.putGame(game.withUser(team, auth.username()));
    }

    private static AuthData createSession(String username, DataAccess data) throws DataAccessException {
        var uuid = UUID.randomUUID().toString();
        var authData = new AuthData(uuid, username);
        data.putAuth(authData);

        return authData;
    }
}
