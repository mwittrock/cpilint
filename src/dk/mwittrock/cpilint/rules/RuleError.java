package dk.mwittrock.cpilint.rules;

import dk.mwittrock.cpilint.CpiLintError;

@SuppressWarnings("serial")
public final class RuleError extends CpiLintError {
	
	public RuleError(String message) {
		super(message);
	}
	
	public RuleError(String message, Throwable cause) {
		super(message, cause);
	}

}
