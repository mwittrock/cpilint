package org.cpilint.issues;

import java.util.Optional;

import org.cpilint.artifacts.IflowArtifactTag;

abstract class ChannelIssueBase extends IssueBase {
	
	private final String channelName;
	private final String channelId;
	
	protected ChannelIssueBase(Optional<String> ruleId, IflowArtifactTag tag, String channelName, String channelId, String message) {
		super(ruleId, tag, message);
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
