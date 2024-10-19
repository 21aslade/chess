package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class Service {
    public static AuthData registerUser(UserData user, DataAccess data) throws DataAccessException, ServiceException {
        var current = data.getUser(user.username());
        if (current != null) {
            throw new ServiceException(ServiceException.ErrorKind.AlreadyExists);
        }

        data.putUser(user);

        return createSession(user, data);
    }

    private static AuthData createSession(UserData user, DataAccess data) throws DataAccessException {
        var uuid = UUID.randomUUID().toString();
        var authData = new AuthData(uuid, user.username());
        data.putAuth(authData);

        return authData;
    }
}
