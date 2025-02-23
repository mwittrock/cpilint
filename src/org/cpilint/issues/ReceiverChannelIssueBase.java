package org.cpilint.issues;

import java.util.Optional;

import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.model.ReceiverAdapter;

abstract class ReceiverChannelIssueBase extends ChannelIssueBase {
	
	private ReceiverAdapter receiverAdapter;
	
	protected ReceiverChannelIssueBase(Optional<String> ruleId, IflowArtifactTag tag, ReceiverAdapter receiverAdapter, String channelName, String channelId, String message) {
		super(ruleId, tag, channelName, channelId, message);
		this.receiverAdapter = receiverAdapter;
	}
	
	public ReceiverAdapter getReceiverAdapter() {
		return receiverAdapter;
	}

}
