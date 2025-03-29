package org.cpilint;

@SuppressWarnings("serial")
public final class VersionCheckError extends CpiLintError {
	
	public VersionCheckError(String message) {
		super(message);
	}
	
	public VersionCheckError(String message, Throwable cause) {
		super(message, cause);
	}

}
