package dk.mwittrock.cpilint.util;

import dk.mwittrock.cpilint.CpiLintError;

@SuppressWarnings("serial")
public final class JarResourceError extends CpiLintError {
	
	public JarResourceError(String message) {
		super(message);
	}
	
	public JarResourceError(String message, Throwable cause) {
		super(message, cause);
	}

}
