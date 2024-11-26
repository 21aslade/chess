package client;

import chess.ChessGame.TeamColor;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import server.ServerInterface.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

public class HttpFacade implements ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String baseUrl;

    public HttpFacade(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void clear() {
        makeRequest("DELETE", "/db", null, null, null);
    }

    @Override
    public AuthData register(UserData user) {
        return makeRequest("POST", "/user", null, user, AuthData.class);
    }

    @Override
    public AuthData login(String username, String password) {
        var request = new LoginRequest(username, password);
        return makeRequest("POST", "/session", null, request, AuthData.class);
    }

    @Override
    public void logout(String authToken) throws ServerException {
        makeRequest("DELETE", "/session", authToken, null, null);
    }

    @Override
    public List<GameData> listGames(String authToken) throws ServerException {
        var response = makeRequest("GET", "/game", authToken, null, ListGamesResponse.class);
        assert response != null;
        return response.games();
    }

    @Override
    public int createGame(String authToken, String gameName) throws ServerException {
        var request = new CreateGameRequest(gameName);
        var response = makeRequest("POST", "/game", authToken, request, CreateGameResponse.class);
        assert response != null;
        return response.gameID();
    }

    @Override
    public void joinGame(String authToken, int gameId, TeamColor color) throws ServerException {
        var request = new JoinGameRequest(color, gameId);
        makeRequest("PUT", "/game", authToken, request, JoinGameRequest.class);
    }

    private <T> T makeRequest(String method, String path, String authToken, Object body, Class<T> responseClass) throws
        ServerException {
        var uri = URI.create(baseUrl + path);
        var bodyText = body != null ? gson.toJson(body) : null;
        var request = createRequest(uri, method, authToken, bodyText);

        try {
            var response = client.send(request, BodyHandlers.ofString());
            return handleResponse(response, responseClass);
        } catch (IOException | InterruptedException ex) {
            throw new ServerException(ex.getMessage());
        }
    }

    private HttpRequest createRequest(URI uri, String method, String authToken, String body) {
        var bodyPublisher = body != null ? BodyPublishers.ofString(body) : BodyPublishers.noBody();
        var request = HttpRequest.newBuilder(uri)
            .method(method, bodyPublisher);

        if (authToken != null) {
            request.setHeader("authorization", authToken);
        }

        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }

        return request.build();
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) {
        if (!isSuccessful(response.statusCode())) {
            var error = gson.fromJson(response.body(), ResponseExceptionBody.class);
            throw new ServerException(error.message());
        }
        if (responseClass == null) {
            return null;
        }
        return gson.fromJson(response.body(), responseClass);
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
