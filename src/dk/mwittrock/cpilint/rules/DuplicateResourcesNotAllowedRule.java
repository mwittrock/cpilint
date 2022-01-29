package dk.mwittrock.cpilint.rules;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dk.mwittrock.cpilint.artifacts.ArtifactResource;
import dk.mwittrock.cpilint.artifacts.ArtifactResourceType;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.issues.DuplicateResourcesNotAllowedIssue;

final class DuplicateResourcesNotAllowedRule extends RuleBase {
	
	private static final String HASHING_ALGORITHM = "SHA-256";
	
	private final Set<ArtifactResourceType> typesToInspect;
	private final Map<ArtifactResourceType, Map<BigInteger, Set<ArtifactResource>>> resources = new HashMap<>();
	
	DuplicateResourcesNotAllowedRule() {
		// Look for duplicates of all supported resource types.
		this(Set.of(
			ArtifactResourceType.MESSAGE_MAPPING,
			ArtifactResourceType.XSLT_MAPPING,
			ArtifactResourceType.OPERATION_MAPPING,
			ArtifactResourceType.JAVASCRIPT_SCRIPT,
			ArtifactResourceType.GROOVY_SCRIPT,
			ArtifactResourceType.JAVA_ARCHIVE,
			ArtifactResourceType.EDMX,
			ArtifactResourceType.WSDL,
			ArtifactResourceType.XSD
		));
	}
	
	DuplicateResourcesNotAllowedRule(Set<ArtifactResourceType> typesToInspect) {
		// Only look for duplicates in the provided types of resources.
		this.typesToInspect = new HashSet<>(typesToInspect);
	}

	@Override
	public void inspect(IflowArtifact iflow) {
		for (ArtifactResourceType type : typesToInspect) {
			Map<BigInteger, Set<ArtifactResource>> digests = resources.getOrDefault(type, new HashMap<>());
			for (ArtifactResource resource : iflow.getResourcesByType(type)) {
				BigInteger digest = calculateDigest(resource);
				if (digests.containsKey(digest)) {
					digests.get(digest).add(resource);
				} else {
					/*
					 * Set.of(resource) returns an immutable set, and therefore
					 * cannot be used here. Too bad, really.
					 */
					Set<ArtifactResource> resourceSet = new HashSet<>();
					resourceSet.add(resource);
					digests.put(digest, resourceSet);
				}
			}
			resources.put(type, digests);
		}
	}

	@Override
	public void endTesting() {
		if (resources.isEmpty()) {
			// No iflows were inspected.
			return;
		}
		for (ArtifactResourceType type : typesToInspect) {
			assert resources.containsKey(type);
			Map<BigInteger, Set<ArtifactResource>> digests = resources.get(type);
			digests.values()
				.stream()
				.filter(s -> s.size() > 1)
				.map(s -> new DuplicateResourcesNotAllowedIssue(type, s))
				.forEach(consumer::consume);
		}
	}
	
	private static BigInteger calculateDigest(ArtifactResource resource) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(HASHING_ALGORITHM);
			md.update(resource.getContents().readAllBytes());
		} catch (NoSuchAlgorithmException e) {
			throw new RuleError(String.format("Unknown hashing algorithm '%s'", HASHING_ALGORITHM), e);
		} catch (IOException e) {
			throw new RuleError("I/O error reading artifact resource contents", e);
		}
		return new BigInteger(1, md.digest());
	}

}
