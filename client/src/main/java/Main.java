import client.Client;
import client.HttpFacade;
import ui.Repl;

public class Main {
    public static void main(String[] args) {
        var url = args.length > 0 ? args[0] : "http://localhost:8080";

        var client = new Client(url);
        Repl.run(client);
    }
}