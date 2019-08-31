package dk.mwittrock.cpilint.issues;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;

abstract class ChannelIssueBase extends ArtifactIssueBase {
	
	private final String channelName;
	private final String channelId;
	
	protected ChannelIssueBase(IflowArtifactTag tag, String channelName, String channelId, String message) {
		super(tag, message);
		this.channelName = channelName;
		this.channelId = channelId;
	}

	public String getChannelName() {
		return channelName;
	}
	
	public String getChannelId() {
		return channelId;
	}

}
