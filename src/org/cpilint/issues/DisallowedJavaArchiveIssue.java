package org.cpilint.issues;

import org.cpilint.artifacts.ArtifactResource;
import org.cpilint.artifacts.ArtifactResourceType;
import org.cpilint.artifacts.IflowArtifactTag;

public final class DisallowedJavaArchiveIssue extends ArtifactIssueBase {
	
	private final String archiveName;
	
	public DisallowedJavaArchiveIssue(IflowArtifactTag tag, String archiveName) {
		super(tag, String.format(
			"The iflow artifact contains the Java archive '%s', which is not allowed.",
			archiveName));
		this.archiveName = archiveName;
	}

	public String getArchiveName() {
		return archiveName;
	}
	
	public static DisallowedJavaArchiveIssue fromResource(ArtifactResource archiveResource) {
		if (archiveResource.getType() != ArtifactResourceType.JAVA_ARCHIVE) {
			throw new IllegalArgumentException("Provided resource is not a Java archive");
		}
		return new DisallowedJavaArchiveIssue(archiveResource.getTag(), archiveResource.getName());
	}

}
