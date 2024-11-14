import client.Client;
import client.HttpFacade;
import ui.Repl;

public class Main {
    public static void main(String[] args) {
        var url = args.length > 0 ? args[0] : "http://localhost:8080";

        var facade = new HttpFacade(url);
        var client = new Client(facade);
        Repl.run(client);
    }
}