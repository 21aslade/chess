package websocket;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private final DataAccess data;
    private final Gson gson = new Gson();
    private final ConcurrentHashMap<Integer, ConnectionManager> managers = new ConcurrentHashMap<>();

    public WebSocketHandler(DataAccess data) {
        this.data = data;
    }

    @OnWebSocketMessage
    public void onCommand(Session session, String commandText) {
        var command = deserializeCommand(commandText);
        
    }

    private UserGameCommand deserializeCommand(String messageText) {
        var command = gson.fromJson(messageText, UserGameCommand.class);
        return switch (command.getCommandType()) {
            case CONNECT, LEAVE, RESIGN -> command;
            case MAKE_MOVE -> gson.fromJson(messageText, MakeMoveCommand.class);
        };
    }
}
