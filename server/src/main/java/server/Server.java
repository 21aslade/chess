package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.UserData;
import service.Service;
import service.ServiceException;
import spark.*;

public class Server {
    private final DataAccess data = new MemoryDataAccess();
    private final Gson gson = new Gson();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.delete("/db", this::clear);
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.post("/game", this::createGame);
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
        res.body(ex.toJson());
    }

    private <T> T tryFromJson(String json, Class<T> classOfT) throws ResponseException {
        try {
            return gson.fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            throw new ResponseException(400, "Error: bad request");
        }
    }

    private static <T> T tryRun(ServerFunction<T> callback) throws ResponseException {
        try {
            return callback.run();
        } catch (DataAccessException e) {
            throw new ResponseException(500, "Error: " + e.getMessage());
        } catch (ServiceException e) {
            throw switch (e.kind()) {
                case AlreadyExists -> new ResponseException(403, "Error: already taken");
                case DoesNotExist, NullInput -> new ResponseException(400, "Error: bad request");
                case Unauthorized -> new ResponseException(401, "Error: unauthorized");
            };
        }
    }

    private Object register(Request req, Response res) throws ResponseException {
        res.type("application/json");
        var user = tryFromJson(req.body(), UserData.class);
        var authData = Server.tryRun(() -> Service.registerUser(user, data));

        res.status(200);
        return gson.toJson(authData);
    }

    private Object login(Request req, Response res) throws ResponseException {
        res.type("application/json");
        var request = tryFromJson(req.body(), LoginRequest.class);
        var authData = Server.tryRun(() -> Service.login(request.username(), request.password(), data));

        res.status(200);
        return gson.toJson(authData);
    }

    private Object logout(Request req, Response res) throws ResponseException {
        res.type("application/json");
        var authToken = req.headers("authorization");
        Server.tryRun(() -> {
            Service.logout(authToken, data);
            return false; // Dummy return so the interface works :(
        });

        res.status(200);
        return "{}";
    }

    private Object createGame(Request req, Response res) throws ResponseException {
        res.type("application/json");
        var request = tryFromJson(req.body(), CreateGameRequest.class);
        var authToken = req.headers("authorization");
        var game = Server.tryRun(() -> Service.createGame(request.gameName(), authToken, data));

        res.status(200);
        return gson.toJson(new CreateGameResponse(game.gameId()));
    }

    private Object clear(Request req, Response res) throws ResponseException {
        res.type("application/json");
        Server.tryRun(() -> {
            Service.clear(data);
            return false; // Dummy return so the interface works :(
        });

        res.status(200);
        return "{}";
    }

    private interface ServerFunction<T> {
        T run() throws DataAccessException, ServiceException;
    }
}
