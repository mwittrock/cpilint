package org.cpilint.issues;

import java.util.Optional;

import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.model.SenderAdapter;

public final class CsrfProtectionRequiredIssue extends SenderChannelIssueBase {

	public CsrfProtectionRequiredIssue(Optional<String> ruleId, IflowArtifactTag tag, String channelName, String channelId) {
		super(ruleId, tag, SenderAdapter.HTTPS, channelName, channelId, String.format(
			"Sender HTTPS channel '%s' (ID '%s') does not employ CSRF protection, which is required.", 
			channelName,
			channelId));
	}

}
