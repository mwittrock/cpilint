package dk.mwittrock.cpilint.issues;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.model.ReceiverAdapter;

public final class MatchingProcessDirectChannelsRequiredIssue extends ReceiverChannelIssueBase {
	
	private String address;

	public MatchingProcessDirectChannelsRequiredIssue(IflowArtifactTag tag, String channelName, String channelId, String address) {
		super(tag, ReceiverAdapter.PROCESSDIRECT, channelName, channelId, String.format(
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
