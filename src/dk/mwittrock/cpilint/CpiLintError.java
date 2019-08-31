package dk.mwittrock.cpilint;

@SuppressWarnings("serial")
public class CpiLintError extends Error {
	
	public CpiLintError(String message) {
		super(message);
	}
	
	public CpiLintError(String message, Throwable cause) {
		super(message, cause);
	}

}
