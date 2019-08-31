package dk.mwittrock.cpilint.suppliers;

import dk.mwittrock.cpilint.CpiLintError;

@SuppressWarnings("serial")
public final class IflowArtifactSupplierError extends CpiLintError {
	
	public IflowArtifactSupplierError(String message) {
		super(message);
	}
	
	public IflowArtifactSupplierError(String message, Throwable cause) {
		super(message, cause);
	}

}
