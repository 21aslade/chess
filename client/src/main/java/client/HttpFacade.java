package client;

import chess.ChessGame.TeamColor;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import server.ServerInterface.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class HttpFacade implements ServerFacade {
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
    public AuthData login(String username, String password) throws ServerException {
        return null;
    }

    @Override
    public void logout(String authToken) throws ServerException {

    }

    @Override
    public List<GameData> listGames(String authToken) throws ServerException {
        return List.of();
    }

    @Override
    public int createGame(String authToken, String gameName) throws ServerException {
        return 0;
    }

    @Override
    public void joinGame(String authToken, int gameId, TeamColor color) throws ServerException {

    }

    private <T> T makeRequest(String method, String path, String authToken, Object body, Class<T> responseClass) throws
        ServerException {
        try {
            var url = (new URI(baseUrl + path)).toURL();

            var bodyText = body != null ? gson.toJson(body) : null;
            var connection = createRequest(url, method, authToken, bodyText);

            connection.connect();

            throwIfNotSuccessful(connection);
            return responseClass != null ? readBody(connection, responseClass) : null;
        } catch (Exception ex) {
            throw new ServerException(ex.getMessage());
        }
    }

    private HttpURLConnection createRequest(URL url, String method, String authToken, String body) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setDoOutput(true);

        if (authToken != null) {
            connection.setRequestProperty("authorization", authToken);
        }

        if (body != null) {
            connection.addRequestProperty("Content-Type", "application/json");
            try (OutputStream reqBody = connection.getOutputStream()) {
                reqBody.write(body.getBytes());
            }
        }

        return connection;
    }

    private <T> T readBody(HttpURLConnection connection, Class<T> responseClass) throws IOException {
        try (var response = connection.getInputStream()) {
            var reader = new InputStreamReader(response);
            return gson.fromJson(reader, responseClass);
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection connection) throws IOException {
        var status = connection.getResponseCode();
        if (isSuccessful(status)) {
            return;
        }

        if (connection.getContentLengthLong() == 0) {
            throw new ServerException("An unexpected error has occurred.");
        }

        try (var response = connection.getInputStream()) {
            var reader = new InputStreamReader(response);
            var error = gson.fromJson(reader, ResponseExceptionBody.class);
            throw new ServerException(error.message());
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
