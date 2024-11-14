package service;

import chess.ChessGame;
import chess.ChessGame.TeamColor;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import service.ServiceException.ErrorKind;

import java.util.List;
import java.util.UUID;

public class Service {
    public static AuthData registerUser(UserData user, DataAccess data) throws DataAccessException, ServiceException {
        verifyNonNull(user);
        if (!user.initialized()) {
            throw new ServiceException(ErrorKind.NullInput);
        }

        var current = data.getUser(user.username());
        if (current != null) {
            throw new ServiceException(ErrorKind.AlreadyExists);
        }

        var hashed = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        var dbUser = new UserData(user.username(), hashed, user.email());

        data.insertUser(dbUser);

        return createSession(dbUser.username(), data);
    }

    public static AuthData login(String username, String password, DataAccess data) throws
        DataAccessException,
        ServiceException {
        verifyNonNull(username, password);
        var dbUser = data.getUser(username);

        if (dbUser == null || !BCrypt.checkpw(password, dbUser.password())) {
            throw new ServiceException(ErrorKind.LoginFail);
        }

        return createSession(username, data);
    }

    public static void logout(String authToken, DataAccess data) throws DataAccessException, ServiceException {
        verifyNonNull(authToken);
        Service.verifyAuth(authToken, data);
        data.deleteAuth(authToken);
    }

    public static int createGame(String gameName, String authToken, DataAccess data) throws
        DataAccessException,
        ServiceException {
        verifyNonNull(gameName, authToken);
        Service.verifyAuth(authToken, data);

        return data.createGame(gameName, new ChessGame());
    }

    public static List<GameData> listGames(String authToken, DataAccess data) throws
        DataAccessException,
        ServiceException {
        verifyNonNull(authToken);
        Service.verifyAuth(authToken, data);
        return data.getGames();
    }

    public static void joinGame(int gameId, TeamColor team, String authToken, DataAccess data) throws
        DataAccessException,
        ServiceException {
        verifyNonNull(team, authToken, data);
        var auth = Service.verifyAuth(authToken, data);

        var game = data.getGame(gameId);
        if (game == null) {
            throw new ServiceException(ErrorKind.DoesNotExist);
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

    public static void clear(DataAccess data) throws DataAccessException {
        data.clearUsers();
        data.clearAuth();
        data.clearGames();
    }

    private static void verifyNonNull(Object... objects) throws ServiceException {
        for (var o : objects) {
            if (o == null) {
                throw new ServiceException(ErrorKind.NullInput);
            }
        }
    }

    private static AuthData verifyAuth(String authToken, DataAccess data) throws ServiceException, DataAccessException {
        var auth = data.getAuth(authToken);
        if (auth == null) {
            throw new ServiceException(ErrorKind.Unauthorized);
        }

        return auth;
    }

    private static AuthData createSession(String username, DataAccess data) throws DataAccessException {
        var uuid = UUID.randomUUID().toString();
        var authData = new AuthData(uuid, username);
        data.insertAuth(authData);

        return authData;
    }
}
