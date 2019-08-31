package dk.mwittrock.cpilint.artifacts;

import dk.mwittrock.cpilint.CpiLintError;

@SuppressWarnings("serial")
public final class IflowArtifactError extends CpiLintError {
	
	public IflowArtifactError(String message) {
		super(message);
	}
	
	public IflowArtifactError(String message, Throwable cause) {
		super(message, cause);
	}

}
