package dk.mwittrock.cpilint.issues;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;

public final class UnencryptedDataStoreWriteNotAllowedIssue extends StepIssueBase {

	public UnencryptedDataStoreWriteNotAllowedIssue(IflowArtifactTag tag, String stepName, String stepId) {
		super(tag, stepName, stepId, String.format(
			"Data store step '%s' (ID '%s') performs an unencrypted write, which is not allowed.",
			stepName,
			stepId));
	}
	
}
