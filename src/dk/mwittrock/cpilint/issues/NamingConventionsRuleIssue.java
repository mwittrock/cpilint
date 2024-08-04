package dk.mwittrock.cpilint.issues;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;

public final class NamingConventionsRuleIssue extends ArtifactIssueBase {
	
	private String actualName;
	
	public NamingConventionsRuleIssue(IflowArtifactTag tag, String message, String actualName) {
		super(tag, message);
		this.actualName = actualName;
	}
	
	public String getActualName() {
		return actualName;
	}

}
