package org.cpilint.issues;

import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.model.ReceiverAdapter;

abstract class ReceiverChannelIssueBase extends ChannelIssueBase {
	
	private ReceiverAdapter receiverAdapter;
	
	protected ReceiverChannelIssueBase(IflowArtifactTag tag, ReceiverAdapter receiverAdapter, String channelName, String channelId, String message) {
		super(tag, channelName, channelId, message);
		this.receiverAdapter = receiverAdapter;
	}
	
	public ReceiverAdapter getReceiverAdapter() {
		return receiverAdapter;
	}

}
