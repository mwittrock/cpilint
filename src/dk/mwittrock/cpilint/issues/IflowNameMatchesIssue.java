package dk.mwittrock.cpilint.issues;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;

public final class IflowNameMatchesIssue extends ArtifactIssueBase {

	public IflowNameMatchesIssue(IflowArtifactTag tag, String patternString) {
		super(tag, patternString.format("Iflow name does not match the required pattern (%s) for Iflow names.", patternString));
	}

}
