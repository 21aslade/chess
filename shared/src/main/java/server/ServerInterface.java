package server;

import chess.ChessGame;
import model.GameData;

import java.util.List;

// This class is just a namespace. Oh, Java...
public class ServerInterface {
    public record LoginRequest(String username, String password) {}

    public record ListGamesResponse(List<GameData> games) {}

    public record CreateGameRequest(String gameName) {}

    public record CreateGameResponse(int gameID) {}

    public record JoinGameRequest(ChessGame.TeamColor playerColor, int gameID) {}

    public record ResponseExceptionBody(String message) {}
}
