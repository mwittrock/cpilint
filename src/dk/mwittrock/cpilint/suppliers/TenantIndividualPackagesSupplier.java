package dk.mwittrock.cpilint.suppliers;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.mwittrock.cpilint.api.CloudIntegrationApi;
import dk.mwittrock.cpilint.api.CloudIntegrationApiError;

public final class TenantIndividualPackagesSupplier extends PackageSupplierBase {
	
	private static final Logger logger = LoggerFactory.getLogger(TenantIndividualPackagesSupplier.class);
	private boolean skipDrafts;
	private Set<String> packageIds;
	private Set<String> skipIflowArtifactIds;

	public TenantIndividualPackagesSupplier(CloudIntegrationApi api, boolean skipDrafts, Set<String> packageIds, Set<String> skipIflowArtifactIds) {
		super(api);
		this.skipDrafts = skipDrafts;
		Objects.requireNonNull(packageIds, "packageIds must not be null");
		if (packageIds.isEmpty()) {
			throw new IllegalArgumentException("No package IDs provided");
		}
		this.packageIds = new HashSet<>(packageIds);
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
		 * Make sure to proceed with only editable integration packages.
		 * Read-only packages will return iflow IDs, that cause HTTP status
		 * 400 when they are fetched later on. Nonexistent packages will cause
		 * HTTP 404 when trying to fetch their iflow artifacts.
		 */
		Set<String> editablePackageIds = new HashSet<>(packageIds);
		final boolean packagesFiltered;
		try {
			packagesFiltered = editablePackageIds.retainAll(api.getEditableIntegrationPackageIds(false));
		} catch (CloudIntegrationApiError e) {
			throw new IflowArtifactSupplierError("API error when fetching editable packages", e);
		}
		/*
		 * Log it if any packages were filtered out.
		 */
		if (packagesFiltered) {
			logger.debug("The following read-only or nonexistent packages were filtered out: {}",
				packageIds.stream().filter(p -> !editablePackageIds.contains(p)).collect(Collectors.joining(",")));
		}
		iflowArtifactIdIterator = iteratorFromPackages(editablePackageIds, skipDrafts, skipIflowArtifactIds);
	}

	@Override
	public void shutdown() {
		// No shutdown steps required.
	}

}
