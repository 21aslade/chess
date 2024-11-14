package client;

import model.AuthData;
import model.GameData;

public class Client {
    private AuthData session;
    private GameData game;

    public enum State {
        LOGGED_OUT,
        LOGGED_IN,
        PLAYING
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
}
