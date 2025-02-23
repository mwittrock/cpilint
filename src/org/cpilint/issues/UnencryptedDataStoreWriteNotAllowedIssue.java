package org.cpilint.issues;

import java.util.Optional;

import org.cpilint.artifacts.IflowArtifactTag;

public final class UnencryptedDataStoreWriteNotAllowedIssue extends StepIssueBase {

	public UnencryptedDataStoreWriteNotAllowedIssue(Optional<String> ruleId, IflowArtifactTag tag, String stepName, String stepId) {
		super(ruleId, tag, stepName, stepId, String.format(
			"Data store step '%s' (ID '%s') performs an unencrypted write, which is not allowed.",
			stepName,
			stepId));
	}
	
}
