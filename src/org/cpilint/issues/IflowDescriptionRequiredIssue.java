package org.cpilint.issues;

import java.util.Optional;

import org.cpilint.artifacts.IflowArtifactTag;

public final class IflowDescriptionRequiredIssue extends IssueBase {
	
	public IflowDescriptionRequiredIssue(Optional<String> ruleId, IflowArtifactTag tag) {
		super(ruleId, tag, "Iflow does not have the required description.");
	}

}
