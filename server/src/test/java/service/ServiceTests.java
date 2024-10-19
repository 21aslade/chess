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
    UserData user1 = new UserData("strength", "weakness", "journey@destination.com");
    UserData user2 = new UserData("favorite_color", "yellow", "bridge@death.com");
    String gameName = "game";

    MemoryDataAccess dataAccess = new MemoryDataAccess();

    @BeforeEach
    public void clearDatabase() {
        dataAccess.clearAuth();
        dataAccess.clearGames();
        dataAccess.clearUsers();
    }

    @Test
    public void registerUserSuccess() throws DataAccessException, ServiceException {
        var authData = Service.registerUser(user1, dataAccess);
        assertEquals(user1.username(), authData.username());
    }

    @Test
    public void registerUserExisting() throws DataAccessException, ServiceException {
        Service.registerUser(user1, dataAccess);

        var sameUsername = new UserData(user1.username(), "in", "numbers@hotmail.com");
        var error = assertThrows(ServiceException.class, () -> Service.registerUser(sameUsername, dataAccess));
        assertEquals(ErrorKind.AlreadyExists, error.kind());
    }

    @Test
    public void loginSuccess() throws DataAccessException, ServiceException {
        var auth1 = Service.registerUser(user1, dataAccess);
        var auth2 = Service.login(user1.username(), user1.password(), dataAccess);
        var auth3 = Service.login(user1.username(), user1.password(), dataAccess);
        assertEquals(user1.username(), auth2.username());
        assertNotEquals(auth1.authToken(), auth2.authToken());
        assertNotEquals(auth2.authToken(), auth3.authToken());
    }

    @Test
    public void loginNonexistent() {
        var error =
            assertThrows(ServiceException.class, () -> Service.login(user1.username(), user1.password(), dataAccess));
        assertEquals(ErrorKind.Unauthorized, error.kind());
    }

    @Test
    public void loginWrongPassword() throws ServiceException, DataAccessException {
        Service.registerUser(user2, dataAccess);
        assertThrows(ServiceException.class, () -> Service.login(user2.username(), "blue", dataAccess));
    }

    @Test
    public void logoutSuccess() throws ServiceException, DataAccessException {
        var auth = Service.registerUser(user1, dataAccess);

        Service.logout(auth.authToken(), dataAccess);
    }

    @Test
    public void logoutNonexistent() {
        var error = assertThrows(ServiceException.class, () -> Service.logout("nope way", dataAccess));
        assertEquals(ErrorKind.Unauthorized, error.kind());
    }

    @Test
    public void createGameSuccess() throws ServiceException, DataAccessException {
        var auth = Service.registerUser(user1, dataAccess);
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
        var auth = Service.registerUser(user1, dataAccess);
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
        var auth = Service.registerUser(user1, dataAccess);

        var error = assertThrows(
            ServiceException.class,
            () -> Service.joinGame(0, TeamColor.WHITE, auth.authToken(), dataAccess)
        );

        assertEquals(ErrorKind.DoesNotExist, error.kind());
    }

    @Test
    public void joinGameAlreadyTaken() throws ServiceException, DataAccessException {
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
