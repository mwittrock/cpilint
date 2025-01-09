package org.cpilint.api;

import org.cpilint.CpiLintError;

@SuppressWarnings("serial")
public final class CloudIntegrationApiError extends CpiLintError {

	public CloudIntegrationApiError(String message) {
		super(message);
	}
	
	public CloudIntegrationApiError(String message, Throwable cause) {
		super(message, cause);
	}
	
}