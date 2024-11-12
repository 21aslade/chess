package client;

import chess.ChessGame.TeamColor;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;

public interface ServerFacade {
    void clear() throws ServerException;

    AuthData register(UserData user) throws ServerException;

    AuthData login(String username, String password) throws ServerException;

    void logout(String authToken) throws ServerException;

    List<GameData> listGames(String authToken) throws ServerException;

    int createGame(String authToken, String gameName) throws ServerException;

    void joinGame(String authToken, int gameId, TeamColor color) throws ServerException;
}
