package dk.mwittrock.cpilint.consumers;

import dk.mwittrock.cpilint.issues.Issue;

public final class ConsoleIssueConsumer implements IssueConsumer {
	
	private int issuesConsumed = 0;

    @Override
    public void consume(Issue issue) {
        System.out.println(issue.getMessage());
        issuesConsumed++;
    }

	@Override
	public int issuesConsumed() {
		return issuesConsumed;
	}

}