package dk.mwittrock.cpilint.issues;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.model.SenderAdapter;

abstract class SenderChannelIssueBase extends ChannelIssueBase {
	
	private SenderAdapter senderAdapter;
	
	protected SenderChannelIssueBase(IflowArtifactTag tag, SenderAdapter senderAdapter, String channelName, String channelId, String message) {
		super(tag, channelName, channelId, message);
		this.senderAdapter = senderAdapter;
	}
	
	public SenderAdapter getSenderAdapter() {
		return senderAdapter;
	}

}
