package org.cpilint.issues;

import java.util.Optional;

import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.model.ReceiverAdapter;

public final class CleartextBasicAuthNotAllowedIssue extends ReceiverChannelIssueBase {
	
	public CleartextBasicAuthNotAllowedIssue(Optional<String> ruleId, IflowArtifactTag tag, ReceiverAdapter receiverAdapter, String channelName, String channelId) {
		super(ruleId, tag, receiverAdapter, channelName, channelId, String.format(
			"Receiver channel '%s' (ID '%s') of type %s is configured with basic authentication and an unencrypted endpoint, which is not allowed.",
			channelName,
			channelId,
			receiverAdapter.getName()));
	}
	
}
