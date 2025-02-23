package org.cpilint.issues;

import java.util.Optional;

import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.model.ReceiverAdapter;

public final class MatchingProcessDirectChannelsRequiredIssue extends ReceiverChannelIssueBase {
	
	private String address;

	public MatchingProcessDirectChannelsRequiredIssue(Optional<String> ruleId, IflowArtifactTag tag, String channelName, String channelId, String address) {
		super(ruleId, tag, ReceiverAdapter.PROCESSDIRECT, channelName, channelId, String.format(
			"No matching sender channel address ('%s') for ProcessDirect receiver channel '%s' (ID '%s').",
			address,
			channelName,
			channelId));
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}

}
