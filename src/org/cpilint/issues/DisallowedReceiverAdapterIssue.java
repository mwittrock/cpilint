package org.cpilint.issues;

import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.model.ReceiverAdapter;

public final class DisallowedReceiverAdapterIssue extends ReceiverChannelIssueBase {
	
	public DisallowedReceiverAdapterIssue(IflowArtifactTag tag, ReceiverAdapter receiverAdapter, String channelName, String channelId) {
		super(tag, receiverAdapter, channelName, channelId, String.format(
			"The adapter type of receiver channel '%s' (ID '%s') is %s, which is not allowed.", 
			channelName, 
			channelId,
			receiverAdapter.getName()));
	}
	
}
