package ui;

import chess.ChessBoard;
import chess.ChessGame.TeamColor;
import client.Client;
import model.GameData;
import model.UserData;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import static ui.EscapeSequences.*;

public class Repl {
    private static final List<ReplCommand> LOGGED_OUT_COMMANDS = List.of(
        new ReplCommand("register", List.of("username", "password", "email"), "create account", Repl::handleRegister),
        new ReplCommand("login", List.of("username", "password"), "log in to play chess", Repl::handleLogin),
        new ReplCommand("quit", List.of(), "quit the repl", Repl::handleQuit),
        new ReplCommand("help", List.of(), "show possible commands", Repl::handleHelp)
    );

    private static final List<ReplCommand> LOGGED_IN_COMMANDS = List.of(
        new ReplCommand("create", List.of("gameName"), "create new game", Repl::handleCreate),
        new ReplCommand("list", List.of(), "list all games", Repl::handleList),
        new ReplCommand("observe", List.of("id"), "observe chess game", Repl::handleObserve),
        new ReplCommand("join", List.of("id", "white|black"), "join chess game", Repl::handleJoin),
        new ReplCommand("logout", List.of(), "end session", Repl::handleLogout),
        new ReplCommand("quit", List.of(), "quit the repl", Repl::handleQuit),
        new ReplCommand("help", List.of(), "show possible commands", Repl::handleHelp)
    );

    private static final List<ReplCommand> GAME_COMMANDS = List.of(
        new ReplCommand("board", List.of(), "show board", Repl::handleBoard),
        new ReplCommand("help", List.of(), "show possible commands", Repl::handleHelp)
    );

    private static final ReplCommand MOVE_COMMAND =
        new ReplCommand("move", List.of("src", "dest"), "make move", Repl::handleMove);

    public static void run(Client client) {
        var scanner = new Scanner(System.in);
        client.setWsHandler((m) -> Repl.handleMessage(client, m));

        System.out.println("Welcome! Type \"help\" for a list of commands.");

        while (true) {
            System.out.print("> ");
            System.out.flush();

            var line = scanner.nextLine();
            var result = runCommand(client, line);
            if (result == null) {
                break;
            }
            System.out.println(result);
        }
    }

    private static String runCommand(Client client, String line) {
        var commands = availableCommands(client);
        var command = line.trim().split(" ", 2);

        var chosenCommand = commands
            .filter((c) -> c.name().equals(command[0]))
            .findAny()
            .orElse(null);

        if (chosenCommand == null) {
            return "Unknown command \"" + command[0] + "\". Type \"help\" for a list of commands.";
        }

        var args = command.length > 1 ? command[1].split(" ") : new String[] {};
        if (args.length != chosenCommand.args().size()) {
            return "Usage: " + chosenCommand.usageText();
        }

        try {
            return chosenCommand.action().run(client, args);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private static Stream<ReplCommand> availableCommands(Client client) {
        return switch (client.state()) {
            case LOGGED_OUT -> LOGGED_OUT_COMMANDS.stream();
            case LOGGED_IN -> LOGGED_IN_COMMANDS.stream();
            case PLAYING -> {
                if (client.canMove()) {
                    yield Stream.concat(Stream.of(MOVE_COMMAND), GAME_COMMANDS.stream());
                } else {
                    yield GAME_COMMANDS.stream();
                }
            }
        };
    }

    private static String handleHelp(Client client, String[] args) {
        return helpText(availableCommands(client));
    }

    private static String handleQuit(Client client, String[] args) {
        client.quit();
        return null;
    }

    private static String handleRegister(Client client, String[] args) {
        var userData = new UserData(args[0], args[1], args[2]);
        client.register(userData);
        return "Welcome, " + args[0] + "!";
    }

    private static String handleLogin(Client client, String[] args) {
        client.login(args[0], args[1]);
        return "Welcome, " + args[0] + "!";
    }

    private static String handleLogout(Client client, String[] args) {
        client.logout();
        return "Logged out successfully.";
    }

    private static String handleCreate(Client client, String[] args) {
        client.createGame(args[0]);
        return "Created game " + args[0];
    }

    private static String handleList(Client client, String[] args) {
        var games = client.listGames();
        if (games.isEmpty()) {
            return "No games found.";
        }

        var result = new StringBuilder().append("Games:");
        for (var i = 0; i < games.size(); i++) {
            formatGame(i + 1, games.get(i), result);
        }

        return result.toString();
    }

    private static void formatGame(int index, GameData game, StringBuilder result) {
        result.append("\n ")
            .append(index)
            .append(" - ")
            .append(game.gameName());

        var hasWhite = game.whiteUsername() != null;
        var hasBlack = game.blackUsername() != null;
        var hasBoth = hasWhite && hasBlack;

        if (!hasWhite && !hasBlack) {
            return;
        }

        result.append(" (")
            .append(hasWhite ? "white: " + game.whiteUsername() : "")
            .append(hasBoth ? ", " : "")
            .append(hasBlack ? "black: " + game.blackUsername() : "")
            .append(")");
    }

    private static String handleObserve(Client client, String[] args) {
        int gameId;
        try {
            gameId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            return "Error: id must be a valid integer";
        }

        client.observeGame(gameId);

        var board = new ChessBoard();
        board.resetBoard();
        return PrintBoard.printBoard(board, TeamColor.WHITE) +
            "\n" +
            PrintBoard.printBoard(board, TeamColor.BLACK);
    }

    private static String handleJoin(Client client, String[] args) {
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            return "Error: id must be a valid integer";
        }

        var team = switch (args[1].toLowerCase()) {
            case "white" -> TeamColor.WHITE;
            case "black" -> TeamColor.BLACK;
            default -> null;
        };

        if (team == null) {
            return "Error: team must be white or black";
        }

        client.joinGame(gameNumber, team);

        return ERASE_SCREEN;
    }

    private static String handleBoard(Client client, String[] args) {
        return PrintBoard.printBoard(client.board(), client.team());
    }

    private static String handleMove(Client client, String[] args) {
        return "";
    }

    private static void handleMessage(Client client, ServerMessage message) {
        System.out.println();
        var result = switch (message) {
            case LoadGameMessage g -> PrintBoard.printBoard(client.board(), client.team());
            case NotificationMessage n -> n.message();
            case ErrorMessage e -> SET_TEXT_COLOR_RED + e.message() + SET_TEXT_COLOR_WHITE;
            default -> throw new IllegalStateException("Unexpected message: " + message);
        };

        System.out.println(result);
        System.out.print("> ");
        System.out.flush();
    }

    private interface ReplAction {
        String run(Client client, String[] args);
    }

    private record ReplCommand(String name, List<String> args, String description, ReplAction action) {
        public String usageText() {
            var argsText = !args.isEmpty() ? " " + String.join(" ", args) : "";
            return name + argsText + " - " + description;
        }
    }

    private static String helpText(Stream<ReplCommand> commands) {
        var usage = commands.map(ReplCommand::usageText).reduce("", (a, s) -> a + "\n - " + s);
        return "Usage:" + usage;
    }
}
