package org.cpilint.issues;

import java.util.Optional;

import org.cpilint.artifacts.IflowArtifactTag;

public final class MultiConditionTypeRoutersNotAllowedIssue extends StepIssueBase {
	
	public MultiConditionTypeRoutersNotAllowedIssue(Optional<String> ruleId, IflowArtifactTag tag, String stepName, String stepId) {
		super(ruleId, tag, stepName, stepId, String.format(
			"Router step '%s' (ID '%s') contains both XML and non-XML conditions, which is not allowed.",
			stepName,
			stepId));
	}

}
