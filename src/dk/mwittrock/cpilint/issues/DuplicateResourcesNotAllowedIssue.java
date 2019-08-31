package dk.mwittrock.cpilint.issues;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import dk.mwittrock.cpilint.artifacts.ArtifactResource;
import dk.mwittrock.cpilint.artifacts.ArtifactResourceType;

public final class DuplicateResourcesNotAllowedIssue extends IssueBase {
	
	private final ArtifactResourceType type;
	private final Set<ArtifactResource> duplicateResources;
	
	public DuplicateResourcesNotAllowedIssue(ArtifactResourceType type, Set<ArtifactResource> duplicateResources) {
		super(buildMessage(type, duplicateResources));
		this.type = type;
		this.duplicateResources = duplicateResources;
	}
	
	public ArtifactResourceType getType() {
		return type;
	}
	
	public Set<ArtifactResource> getDuplicateResources() {
		return Collections.unmodifiableSet(duplicateResources);
	}
	
	private static String buildMessage(ArtifactResourceType type, Set<ArtifactResource> duplicateResources) {
		assert !duplicateResources.isEmpty();
		StringBuilder sb = new StringBuilder();
		sb.append("The following ");
		sb.append(type.getName());
		sb.append(" resources are duplicates, which is not allowed: ");
		sb.append(IssuesUtil.capitalizeFirst(duplicateResources
			.stream()
			.map(r -> String.format("resource '%s' in iflow '%s' (ID '%s')", r.getName(), r.getTag().getName(), r.getTag().getId()))
			.collect(Collectors.joining(", "))));		
		return sb.toString();
	}
	
}
