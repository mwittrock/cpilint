package org.cpilint.consumers;

import org.cpilint.issues.Issue;

public final class ConsoleIssueConsumer implements IssueConsumer {
	
	private int issuesConsumed = 0;

    @Override
    public void consume(Issue issue) {
        System.out.println(issue);
        issuesConsumed++;
    }

	@Override
	public int issuesConsumed() {
		return issuesConsumed;
	}

}