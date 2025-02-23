package org.cpilint.issues;

import java.util.Optional;

import org.cpilint.artifacts.IflowArtifactTag;

public final class NamingConventionsRuleIssue extends IssueBase {
	
	private String actualName;
	
	public NamingConventionsRuleIssue(Optional<String> ruleId, IflowArtifactTag tag, String message, String actualName) {
		super(ruleId, tag, message);
		this.actualName = actualName;
	}
	
	public String getActualName() {
		return actualName;
	}

}
