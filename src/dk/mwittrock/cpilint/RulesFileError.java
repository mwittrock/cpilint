package dk.mwittrock.cpilint;

@SuppressWarnings("serial")
public final class RulesFileError extends CpiLintError {
	
	public RulesFileError(String message) {
		super(message);
	}
	
	public RulesFileError(String message, Throwable cause) {
		super(message, cause);
	}

}
