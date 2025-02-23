package org.cpilint.issues;

import java.util.Optional;

import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.model.SenderAdapter;

public final class UserRoleIssue extends SenderChannelIssueBase {

    private final String userRole;

    public UserRoleIssue(Optional<String> ruleId, IflowArtifactTag tag, SenderAdapter senderAdapter, String channelName, String channelId, String userRole) {
        super(ruleId, tag, senderAdapter, channelName, channelId, String.format(
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