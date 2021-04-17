package dk.mwittrock.cpilint.suppliers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.mwittrock.cpilint.api.CloudIntegrationApi;
import dk.mwittrock.cpilint.api.CloudIntegrationApiError;

abstract class PackageSupplierBase extends IteratingApiSupplierBase {
	
	private static final Logger logger = LoggerFactory.getLogger(PackageSupplierBase.class);
	 
	protected PackageSupplierBase(CloudIntegrationApi api) {
		super(api);
	}
	 
	protected Iterator<String> iteratorFromPackages(Set<String> packageIds, boolean skipDrafts, Set<String> skipIflowIds) {
		assert packageIds != null;
		assert skipIflowIds != null;
		/*
		 * Fetch all iflow artifact IDs from the provided packages
		 * (possibly skipping drafts).
		 */
		Set<String> iflowIds = new HashSet<>();
		try {
			for (String packageId : packageIds) {
				iflowIds.addAll(api.getIflowArtifactIdsFromPackage(packageId, skipDrafts));
			}
		} catch (CloudIntegrationApiError e) {
			throw new IflowArtifactSupplierError("API error when fetching iflow IDs", e);
		}
		/*
		 * Filter out all iflow IDs, that are to be skipped (if any).
		 */ 
		Set<String> filteredIflowIds = iflowIds
			.stream()
			.filter(i -> !skipIflowIds.contains(i))
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
			logger.info("Please note that there are no iflows left to iterate");
		}
		return filteredIflowIds.iterator();
	}

}