package client;

import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
        facade.register(new UserData("oop", "si", "daisy"));
    }

    @Test
    public void registerDuplicate() {
        var user = new UserData("guess", "who's", "back");
        facade.register(user);
        assertThrows(ServerException.class, () -> facade.register(user));
    }
}
