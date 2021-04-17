package dk.mwittrock.cpilint.suppliers;

import java.util.Iterator;
import java.util.Objects;

import dk.mwittrock.cpilint.api.CloudIntegrationApi;
import dk.mwittrock.cpilint.api.CloudIntegrationApiError;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;

abstract class IteratingApiSupplierBase implements IflowArtifactSupplier {

	protected final CloudIntegrationApi api;
	protected Iterator<String> iflowArtifactIdIterator;
	private int artifactsSupplied = 0;
	
	protected IteratingApiSupplierBase(CloudIntegrationApi api) {
		this.api = Objects.requireNonNull(api, "api must not be null");
	}

	@Override
	public IflowArtifact supply() {
		if (!canSupply()) {
			throw new IllegalStateException("Cannot supply further iflow artifacts");
		}
		IflowArtifact iflow = null;
		try {
			iflow = api.getIflowArtifact(iflowArtifactIdIterator.next());
		} catch (CloudIntegrationApiError e) {
			throw new IflowArtifactSupplierError("API error when retrieving iflow", e);
		}
		artifactsSupplied++;
		return iflow;
	}
	
	@Override
	public boolean canSupply() {
		return iflowArtifactIdIterator.hasNext();
	}

	@Override
	public int artifactsSupplied() {
		return artifactsSupplied;
	}
	
}
