package dk.mwittrock.cpilint.consumers;

import dk.mwittrock.cpilint.CpiLintError;

@SuppressWarnings("serial")
public final class IssueConsumerError extends CpiLintError {
	
	public IssueConsumerError(String message) {
		super(message);
	}
	
	public IssueConsumerError(String message, Throwable cause) {
		super(message, cause);
	}

}
