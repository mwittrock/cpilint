package dk.mwittrock.cpilint.issues;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.model.SenderAdapter;

public final class CsrfProtectionRequiredIssue extends SenderChannelIssueBase {

	public CsrfProtectionRequiredIssue(IflowArtifactTag tag, String channelName, String channelId) {
		super(tag, SenderAdapter.HTTPS, channelName, channelId, String.format(
			"Sender HTTPS channel '%s' (ID '%s') does not employ CSRF protection, which is required.", 
			channelName,
			channelId));
	}

}
