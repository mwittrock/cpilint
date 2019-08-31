package dk.mwittrock.cpilint.issues;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;

public final class MultiConditionTypeRoutersNotAllowedIssue extends StepIssueBase {
	
	public MultiConditionTypeRoutersNotAllowedIssue(IflowArtifactTag tag, String stepName, String stepId) {
		super(tag, stepName, stepId, String.format(
			"Router step '%s' (ID '%s') contains both XML and non-XML conditions, which is not allowed.",
			stepName,
			stepId));
	}

}
