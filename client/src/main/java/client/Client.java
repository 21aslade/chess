package client;

import model.AuthData;
import model.GameData;
import model.UserData;

public class Client {
    private final ServerFacade server;
    private AuthData session;
    private GameData game;

    public enum State {
        LOGGED_OUT,
        LOGGED_IN,
        PLAYING
    }

    public Client(ServerFacade server) {
        this.server = server;
    }

    public State state() {
        if (session == null) {
            return State.LOGGED_OUT;
        } else if (game == null) {
            return State.LOGGED_IN;
        } else {
            return State.PLAYING;
        }
    }

    public void register(UserData user) {
        session = server.register(user);
    }

    public void login(String username, String password) {
        session = server.login(username, password);
    }
}
