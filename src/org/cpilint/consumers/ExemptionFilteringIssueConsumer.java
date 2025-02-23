package org.cpilint.consumers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.cpilint.Exemption;
import org.cpilint.issues.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExemptionFilteringIssueConsumer implements IssueConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ExemptionFilteringIssueConsumer.class);
    private final Map<String, Set<String>> ruleIdToExemptedIflows;
    private final IssueConsumer nextConsumer;

    public ExemptionFilteringIssueConsumer(Set<Exemption> exemptions, IssueConsumer nextConsumer) {
        this.nextConsumer = Objects.requireNonNull(nextConsumer, "nextConsumer must not be null");
        /*
         * The exemption set must not be null and must not be empty (if there are no
         * exemptions, use a different IssueConsumer).
         */
        Objects.requireNonNull(exemptions, "exemptions must not be null");
        if (exemptions.isEmpty()) {
            throw new IllegalArgumentException("The exemption set must not be empty");
        }
        // Store the exemptions in a more convenient data structure.
        ruleIdToExemptedIflows = new HashMap<>();
        for (Exemption e : exemptions) {
            if (!ruleIdToExemptedIflows.containsKey(e.ruleId())) {
                ruleIdToExemptedIflows.put(e.ruleId(), new HashSet<>());
            }
            ruleIdToExemptedIflows.get(e.ruleId()).add(e.iflowId());
        }
    }

    @Override
    public void consume(Issue issue) {
        /*
         * An issue is exempt if all the iflows involved are exempt from the
         * specific rule that created the issue (identified by its ID). Not all
         * rules have an ID, though, so step one is to check if one is present.
         */
        if (issue.getRuleId().isPresent()) {
            String ruleId = issue.getRuleId().get();
            // Are there exempted iflows for this rule ID?
            if (ruleIdToExemptedIflows.containsKey(ruleId)) {
                // Are all the issue's iflows exempt?
                Set<String> exemptedIflows = ruleIdToExemptedIflows.get(ruleId);
                Set<String> issueIflows = issue.getTags().stream().map(t -> t.getId()).collect(Collectors.toSet());
                if (exemptedIflows.containsAll(issueIflows)) {
                    /*
                     * All the issue's iflows are exempt from this rule, so this issue
                     * is exempt. Log this fact and then return (i.e. the issue is not
                     * passed on to the next consumer).
                     */
                    logger.info(exemptionMessage(issue));
                    return;
                }
            }
        }
        // This issue is not exempted; pass it to the next consumer.
        nextConsumer.consume(issue);
    }

    @Override
    public int issuesConsumed() {
        // Exempted issues that were filtered out should not be counted.
        return nextConsumer.issuesConsumed();
    }

    private static final String exemptionMessage(Issue issue) {
        StringBuilder sb = new StringBuilder();
        sb.append("The following issue was filtered out because all involved iflows ");
        sb.append(issue.getTags()
            .stream()
            .map(t -> t.getId())
            .collect(Collectors.joining("', '", "('", "')")));
        sb.append(" are exempt from rule ID '").append(issue.getRuleId().get()).append("'");
        sb.append(": ").append(issue.getMessage());
        return sb.toString();
    }

}
