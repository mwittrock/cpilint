package dk.mwittrock.cpilint.issues;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.model.SenderAdapter;

public final class DisallowedSenderAdapterIssue extends ChannelIssueBase {
	
	private final SenderAdapter senderAdapter;
	
	public DisallowedSenderAdapterIssue(IflowArtifactTag tag, String channelName, String channelId, SenderAdapter senderAdapter) {
		super(tag, channelName, channelId, String.format(
			"The adapter type of sender channel '%s' (ID '%s') is %s, which is not allowed.", 
			channelName, 
			channelId,
			senderAdapter.getName()));
		this.senderAdapter = senderAdapter;
	}
	
	public SenderAdapter getSenderAdapter() {
		return senderAdapter;
	}

}
