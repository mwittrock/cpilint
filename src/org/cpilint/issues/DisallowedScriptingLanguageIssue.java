package org.cpilint.issues;

import java.util.Optional;

import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.model.ScriptingLanguage;

public final class DisallowedScriptingLanguageIssue extends StepIssueBase {
	
	private final ScriptingLanguage language;
	
	public DisallowedScriptingLanguageIssue(Optional<String> ruleId, IflowArtifactTag tag, String stepName, String stepId, ScriptingLanguage language) {
		super(ruleId, tag, stepName, stepId, String.format(
			"Script step '%s' (ID '%s') executes a %s script, which is not allowed.", 
			stepName, 
			stepId,
			language.toString()));
		this.language = language;
	}
	
	public ScriptingLanguage getScriptingLanguage() {
		return language;
	}

}
