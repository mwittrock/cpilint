package dk.mwittrock.cpilint.issues;

import dk.mwittrock.cpilint.artifacts.ArtifactResource;
import dk.mwittrock.cpilint.artifacts.ArtifactResourceType;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;

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
