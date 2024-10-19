package service;

import chess.ChessGame;
import chess.ChessGame.TeamColor;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.ServiceException.ErrorKind;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    MemoryDataAccess dataAccess = new MemoryDataAccess();

    @BeforeEach
    public void clearDatabase() {
        dataAccess.clearAuth();
        dataAccess.clearGames();
        dataAccess.clearUsers();
    }

    @Test
    public void registerUserSuccess() throws DataAccessException, ServiceException {
        var user = new UserData("strength", "weakness", "journey@destination.com");
        var authData = Service.registerUser(user, dataAccess);
        assertEquals(user.username(), authData.username());
    }

    @Test
    public void registerUserExisting() throws DataAccessException, ServiceException {
        var user1 = new UserData("strength", "weakness", "journey@destination.com");
        Service.registerUser(user1, dataAccess);

        var user2 = new UserData("strength", "in", "numbers@hotmail.com");
        var error = assertThrows(ServiceException.class, () -> Service.registerUser(user2, dataAccess));
        assertEquals(ErrorKind.AlreadyExists, error.kind());
    }

    @Test
    public void loginSuccess() throws DataAccessException, ServiceException {
        var user = new UserData("strength", "weakness", "journey@destination.com");
        var auth1 = Service.registerUser(user, dataAccess);

        var auth2 = Service.login(user.username(), user.password(), dataAccess);
        assertEquals(user.username(), auth2.username());
        assertNotEquals(auth1.authToken(), auth2.authToken());
    }

    @Test
    public void loginNonexistent() {
        var user = new UserData("hippie", "dippy", "baloney@badcop.com");
        var error =
            assertThrows(ServiceException.class, () -> Service.login(user.username(), user.password(), dataAccess));
        assertEquals(ErrorKind.DoesNotExist, error.kind());
    }

    @Test
    public void loginWrongPassword() throws ServiceException, DataAccessException {
        var user = new UserData("favorite_color", "yellow", "bridge@death.com");
        Service.registerUser(user, dataAccess);

        assertThrows(ServiceException.class, () -> Service.login(user.username(), "blue", dataAccess));
    }

    @Test
    public void logoutSuccess() throws ServiceException, DataAccessException {
        var user = new UserData("strength", "weakness", "journey@destination.com");
        var auth = Service.registerUser(user, dataAccess);

        Service.logout(auth.authToken(), dataAccess);
    }

    @Test
    public void logoutNonexistent() {
        var error = assertThrows(ServiceException.class, () -> Service.logout("nope way", dataAccess));
        assertEquals(ErrorKind.DoesNotExist, error.kind());
    }

    @Test
    public void createGameSuccess() throws ServiceException, DataAccessException {
        var user = new UserData("strength", "weakness", "journey@destination.com");
        var auth = Service.registerUser(user, dataAccess);
        var gameName = "game";
        var game1 = Service.createGame(gameName, auth.authToken(), dataAccess);
        var game2 = Service.createGame(gameName, auth.authToken(), dataAccess);

        var expected1 = new GameData(game1.gameId(), null, null, gameName, new ChessGame());
        assertEquals(expected1, game1);
        assertNotEquals(game1.gameId(), game2.gameId());
    }

    @Test
    public void createGameUnauthorized() {
        var error = assertThrows(ServiceException.class, () -> Service.createGame("game", "no chance", dataAccess));
        assertEquals(ErrorKind.Unauthorized, error.kind());
    }

    @Test
    public void listGamesSuccess() throws ServiceException, DataAccessException {
        var user = new UserData("strength", "weakness", "journey@destination.com");
        var auth = Service.registerUser(user, dataAccess);
        var gameName = "game";
        var game1 = Service.createGame(gameName, auth.authToken(), dataAccess);
        var game2 = Service.createGame(gameName, auth.authToken(), dataAccess);

        var actual = Service.listGames(auth.authToken(), dataAccess);
        assertEquals(List.of(game1, game2), actual);
    }

    @Test
    public void listGamesUnauthorized() {
        var error = assertThrows(ServiceException.class, () -> Service.listGames("nah", dataAccess));
        assertEquals(ErrorKind.Unauthorized, error.kind());
    }

    @Test
    public void joinGameSuccess() throws ServiceException, DataAccessException {
        var user1 = new UserData("strength", "weakness", "journey@destination.com");
        var user2 = new UserData("favorite_color", "yellow", "bridge@death.com");
        var auth1 = Service.registerUser(user1, dataAccess);
        var auth2 = Service.registerUser(user2, dataAccess);
        var gameName = "game";
        var game = Service.createGame(gameName, auth1.authToken(), dataAccess);

        Service.joinGame(game.gameId(), TeamColor.WHITE, auth1.authToken(), dataAccess);
        // Test for idempotence
        Service.joinGame(game.gameId(), TeamColor.WHITE, auth1.authToken(), dataAccess);
        Service.joinGame(game.gameId(), TeamColor.BLACK, auth2.authToken(), dataAccess);

        var expected = new GameData(game.gameId(), user1.username(), user2.username(), gameName, new ChessGame());
        assertEquals(List.of(expected), Service.listGames(auth1.authToken(), dataAccess));
    }

    @Test
    public void joinGameUnauthorized() {
        var error = assertThrows(ServiceException.class, () -> Service.joinGame(0, TeamColor.WHITE, "heh", dataAccess));
        assertEquals(ErrorKind.Unauthorized, error.kind());
    }

    @Test
    public void joinGameNonexistent() throws ServiceException, DataAccessException {
        var user = new UserData("strength", "weakness", "journey@destination.com");
        var auth = Service.registerUser(user, dataAccess);

        var error = assertThrows(
            ServiceException.class,
            () -> Service.joinGame(0, TeamColor.WHITE, auth.authToken(), dataAccess)
        );

        assertEquals(ErrorKind.DoesNotExist, error.kind());
    }

    @Test
    public void joinGameAlreadyTaken() throws ServiceException, DataAccessException {
        var user1 = new UserData("strength", "weakness", "journey@destination.com");
        var user2 = new UserData("favorite_color", "yellow", "bridge@death.com");
        var auth1 = Service.registerUser(user1, dataAccess);
        var auth2 = Service.registerUser(user2, dataAccess);
        var gameName = "game";
        var game = Service.createGame(gameName, auth1.authToken(), dataAccess);

        Service.joinGame(game.gameId(), TeamColor.WHITE, auth1.authToken(), dataAccess);

        var error = assertThrows(
            ServiceException.class,
            () -> Service.joinGame(game.gameId(), TeamColor.WHITE, auth2.authToken(), dataAccess)
        );

        assertEquals(ErrorKind.AlreadyExists, error.kind());
    }
}
