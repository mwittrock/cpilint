package org.example.cpilint.extensions;

import java.util.Optional;
import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.issues.IssueBase;

public final class IflowNameIssue extends IssueBase {

    public IflowNameIssue(Optional<String> ruleId, IflowArtifactTag tag, String expectedName, String actualName) {
        super(ruleId, tag, issueMessage(expectedName, actualName));
    }

    private static String issueMessage(String expectedName, String actualName) {
        return "Expected iflow name '%s' but actual name is '%s'".formatted(expectedName, actualName);
    }

}
