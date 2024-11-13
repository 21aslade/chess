package client;

import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;

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
}
