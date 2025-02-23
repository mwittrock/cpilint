package org.cpilint.issues;

import java.util.Optional;

import org.cpilint.artifacts.ArtifactResource;
import org.cpilint.artifacts.ArtifactResourceType;
import org.cpilint.artifacts.IflowArtifactTag;

public final class DisallowedJavaArchiveIssue extends IssueBase {
	
	private final String archiveName;
	
	public DisallowedJavaArchiveIssue(Optional<String> ruleId, IflowArtifactTag tag, String archiveName) {
		super(ruleId, tag, String.format(
			"The iflow artifact contains the Java archive '%s', which is not allowed.",
			archiveName));
		this.archiveName = archiveName;
	}

	public String getArchiveName() {
		return archiveName;
	}
	
	public static DisallowedJavaArchiveIssue fromResource(Optional<String> ruleId, ArtifactResource archiveResource) {
		if (archiveResource.getType() != ArtifactResourceType.JAVA_ARCHIVE) {
			throw new IllegalArgumentException("Provided resource is not a Java archive");
		}
		return new DisallowedJavaArchiveIssue(ruleId, archiveResource.getTag(), archiveResource.getName());
	}

}
