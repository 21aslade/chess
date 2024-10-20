package model;

import chess.ChessGame;
import chess.ChessGame.TeamColor;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    public String user(TeamColor team) {
        return switch (team) {
            case WHITE -> this.whiteUsername;
            case BLACK -> this.blackUsername;
        };
    }

    public GameData withUser(TeamColor team, String username) {
        return switch (team) {
            case WHITE -> new GameData(gameID, username, blackUsername, gameName, game);
            case BLACK -> new GameData(gameID, whiteUsername, username, gameName, game);
        };
    }
}
