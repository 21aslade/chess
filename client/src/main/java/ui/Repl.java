package ui;

import client.Client;
import model.UserData;

import java.util.List;
import java.util.Scanner;

public class Repl {
    private static final List<ReplCommand> loggedOutCommands = List.of(
        new ReplCommand("help", List.of(), "show possible commands", Repl::handleHelp),
        new ReplCommand("quit", List.of(), "quit the repl", (_c, _a) -> null),
        new ReplCommand("register", List.of("username", "password", "email"), "create account", Repl::handleRegister),
        new ReplCommand("login", List.of("username", "password"), "log in to play chess", Repl::handleLogin)
    );

    private static final List<ReplCommand> loggedInCommands = List.of(
        new ReplCommand("help", List.of(), "show possible commands", Repl::handleHelp)
    );

    private static final List<ReplCommand> gameCommands = List.of(
        new ReplCommand("help", List.of(), "show possible commands", Repl::handleHelp)
    );

    public static void run(Client client) {
        var scanner = new Scanner(System.in);

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
        var command = line.split(" ", 2);

        var chosenCommand = commands.stream()
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

    private static List<ReplCommand> availableCommands(Client client) {
        return switch (client.state()) {
            case LOGGED_OUT -> loggedOutCommands;
            case LOGGED_IN -> loggedInCommands;
            case PLAYING -> gameCommands;
        };
    }

    private static String handleHelp(Client client, String[] _args) {
        return helpText(availableCommands(client));
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

    private interface ReplAction {
        String run(Client client, String[] args);
    }

    private record ReplCommand(String name, List<String> args, String description, ReplAction action) {
        public String usageText() {
            var argsText = !args.isEmpty() ? " " + String.join(" ", args) : "";
            return name + argsText + " - " + description;
        }
    }

    private static String helpText(List<ReplCommand> commands) {
        var usage = commands.stream().map(ReplCommand::usageText).reduce("", (a, s) -> a + "\n - " + s);
        return "Usage:" + usage;
    }
}