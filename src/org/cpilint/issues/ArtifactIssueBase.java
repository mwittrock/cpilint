package org.cpilint.issues;

import org.cpilint.artifacts.IflowArtifactTag;

abstract class ArtifactIssueBase extends IssueBase {
	
	private final IflowArtifactTag tag;
	
	protected ArtifactIssueBase(IflowArtifactTag tag, String message) {
		super(String.format(
	    	"In iflow '%s' (ID '%s'): %s",
	    	tag.getName(),
	    	tag.getId(),
	    	message));
		this.tag = tag;
	}
	
	public IflowArtifactTag getTag() {
		return tag;
	}

}
