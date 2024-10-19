package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        var user = new UserData("strength", "weakness", "journey@destination.com");
        Service.registerUser(user, dataAccess);

        var user2 = new UserData("strength", "in", "numbers@hotmail.com");
        var error = assertThrows(ServiceException.class, () -> Service.registerUser(user2, dataAccess));
        assertEquals(ServiceException.ErrorKind.AlreadyExists, error.kind());
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
        assertEquals(ServiceException.ErrorKind.DoesNotExist, error.kind());
    }

    @Test
    public void loginWrongPassword() throws ServiceException, DataAccessException {
        var user = new UserData("favorite_color", "yellow", "journey@destination.com");
        Service.registerUser(user, dataAccess);

        assertThrows(ServiceException.class, () -> Service.login(user.username(), "blue", dataAccess));
    }

    @Test
    public void logoutSuccess() throws ServiceException, DataAccessException {
        var user = new UserData("strength", "weakness", "journey@destination.com");
        var auth1 = Service.registerUser(user, dataAccess);

        Service.logout(auth1.authToken(), dataAccess);
    }

    @Test
    public void logoutNonexistent() {
        var error = assertThrows(ServiceException.class, () -> Service.logout("nope way", dataAccess));
        assertEquals(ServiceException.ErrorKind.DoesNotExist, error.kind());
    }
}
