package server;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.GameData;
import model.UserData;
import service.Service;
import service.ServiceException;
import spark.*;

import java.util.List;

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
        record ResponseExceptionBody(String message) {}
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
                case DoesNotExist, NullInput -> new ResponseException(400, "Error: bad request");
                case Unauthorized -> new ResponseException(401, "Error: unauthorized");
            };
        }
    }

    private Object register(Request req, Response res) throws ResponseException {
        return route(req, res, UserData.class, (auth, user) -> {
            var authData = Service.registerUser(user, data);
            return gson.toJson(authData);
        });
    }

    private Object login(Request req, Response res) throws ResponseException {
        record LoginRequest(String username, String password) {}
        return route(req, res, LoginRequest.class, (auth, request) -> {
            var authData = Service.login(request.username(), request.password(), data);
            return gson.toJson(authData);
        });
    }

    private Object logout(Request req, Response res) throws ResponseException {
        return route(req, res, null, (auth, request) -> {
            Service.logout(auth, data);
            return "{}";
        });
    }

    private Object createGame(Request req, Response res) throws ResponseException {
        record CreateGameRequest(String gameName) {}
        record CreateGameResponse(int gameID) {}
        return route(req, res, CreateGameRequest.class, (auth, request) -> {
            var game = Service.createGame(request.gameName(), auth, data);
            return gson.toJson(new CreateGameResponse(game.gameID()));
        });
    }

    private Object joinGame(Request req, Response res) throws ResponseException {
        record JoinGameRequest(ChessGame.TeamColor playerColor, int gameID) {}
        return route(req, res, JoinGameRequest.class, (auth, request) -> {
            Service.joinGame(request.gameID(), request.playerColor(), auth, data);
            return "{}";
        });
    }

    private Object listGames(Request req, Response res) throws ResponseException {
        record ListGamesResponse(List<GameData> games) {}
        return route(req, res, null, (auth, request) -> {
            var games = Service.listGames(auth, data);
            return gson.toJson(new ListGamesResponse(games));
        });
    }

    private Object clear(Request req, Response res) throws ResponseException {
        return route(req, res, null, (auth, request) -> {
            Service.clear(data);
            return "{}";
        });
    }

    private interface HandlerFunction<T> {
        String run(String authToken, T body) throws DataAccessException, ServiceException;
    }
}
