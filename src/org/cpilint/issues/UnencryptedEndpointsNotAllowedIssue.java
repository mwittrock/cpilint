package org.cpilint.issues;

import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.model.ReceiverAdapter;

public final class UnencryptedEndpointsNotAllowedIssue extends ReceiverChannelIssueBase {
	
	public UnencryptedEndpointsNotAllowedIssue(IflowArtifactTag tag, ReceiverAdapter receiverAdapter, String channelName, String channelId) {
		super(tag, receiverAdapter, channelName, channelId, String.format(
			"Receiver channel '%s' (ID '%s') of type %s is configured with an unencrypted endpoint, which is not allowed.",
			channelName,
			channelId,
			receiverAdapter.getName()));
	}
	
}
