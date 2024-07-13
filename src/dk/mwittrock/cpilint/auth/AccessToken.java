package dk.mwittrock.cpilint.auth;

import java.util.Objects;

public final class AccessToken {

    private static final long EXPIRY_BUFFER_MILLIS = 10 * 1000;

    private final String token;
    private final long expirationTimeMillis;

    AccessToken(String token, int lifetimeSeconds) {
        Objects.requireNonNull(token, "token must not be null");
        if (token.isEmpty() || token.isBlank()) {
            throw new IllegalArgumentException("token must not be empty or blank");
        }
        this.token = token;
        if (lifetimeSeconds <= 0) {
            throw new IllegalArgumentException("lifetimeSeconds must be greater than zero");
        }
        /*
         * Why subtract a buffer when calculating the expiration time? It's partly to account for
         * the elapsed time between the token creation and the instantiation of this object and
         * partly to avoid authentication errors happening when a token expires sometime between
         * the call to isValid and the token being used to authenticate an API call.
         */
        expirationTimeMillis = System.currentTimeMillis() + lifetimeSeconds * 1000 - EXPIRY_BUFFER_MILLIS;
    }

    public boolean isValid() {
        return System.currentTimeMillis() < expirationTimeMillis;
    }

    public String getToken() {
        if (!isValid()) {
            throw new IllegalStateException("Access token is no longer valid");
        }
        return token;
    }
    
}