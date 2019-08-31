package dk.mwittrock.cpilint.issues;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.model.ReceiverAdapter;

public final class CleartextBasicAuthNotAllowedIssue extends ReceiverChannelIssueBase {
	
	public CleartextBasicAuthNotAllowedIssue(IflowArtifactTag tag, ReceiverAdapter receiverAdapter, String channelName, String channelId) {
		super(tag, receiverAdapter, channelName, channelId, String.format(
			"Receiver channel '%s' (ID '%s') of type %s is configured with basic authentication and an unencrypted endpoint, which is not allowed.",
			channelName,
			channelId,
			receiverAdapter.getName()));
	}
	
}
