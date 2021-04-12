package dk.mwittrock.cpilint.suppliers;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.mwittrock.cpilint.api.CloudIntegrationApi;
import dk.mwittrock.cpilint.api.CloudIntegrationApiError;

public final class TenantAllArtifactsSupplier extends IteratingApiSupplierBase {
	
	private static final Logger logger = LoggerFactory.getLogger(TenantAllArtifactsSupplier.class);
	private boolean skipSapPackages;
	private boolean skipDrafts;
	private Set<String> skipIflowArtifactIds;
	
	public TenantAllArtifactsSupplier(CloudIntegrationApi api, boolean skipSapPackages, boolean skipDrafts, Set<String> skipIflowArtifactIds) {
		super(api);
		this.skipSapPackages = skipSapPackages;
		this.skipDrafts = skipDrafts;
		/*
		 * It's okay for skipIflowArtifactIds to reference an empty Set, but
		 * it must not be null.
		 */
		Objects.requireNonNull(skipIflowArtifactIds, "skipIflowArtifactIds must not be null");
		this.skipIflowArtifactIds = new HashSet<>(skipIflowArtifactIds);
	}
	
	@Override
	public void setup() {
		/*
		 * Fetch all iflow artifact IDs from the tenant by first fetching
		 * all packages (possibly skipping SAP packages) and then fetching
		 * all iflow artifacts from every package (possibly skipping drafts).
		 */
		Set<String> iflowArtifactIds = new HashSet<>();
		try {
			for (String packageId : api.getIntegrationPackageIds(skipSapPackages)) {
				iflowArtifactIds.addAll(api.getIflowArtifactIdsFromPackage(packageId, skipDrafts));
			}
		} catch (CloudIntegrationApiError e) {
			throw new IflowArtifactSupplierError("API error", e);
		}
		/*
		 * Now filter out all iflow artifact IDs that are to be skipped (if any).
		 */
		Set<String> filteredIds = iflowArtifactIds
			.stream()
			.filter(i -> !skipIflowArtifactIds.contains(i))
			.collect(Collectors.toSet());
		/*
		 * If iflow artifact IDs were actually skipped, the log should reflect that.
		 */
		if (filteredIds.size() < iflowArtifactIds.size()) {
			logger.debug("The following iflow artifact IDs were skipped: {}",
				iflowArtifactIds.stream().filter(i -> !filteredIds.contains(i)).collect(Collectors.joining(",")));
		}
		iflowArtifactIdIterator = filteredIds.iterator();
	}
	
	@Override
	public void shutdown() {
		// No shutdown steps needed.
	}

}
