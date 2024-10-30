package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DBDataAccess implements DataAccess {
    public DBDataAccess() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void putUser(UserData user) throws DataAccessException {
        var statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        executeStatement(statement, user.username(), user.password(), user.email());
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var statement = "SELECT password, email FROM user WHERE username=?";
        return executeQuery(statement, (rs) -> {
            if (!rs.next()) { return null; }
            var password = rs.getString(1);
            var email = rs.getString(2);
            return new UserData(username, password, email);
        }, username);
    }

    @Override
    public void clearUsers() throws DataAccessException {
        executeStatement("TRUNCATE user");
    }

    @Override
    public void putGame(GameData game) throws DataAccessException {

    }

    @Override
    public int gameCount() throws DataAccessException {
        return 0;
    }

    @Override
    public GameData getGame(int gameId) throws DataAccessException {
        return null;
    }

    @Override
    public List<GameData> getGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public void clearGames() throws DataAccessException {

    }

    @Override
    public void putAuth(AuthData auth) throws DataAccessException {
        var statement = "INSERT INTO auth (auth_token, username) VALUES (?, ?)";
        executeStatement(statement, auth.authToken(), auth.username());
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        var statement = "SELECT username FROM auth WHERE auth_token=?";
        return executeQuery(statement, (rs) -> {
            if (!rs.next()) { return null; }
            var username = rs.getString(1);
            return new AuthData(token, username);
        }, token);
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        var statement = "DELETE FROM auth WHERE auth_token=?";
        executeStatement(statement, token);
    }

    @Override
    public void clearAuth() throws DataAccessException {
        executeStatement("TRUNCATE auth");
    }

    private void executeStatement(String statement, Object... params) throws DataAccessException {
        try (
            var connection = DatabaseManager.getConnection();
            var prepared = connection.prepareStatement(statement)
        ) {
            for (int i = 0; i < params.length; i++) {
                var param = params[i];
                switch (param) {
                    case String s -> prepared.setString(i + 1, s);
                    case Integer n -> prepared.setInt(i + 1, n);
                    case Boolean b -> prepared.setBoolean(i + 1, b);
                    default -> throw new RuntimeException("Object of unknown type detected");
                }
            }
            prepared.execute();
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    interface ResultSetFn<T> {
        T apply(ResultSet r) throws SQLException;
    }

    private <T> T executeQuery(String statement, ResultSetFn<T> f, Object... params) throws
        DataAccessException {
        try (
            var connection = DatabaseManager.getConnection();
            var prepared = connection.prepareStatement(statement)
        ) {
            for (int i = 0; i < params.length; i++) {
                var param = params[i];
                switch (param) {
                    case String s -> prepared.setString(i + 1, s);
                    case Integer n -> prepared.setInt(i + 1, n);
                    case Boolean b -> prepared.setBoolean(i + 1, b);
                    default -> throw new RuntimeException("Object of unknown type detected");
                }
            }
            return f.apply(prepared.executeQuery());
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private final String[] createStatements = {
        """
            CREATE TABLE IF NOT EXISTS user (
              `username` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL,
              PRIMARY KEY (`username`)
            ) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
        """
            CREATE TABLE IF NOT EXISTS auth (
              `auth_token` varchar(256) NOT NULL,
              `username` varchar(256) NOT NULL,
              PRIMARY KEY (`auth_token`)
            ) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
        """
            CREATE TABLE IF NOT EXISTS gameData (
              `id` int NOT NULL AUTO_INCREMENT,
              `white_username` varchar(256),
              `black_username` varchar(256),
              `game_name` varchar(256) NOT NULL,
              `game` text NOT NULL,
              PRIMARY KEY (`id`)
            ) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var connection = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var prepared = connection.prepareStatement(statement)) {
                    prepared.execute();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
