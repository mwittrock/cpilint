package org.cpilint.issues;

import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.model.SenderAdapter;

public final class CsrfProtectionRequiredIssue extends SenderChannelIssueBase {

	public CsrfProtectionRequiredIssue(IflowArtifactTag tag, String channelName, String channelId) {
		super(tag, SenderAdapter.HTTPS, channelName, channelId, String.format(
			"Sender HTTPS channel '%s' (ID '%s') does not employ CSRF protection, which is required.", 
			channelName,
			channelId));
	}

}
