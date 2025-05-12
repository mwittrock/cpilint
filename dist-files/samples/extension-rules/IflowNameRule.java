package org.example.cpilint.extensions;

import java.util.Objects;

import org.cpilint.artifacts.IflowArtifact;
import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.rules.RuleBase;

public final class IflowNameRule extends RuleBase {

    private final String expectedIflowName;

    public IflowNameRule(String expectedIflowName) {
        this.expectedIflowName = Objects.requireNonNull(expectedIflowName, "expectedIflowName must not be null");
    }

    @Override
    public void inspect(IflowArtifact iflow) {
        assert iflow != null;
        IflowArtifactTag tag = iflow.getTag();
        String actualIflowName = tag.getName();
        if (!expectedIflowName.equals(actualIflowName)) {
            consumer.consume(new IflowNameIssue(ruleId, tag, expectedIflowName, actualIflowName));
        }
    }

}