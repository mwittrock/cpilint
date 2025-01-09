package org.cpilint.rules;

import org.cpilint.CpiLintError;

@SuppressWarnings("serial")
public final class RuleFactoryError extends CpiLintError {
	
	public RuleFactoryError(String message) {
		super(message);
	}
	
	public RuleFactoryError(String message, Throwable cause) {
		super(message, cause);
	}

}
