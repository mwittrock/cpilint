package org.cpilint.issues;

import org.cpilint.artifacts.IflowArtifactTag;

public final class IflowDescriptionRequiredIssue extends ArtifactIssueBase {
	
	public IflowDescriptionRequiredIssue(IflowArtifactTag tag) {
		super(tag, "Iflow does not have the required description.");
	}

}
