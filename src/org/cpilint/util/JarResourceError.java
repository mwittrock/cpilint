package org.cpilint.util;

import org.cpilint.CpiLintError;

@SuppressWarnings("serial")
public final class JarResourceError extends CpiLintError {
	
	public JarResourceError(String message) {
		super(message);
	}
	
	public JarResourceError(String message, Throwable cause) {
		super(message, cause);
	}

}
