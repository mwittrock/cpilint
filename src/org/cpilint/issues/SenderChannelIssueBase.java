package org.cpilint.issues;

import java.util.Optional;

import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.model.SenderAdapter;

abstract class SenderChannelIssueBase extends ChannelIssueBase {
	
	private SenderAdapter senderAdapter;
	
	protected SenderChannelIssueBase(Optional<String> ruleId, IflowArtifactTag tag, SenderAdapter senderAdapter, String channelName, String channelId, String message) {
		super(ruleId, tag, channelName, channelId, message);
		this.senderAdapter = senderAdapter;
	}
	
	public SenderAdapter getSenderAdapter() {
		return senderAdapter;
	}

}
