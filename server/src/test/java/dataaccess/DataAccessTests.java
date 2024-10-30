package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessTests {
    static class Implementations implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws
            DataAccessException {
            var memory = new MemoryDataAccess();
            var database = new DBDataAccess();
            database.clearAuth();
            database.clearGames();
            database.clearUsers();
            return Stream.of(memory, database).map(Arguments::of);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(Implementations.class)
    void emptyGetUser(DataAccess dataAccess) throws DataAccessException {
        assertNull(dataAccess.getUser("anything"));
    }

    @ParameterizedTest
    @ArgumentsSource(Implementations.class)
    void putUser(DataAccess dataAccess) throws DataAccessException {
        var user = new UserData("beans", "paradox", "em");
        dataAccess.putUser(user);
        var result = dataAccess.getUser(user.username());
        assertEquals(user, result);
    }

    @ParameterizedTest
    @ArgumentsSource(Implementations.class)
    void clearUsers(DataAccess dataAccess) throws DataAccessException {
        var user = new UserData("beans", "paradox", "em");
        dataAccess.putUser(user);
        dataAccess.clearUsers();
        assertNull(dataAccess.getUser(user.username()));
    }

    @ParameterizedTest
    @ArgumentsSource(Implementations.class)
    void emptyGetGames(DataAccess dataAccess) throws DataAccessException {
        assertEquals(0, dataAccess.getGames().size());
    }

    @ParameterizedTest
    @ArgumentsSource(Implementations.class)
    void putGame(DataAccess dataAccess) throws DataAccessException {
        var game = new GameData(3, "apple", "dumpling", "gang", new ChessGame());
        dataAccess.putGame(game);
        var result = dataAccess.getGame(game.gameID());
        assertEquals(game, result);

        var game2 = new GameData(game.gameID(), "never", "before", "seen", new ChessGame());
        dataAccess.putGame(game2);
        var result2 = dataAccess.getGame(game2.gameID());
        assertEquals(game2, result2);
    }

    @ParameterizedTest
    @ArgumentsSource(Implementations.class)
    void getGames(DataAccess dataAccess) throws DataAccessException {
        var game = new GameData(3, "apple", "dumpling", "gang", new ChessGame());
        dataAccess.putGame(game);
        var game2 = new GameData(4, "never", "before", "seen", new ChessGame());
        dataAccess.putGame(game2);

        var expected = List.of(game, game2);
        var actual = dataAccess.getGames();
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ArgumentsSource(Implementations.class)
    void clearGames(DataAccess dataAccess) throws DataAccessException {
        var game = new GameData(3, "apple", "dumpling", "gang", new ChessGame());
        dataAccess.putGame(game);
        dataAccess.clearGames();
        assertNull(dataAccess.getGame(game.gameID()));
    }

    @ParameterizedTest
    @ArgumentsSource(Implementations.class)
    void emptyGetAuth(DataAccess dataAccess) throws DataAccessException {
        assertNull(dataAccess.getAuth("anything"));
    }

    @ParameterizedTest
    @ArgumentsSource(Implementations.class)
    void putAuth(DataAccess dataAccess) throws DataAccessException {
        var auth = new AuthData("oh", "yeah");
        dataAccess.putAuth(auth);
        var result = dataAccess.getAuth(auth.authToken());
        assertEquals(auth, result);
    }

    @ParameterizedTest
    @ArgumentsSource(Implementations.class)
    void deleteAuth(DataAccess dataAccess) throws DataAccessException {
        var auth1 = new AuthData("oh", "yeah");
        dataAccess.putAuth(auth1);
        var auth2 = new AuthData("yes", "sir");
        dataAccess.putAuth(auth2);

        dataAccess.deleteAuth(auth1.authToken());

        assertNull(dataAccess.getAuth(auth1.authToken()));
        assertEquals(dataAccess.getAuth(auth2.authToken()), auth2);
    }

    @ParameterizedTest
    @ArgumentsSource(Implementations.class)
    void clearAuth(DataAccess dataAccess) throws DataAccessException {
        var auth = new AuthData("oh", "yeah");
        dataAccess.putAuth(auth);
        dataAccess.clearAuth();
        assertNull(dataAccess.getAuth(auth.authToken()));
    }
}
