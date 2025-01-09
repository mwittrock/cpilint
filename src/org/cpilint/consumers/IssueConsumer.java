package org.cpilint.consumers;

import org.cpilint.issues.Issue;

public interface IssueConsumer {

    public void consume(Issue issue);
    
    public int issuesConsumed();

}