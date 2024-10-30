package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.List;

public class DBDataAccess implements DataAccess {
    public DBDataAccess() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void putUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void clearUsers() throws DataAccessException {

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

    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {

    }

    @Override
    public void clearAuth() throws DataAccessException {

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
