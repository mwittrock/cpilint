package dk.mwittrock.cpilint.suppliers;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import dk.mwittrock.cpilint.api.CloudIntegrationApi;

public final class TenantSingleArtifactsSupplier extends IteratingApiSupplierBase {

	@Override
	public void setup() {
		// No setup steps needed.
	}
	
	public TenantSingleArtifactsSupplier(CloudIntegrationApi api, Set<String> iflowArtifactIds) {
		super(api);
		Objects.requireNonNull(iflowArtifactIds, "iflowArtifactIds must not be null");
		if (iflowArtifactIds.isEmpty()) {
			throw new IllegalArgumentException("No iflow artifact IDs provided");
		}
		iflowArtifactIdIterator = new HashSet<>(iflowArtifactIds).iterator();
	}
	
	@Override
	public void shutdown() {
		// No shutdown steps needed either.
	}

}
