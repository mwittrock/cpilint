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
	private Set<String> skipPackageIds;
	
	public TenantAllArtifactsSupplier(CloudIntegrationApi api, boolean skipSapPackages, boolean skipDrafts, Set<String> skipIflowArtifactIds, Set<String> skipPackageIds) {
		super(api);
		this.skipSapPackages = skipSapPackages;
		this.skipDrafts = skipDrafts;
		/*
		 * It's okay for skipIflowArtifactIds to reference an empty Set, but
		 * it must not be null.
		 */
		Objects.requireNonNull(skipIflowArtifactIds, "skipIflowArtifactIds must not be null");
		this.skipIflowArtifactIds = new HashSet<>(skipIflowArtifactIds);
		/*
		 * It's okay for skipPackageIds to reference an empty Set, but
		 * it must not be null.
		 */
		Objects.requireNonNull(skipPackageIds, "skipPackageIds must not be null");
		this.skipPackageIds = new HashSet<>(skipPackageIds);
	}
	
	@Override
	public void setup() {
		/*
		 * Fetch all packages from the tenant (possibly skipping SAP packages)
		 * and filter out all packages, that are to be skipped (if any).
		 */
		Set<String> packageIds;
		try {
			packageIds = api.getIntegrationPackageIds(skipSapPackages);
		} catch (CloudIntegrationApiError e) {
			throw new IflowArtifactSupplierError("API error when fetching package IDs", e);
		}
		Set<String> filteredPackageIds = packageIds
			.stream()
			.filter(p -> !skipPackageIds.contains(p))
			.collect(Collectors.toSet());
		/*
		 * Log the packages that were skipped (if any).
		 */
		if (filteredPackageIds.size() < packageIds.size()) {
			logger.debug("The following package IDs were skipped: {}",
				packageIds.stream().filter(p -> !filteredPackageIds.contains(p)).collect(Collectors.joining(",")));
		}
		/*
		 * Now fetch all iflow artifact IDs from all remaining packages
		 * (possibly skipping drafts), and filter out all iflow artifact
		 * IDs, that are to be skipped (if any). 
		 */
		Set<String> iflowIds = new HashSet<>();
		try {
			for (String packageId : filteredPackageIds) {
				iflowIds.addAll(api.getIflowArtifactIdsFromPackage(packageId, skipDrafts));
			}
		} catch (CloudIntegrationApiError e) {
			throw new IflowArtifactSupplierError("API error when fetching iflow IDs", e);
		}
		Set<String> filteredIflowIds = iflowIds
			.stream()
			.filter(i -> !skipIflowArtifactIds.contains(i))
			.collect(Collectors.toSet());
		/*
		 * Log the iflows that were skipped (if any).
		 */
		if (filteredIflowIds.size() < iflowIds.size()) {
			logger.debug("The following iflow IDs were skipped: {}",
				iflowIds.stream().filter(i -> !filteredIflowIds.contains(i)).collect(Collectors.joining(",")));
		}
		/*
		 * If there are no iflows left at this point, log that fact.
		 */
		if (filteredIflowIds.isEmpty()) {
			logger.info("Please note that after filtering, there are no iflows left");
		}
		iflowArtifactIdIterator = filteredIflowIds.iterator();
	}
	
	@Override
	public void shutdown() {
		// No shutdown steps needed.
	}

}
