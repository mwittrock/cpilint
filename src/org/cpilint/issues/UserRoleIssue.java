package org.cpilint.issues;

import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.model.SenderAdapter;

public final class UserRoleIssue extends SenderChannelIssueBase {

    private final String userRole;

    public UserRoleIssue(IflowArtifactTag tag, SenderAdapter senderAdapter, String channelName, String channelId, String userRole) {
        super(tag, senderAdapter, channelName, channelId, String.format(
            "User role '%s' not allowed in %s sender channel '%s' (ID '%s')",
            userRole,
            senderAdapter.getName(),
            channelName,
            channelId));
        this.userRole = userRole;
    }

    public String getUserRole() {
        return userRole;
    }

}