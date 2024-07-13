package dk.mwittrock.cpilint.auth;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.mwittrock.cpilint.util.HttpUtil;

public final class AuthorizationServer {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationServer.class);
    private static final String ACCESS_TOKEN_FIELD = "access_token";
    private static final String EXPIRES_IN_FIELD = "expires_in";
    private static final String TOKEN_TYPE_FIELD = "token_type";
    private static final String EXPECTED_TOKEN_TYPE = "bearer";

    private final HttpClient client;
    private final HttpRequest request;

    private AuthorizationServer(HttpClient client, HttpRequest request) {
        assert client != null;
        assert request != null;
        this.client = client;
        this.request = request;
    }

    public static AuthorizationServer newInstance(String clientId, String clientSecret, URI tokenUri) {
        Objects.requireNonNull(clientId, "clientId must not be null");
        Objects.requireNonNull(clientSecret, "clientSecret must not be null");
        Objects.requireNonNull(tokenUri, "tokenUri must not be null");
        if (clientId.isEmpty() || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId must not be empty or blank");
        }
        if (clientSecret.isEmpty() || clientSecret.isBlank()) {
            throw new IllegalArgumentException("clientSecret must not be empty or blank");
        }
        final String authHeader = "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
        HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(tokenUri)
            .header(HttpUtil.REQUEST_HEADER_AUTHORIZATION, authHeader)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
        return new AuthorizationServer(client, request);
    }

    public AccessToken requestAccessToken() {
        logger.info("Requesting access token from authorization server");
        HttpResponse<String> response;
        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new AuthorizationServerError("I/O error when requesting access token", e);
        }
        final int httpStatus = response.statusCode();
        if (httpStatus != HttpUtil.HTTP_OKAY_STATUS_CODE) {
            String message = String.format("Unexpected HTTP status code %d when requesting access token", httpStatus);
            throw new AuthorizationServerError(message);
        }
        JSONObject json;
        try {
            json = new JSONObject(response.body());
        } catch (JSONException e) {
            throw new AuthorizationServerError("Authorization server did not return valid JSON", e);
        }
        validateJsonResponse(json);
        String token = json.getString(ACCESS_TOKEN_FIELD);
        int expiresIn = json.getInt(EXPIRES_IN_FIELD);
        return new AccessToken(token, expiresIn);
    }

    private void validateJsonResponse(JSONObject json) {
        logger.info("Validating authorization server response");
        // Check that the fields we need are present.
        Collection<String> missingFields = Stream.of(ACCESS_TOKEN_FIELD, EXPIRES_IN_FIELD, TOKEN_TYPE_FIELD)
            .filter(f -> !json.has(f))
            .collect(Collectors.toSet());
        if (!missingFields.isEmpty()) {
            String message = String.format("Authorization server JSON response has missing fields: %s", missingFields.stream().collect(Collectors.joining(",")));
            throw new AuthorizationServerError(message);
        }
        // The reponse must contain the expected token type.
        String tokenType = json.getString(TOKEN_TYPE_FIELD);
        if (!tokenType.equals(EXPECTED_TOKEN_TYPE)) {
            throw new AuthorizationServerError("Unexpected token type received from authorization server: " + tokenType);
        }
        // The expiry field must contain an int.
        try {
            json.getInt(EXPIRES_IN_FIELD);
        } catch (JSONException e) {
            // The field is there so this exception means its contents could not be converted to int.
            throw new AuthorizationServerError("Token expiry in authorization server JSON response could not be converted to int");
        }
        logger.info("Authorization server response was valid");
    }

}