package dk.mwittrock.cpilint.consumers;

import dk.mwittrock.cpilint.issues.Issue;

public interface IssueConsumer {

    public void consume(Issue issue);
    
    public int issuesConsumed();

}