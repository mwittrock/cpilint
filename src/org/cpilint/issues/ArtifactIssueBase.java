package org.cpilint.issues;

import org.cpilint.artifacts.IflowArtifactTag;

abstract class ArtifactIssueBase extends IssueBase {
	
	private final IflowArtifactTag tag;
	
	protected ArtifactIssueBase(IflowArtifactTag tag, String message) {
		super(String.format(
			"In iflow '%s' (ID '%s')%s: %s",
	    	tag.getName(),
	    	tag.getId(),
			tag.getPackageInfo().isPresent() ? " of package '%s' (ID '%s')".formatted(tag.getPackageInfo().get().name(), tag.getPackageInfo().get().id()) : "",
	    	message));
		this.tag = tag;
	}
	
	public IflowArtifactTag getTag() {
		return tag;
	}

}
