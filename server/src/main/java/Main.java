import chess.*;
import server.Server;

public class Main {
    public static void main(String[] args) {
        var server = new Server();
        server.run(8080);
    }
}