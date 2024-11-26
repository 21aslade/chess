package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;

public class Connection {
    public final String id;
    public final Session session;

    public Connection(String id, Session session) {
        this.id = id;
        this.session = session;
    }

    public void send(ServerMessage msg) throws IOException {
        session.getRemote().sendString(new Gson().toJson(msg));
    }
}