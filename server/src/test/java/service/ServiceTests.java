package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServiceTests {
    @Test
    public void registerUserSuccess() throws DataAccessException, ServiceException {
        var dataAccess = new MemoryDataAccess();
        var user = new UserData("strength", "weakness", "journey@destination.com");
        var authData = Service.registerUser(user, dataAccess);
        assertEquals(user.username(), authData.username());
    }

    @Test
    public void registerUserFailure() throws DataAccessException, ServiceException {
        var dataAccess = new MemoryDataAccess();
        var user = new UserData("strength", "weakness", "journey@destination.com");
        Service.registerUser(user, dataAccess);

        var user2 = new UserData("strength", "in", "numbers@hotmail.com");
        var error = assertThrows(ServiceException.class, () -> Service.registerUser(user2, dataAccess));
        assertEquals(ServiceException.ErrorKind.AlreadyExists, error.kind());
    }
}
