package dk.mwittrock.cpilint.auth;

import dk.mwittrock.cpilint.CpiLintError;

@SuppressWarnings("serial")
public final class AuthorizationServerError extends CpiLintError {
	
	public AuthorizationServerError(String message) {
		super(message);
	}
	
	public AuthorizationServerError(String message, Throwable cause) {
		super(message, cause);
	}

}
