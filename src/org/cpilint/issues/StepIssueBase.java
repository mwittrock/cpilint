package org.cpilint.issues;

import java.util.Optional;

import org.cpilint.artifacts.IflowArtifactTag;

abstract class StepIssueBase extends IssueBase {
	
	private final String stepName;
	private final String stepId;
	
	protected StepIssueBase(Optional<String> ruleId, IflowArtifactTag tag, String stepName, String stepId, String message) {
		super(ruleId, tag, message);
		this.stepName = stepName;
		this.stepId = stepId;
	}

	public String getStepName() {
		return stepName;
	}
	
	public String getStepId() {
		return stepId;
	}

}
