package org.cpilint.consumers;

import org.cpilint.CpiLintError;

@SuppressWarnings("serial")
public final class IssueConsumerError extends CpiLintError {
	
	public IssueConsumerError(String message) {
		super(message);
	}
	
	public IssueConsumerError(String message, Throwable cause) {
		super(message, cause);
	}

}
