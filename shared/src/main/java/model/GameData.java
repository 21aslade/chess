package model;

import chess.ChessGame;
import chess.ChessGame.TeamColor;

public record GameData(int gameId, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    public String user(TeamColor team) {
        return switch (team) {
            case WHITE -> this.whiteUsername;
            case BLACK -> this.blackUsername;
        };
    }

    public GameData withUser(TeamColor team, String username) {
        return switch (team) {
            case WHITE -> new GameData(gameId, username, blackUsername, gameName, game);
            case BLACK -> new GameData(gameId, whiteUsername, username, gameName, game);
        };
    }
}
