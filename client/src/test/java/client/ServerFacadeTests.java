package client;

import chess.ChessGame;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {
    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new HttpFacade("http://localhost:" + port);
    }

    @BeforeEach
    public void clearDatabase() {
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void register() {
        var user = new UserData("oop", "si", "daisy");
        var authData = facade.register(user);
        assertEquals(user.username(), authData.username());
        assertNotNull(authData.authToken());
    }

    @Test
    public void registerDuplicate() {
        var user = new UserData("guess", "who's", "back");
        facade.register(user);
        assertThrows(ServerException.class, () -> facade.register(user));
    }

    @Test
    public void login() {
        var user = new UserData("a", "b", "c");
        facade.register(user);

        var authData = facade.login(user.username(), user.password());
        assertEquals(user.username(), authData.username());
        assertNotNull(authData.authToken());
    }

    @Test
    public void loginNonexistent() {
        assertThrows(ServerException.class, () -> facade.login("baleeted", "single deuce"));
    }

    @Test
    public void logout() {
        var user = new UserData("n", "o", "p");
        var authData = facade.register(user);

        facade.logout(authData.authToken());
    }

    @Test
    public void logoutNonexistent() {
        assertThrows(ServerException.class, () -> facade.logout("open sesame"));
    }

    @Test
    public void createGame() {
        var authData = facade.register(new UserData("unnecessary", "details", "enclosed"));
        int id = facade.createGame(authData.authToken(), "fhqwhgads");
        assertNotEquals(0, id);
    }

    @Test
    public void createGameUnauthorized() {
        assertThrows(ServerException.class, () -> facade.createGame("dynamite", "gal"));
    }

    @Test
    public void listGames() {
        var authData = facade.register(new UserData("unnecessary", "details", "enclosed"));
        var gameName = "???";
        int id = facade.createGame(authData.authToken(), gameName);

        var games = facade.listGames(authData.authToken());
        assertTrue(games.stream().anyMatch((game) -> game.gameID() == id && game.gameName().equals(gameName)));
    }

    @Test
    public void listGamesEmpty() {
        var authData = facade.register(new UserData("unnecessary", "details", "enclosed"));
        var games = facade.listGames(authData.authToken());
        assertEquals(List.of(), games);
    }

    @Test
    public void joinGame() {
        var authData = facade.register(new UserData("unnecessary", "details", "enclosed"));
        int id = facade.createGame(authData.authToken(), "foo boo grass");

        facade.joinGame(authData.authToken(), id, ChessGame.TeamColor.BLACK);
    }

    @Test
    public void joinGameTaken() {
        var authData1 = facade.register(new UserData("unnecessary", "details", "enclosed"));
        var authData2 = facade.register(new UserData("subsequently", "changed", "tack"));
        int id = facade.createGame(authData1.authToken(), "foo boo grass");

        facade.joinGame(authData1.authToken(), id, ChessGame.TeamColor.BLACK);
        assertThrows(
            ServerException.class,
            () -> facade.joinGame(authData2.authToken(), id, ChessGame.TeamColor.BLACK)
        );
    }
}
