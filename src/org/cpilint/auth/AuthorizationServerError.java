package org.cpilint.auth;

import org.cpilint.CpiLintError;

@SuppressWarnings("serial")
public final class AuthorizationServerError extends CpiLintError {
	
	public AuthorizationServerError(String message) {
		super(message);
	}
	
	public AuthorizationServerError(String message, Throwable cause) {
		super(message, cause);
	}

}
