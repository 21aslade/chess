package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.DBDataAccess;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import server.ServerInterface.*;
import service.Service;
import service.ServiceException;
import spark.*;
import websocket.WebSocketHandler;

public class Server {
    private final WebSocketHandler ws;
    private final DataAccess data;
    private final Gson gson = new Gson();

    public Server() {
        try {
            data = new DBDataAccess();
        } catch (DataAccessException e) {
            System.err.println("Database initialization failed");
            throw new RuntimeException("Failed to initialize database");
        }

        ws = new WebSocketHandler(data);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.webSocket("/ws", ws);

        Spark.delete("/db", this::clear);
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.get("/game", this::listGames);
        Spark.exception(ResponseException.class, this::exceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void exceptionHandler(ResponseException ex, Request req, Response res) {
        res.status(ex.httpCode());
        res.body(gson.toJson(new ResponseExceptionBody(ex.getMessage())));
    }

    private <T> String route(Request req, Response res, Class<T> body, HandlerFunction<T> callback) throws
        ResponseException {
        res.type("application/json");
        try {
            var request = body != null ? gson.fromJson(req.body(), body) : null;
            var authToken = req.headers("authorization");
            res.status(200);
            return callback.run(authToken, request);
        } catch (JsonSyntaxException e) {
            throw new ResponseException(400, "Error: bad request");
        } catch (DataAccessException e) {
            throw new ResponseException(500, "Error: " + e.getMessage());
        } catch (ServiceException e) {
            throw switch (e.kind()) {
                case AlreadyExists -> new ResponseException(403, "Error: already taken");
                case NullInput -> new ResponseException(400, "Error: bad request");
                case DoesNotExist -> new ResponseException(400, "Error: does not exist");
                case Unauthorized -> new ResponseException(401, "Error: unauthorized");
                case LoginFail -> new ResponseException(401, "Error: incorrect username or password");
            };
        }
    }

    private Object register(Request req, Response res) throws ResponseException {
        return route(
            req, res, UserData.class, (auth, user) -> {
                var authData = Service.registerUser(user, data);
                return gson.toJson(authData);
            }
        );
    }

    private Object login(Request req, Response res) throws ResponseException {
        return route(
            req, res, LoginRequest.class, (auth, request) -> {
                var authData = Service.login(request.username(), request.password(), data);
                return gson.toJson(authData);
            }
        );
    }

    private Object logout(Request req, Response res) throws ResponseException {
        return route(
            req, res, null, (auth, request) -> {
                Service.logout(auth, data);
                return "{}";
            }
        );
    }

    private Object createGame(Request req, Response res) throws ResponseException {
        return route(
            req, res, CreateGameRequest.class, (auth, request) -> {
                var id = Service.createGame(request.gameName(), auth, data);
                return gson.toJson(new CreateGameResponse(id));
            }
        );
    }

    private Object joinGame(Request req, Response res) throws ResponseException {
        return route(
            req, res, JoinGameRequest.class, (auth, request) -> {
                Service.joinGame(request.gameID(), request.playerColor(), auth, data);
                return "{}";
            }
        );
    }

    private Object listGames(Request req, Response res) throws ResponseException {
        return route(
            req, res, null, (auth, request) -> {
                var games = Service.listGames(auth, data);
                return gson.toJson(new ListGamesResponse(games));
            }
        );
    }

    private Object clear(Request req, Response res) throws ResponseException {
        return route(
            req, res, null, (auth, request) -> {
                Service.clear(data);
                return "{}";
            }
        );
    }

    private interface HandlerFunction<T> {
        String run(String authToken, T body) throws DataAccessException, ServiceException;
    }
}
