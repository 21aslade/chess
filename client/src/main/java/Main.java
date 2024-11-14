import chess.*;
import client.Client;
import ui.Repl;

public class Main {
    public static void main(String[] args) {
        var client = new Client();
        Repl.run(client);
    }
}