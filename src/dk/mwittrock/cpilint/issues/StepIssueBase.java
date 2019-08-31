package dk.mwittrock.cpilint.issues;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;

abstract class StepIssueBase extends ArtifactIssueBase {
	
	private final String stepName;
	private final String stepId;
	
	protected StepIssueBase(IflowArtifactTag tag, String stepName, String stepId, String message) {
		super(tag, message);
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
