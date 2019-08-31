package dk.mwittrock.cpilint.issues;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.model.SenderAdapter;

public final class ClientCertSenderChannelAuthNotAllowedIssue extends SenderChannelIssueBase {

	public ClientCertSenderChannelAuthNotAllowedIssue(IflowArtifactTag tag, SenderAdapter senderAdapter, String channelName, String channelId) {
		super(tag, senderAdapter, channelName, channelId, String.format(
			"Sender channel '%s' (ID '%s') of type %s employs client certificate authentication, which is not allowed.",
			channelName,
			channelId,
			senderAdapter.getName()));
	}
	
}
